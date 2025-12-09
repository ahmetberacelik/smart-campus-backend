package com.smartcampus.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Mevcut şifre zorunludur")
    private String currentPassword;

    @NotBlank(message = "Yeni şifre zorunludur")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir"
    )
    private String newPassword;
}

