package com.smartcampus.auth.dto.request;

import com.smartcampus.auth.entity.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email adresi zorunludur")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre zorunludur")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir"
    )
    private String password;

    @NotBlank(message = "Ad zorunludur")
    @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
    private String firstName;

    @NotBlank(message = "Soyad zorunludur")
    @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
    private String lastName;

    @Size(max = 20, message = "Telefon numarası en fazla 20 karakter olabilir")
    private String phoneNumber;

    @NotNull(message = "Kullanıcı tipi zorunludur")
    private Role role;

    @NotNull(message = "Bölüm ID zorunludur")
    private Long departmentId;

    // Öğrenci için zorunlu
    private String studentNumber;

    // Öğretim üyesi için zorunlu
    private String employeeNumber;

    // Öğretim üyesi için zorunlu
    private String title;
}

