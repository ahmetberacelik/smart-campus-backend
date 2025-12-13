package com.smartcampus.auth.service;

import com.smartcampus.auth.dto.response.DepartmentResponse;
import com.smartcampus.auth.entity.Department;
import com.smartcampus.auth.exception.ResourceNotFoundException;
import com.smartcampus.auth.repository.DepartmentRepository;
import com.smartcampus.auth.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService Unit Tests")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department testDepartment;
    private Department testDepartment2;

    @BeforeEach
    void setUp() {
        testDepartment = Department.builder()
                .id(1L)
                .name("Bilgisayar Mühendisliği")
                .code("CSE")
                .facultyName("Mühendislik Fakültesi")
                .build();

        testDepartment2 = Department.builder()
                .id(2L)
                .name("Elektrik Mühendisliği")
                .code("EE")
                .facultyName("Mühendislik Fakültesi")
                .build();
    }

    @Nested
    @DisplayName("Get All Departments Tests")
    class GetAllDepartmentsTests {

        @Test
        @DisplayName("Should return all departments")
        void shouldReturnAllDepartments() {
            when(departmentRepository.findAll()).thenReturn(Arrays.asList(testDepartment, testDepartment2));

            List<DepartmentResponse> result = departmentService.getAllDepartments();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(departmentRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no departments exist")
        void shouldReturnEmptyListWhenNoDepartments() {
            when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

            List<DepartmentResponse> result = departmentService.getAllDepartments();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(departmentRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Get Department By Id Tests")
    class GetDepartmentByIdTests {

        @Test
        @DisplayName("Should return department by id")
        void shouldReturnDepartmentById() {
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));

            DepartmentResponse result = departmentService.getDepartmentById(1L);

            assertNotNull(result);
            assertEquals(testDepartment.getCode(), result.getCode());
            assertEquals(testDepartment.getName(), result.getName());
            verify(departmentRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when department not found by id")
        void shouldThrowExceptionWhenDepartmentNotFoundById() {
            when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> departmentService.getDepartmentById(999L));
            verify(departmentRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Get Department By Code Tests")
    class GetDepartmentByCodeTests {

        @Test
        @DisplayName("Should return department by code")
        void shouldReturnDepartmentByCode() {
            when(departmentRepository.findByCode("CSE")).thenReturn(Optional.of(testDepartment));

            DepartmentResponse result = departmentService.getDepartmentByCode("CSE");

            assertNotNull(result);
            assertEquals(testDepartment.getCode(), result.getCode());
            assertEquals(testDepartment.getName(), result.getName());
            verify(departmentRepository, times(1)).findByCode("CSE");
        }

        @Test
        @DisplayName("Should throw exception when department not found by code")
        void shouldThrowExceptionWhenDepartmentNotFoundByCode() {
            when(departmentRepository.findByCode("XXX")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> departmentService.getDepartmentByCode("XXX"));
            verify(departmentRepository, times(1)).findByCode("XXX");
        }
    }
}