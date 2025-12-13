package com.smartcampus.auth.controller;

import com.smartcampus.auth.entity.Department;
import com.smartcampus.auth.repository.DepartmentRepository;
import com.smartcampus.auth.service.EmailService;
import com.smartcampus.auth.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("DepartmentController Integration Tests")
class DepartmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private FileStorageService fileStorageService;

    private Department testDepartment;
    private Department testDepartment2;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();

        testDepartment = Department.builder()
                .name("Bilgisayar Mühendisliği")
                .code("CSE")
                .facultyName("Mühendislik Fakültesi")
                .build();
        testDepartment = departmentRepository.save(testDepartment);

        testDepartment2 = Department.builder()
                .name("Elektrik Mühendisliği")
                .code("EE")
                .facultyName("Mühendislik Fakültesi")
                .build();
        testDepartment2 = departmentRepository.save(testDepartment2);
    }

    @Nested
    @DisplayName("Get All Departments Tests")
    class GetAllDepartmentsTests {

        @Test
        @DisplayName("Should return all departments")
        void shouldReturnAllDepartments() throws Exception {
            mockMvc.perform(get("/api/v1/departments")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[*].code", containsInAnyOrder("CSE", "EE")));
        }

        @Test
        @DisplayName("Should return empty array when no departments exist")
        void shouldReturnEmptyArrayWhenNoDepartments() throws Exception {
            departmentRepository.deleteAll();

            mockMvc.perform(get("/api/v1/departments")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Get Department By Id Tests")
    class GetDepartmentByIdTests {

        @Test
        @DisplayName("Should return department by id")
        void shouldReturnDepartmentById() throws Exception {
            mockMvc.perform(get("/api/v1/departments/{id}", testDepartment.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testDepartment.getId()))
                    .andExpect(jsonPath("$.data.code").value("CSE"))
                    .andExpect(jsonPath("$.data.name").value("Bilgisayar Mühendisliği"));
        }

        @Test
        @DisplayName("Should return 404 when department not found by id")
        void shouldReturn404WhenDepartmentNotFoundById() throws Exception {
            mockMvc.perform(get("/api/v1/departments/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Get Department By Code Tests")
    class GetDepartmentByCodeTests {

        @Test
        @DisplayName("Should return department by code")
        void shouldReturnDepartmentByCode() throws Exception {
            mockMvc.perform(get("/api/v1/departments/code/{code}", "CSE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("CSE"))
                    .andExpect(jsonPath("$.data.name").value("Bilgisayar Mühendisliği"));
        }

        @Test
        @DisplayName("Should return 404 when department not found by code")
        void shouldReturn404WhenDepartmentNotFoundByCode() throws Exception {
            mockMvc.perform(get("/api/v1/departments/code/{code}", "XXX")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}