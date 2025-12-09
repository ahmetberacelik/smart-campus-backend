package com.smartcampus.auth.dto.response;

import com.smartcampus.auth.entity.Role;
import com.smartcampus.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePicture;
    private Role role;
    private Boolean isVerified;
    private LocalDateTime createdAt;

    // Role-specific info
    private StudentInfo studentInfo;
    private FacultyInfo facultyInfo;

    public static UserResponse fromUser(User user) {
        UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt());

        if (user.getStudent() != null) {
            builder.studentInfo(StudentInfo.builder()
                    .studentNumber(user.getStudent().getStudentNumber())
                    .departmentId(user.getStudent().getDepartment().getId())
                    .departmentName(user.getStudent().getDepartment().getName())
                    .gpa(user.getStudent().getGpa() != null ? user.getStudent().getGpa().doubleValue() : 0.0)
                    .cgpa(user.getStudent().getCgpa() != null ? user.getStudent().getCgpa().doubleValue() : 0.0)
                    .build());
        }

        if (user.getFaculty() != null) {
            builder.facultyInfo(FacultyInfo.builder()
                    .employeeNumber(user.getFaculty().getEmployeeNumber())
                    .title(user.getFaculty().getTitle())
                    .departmentId(user.getFaculty().getDepartment().getId())
                    .departmentName(user.getFaculty().getDepartment().getName())
                    .build());
        }

        return builder.build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private String studentNumber;
        private Long departmentId;
        private String departmentName;
        private Double gpa;
        private Double cgpa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacultyInfo {
        private String employeeNumber;
        private String title;
        private Long departmentId;
        private String departmentName;
    }
}

