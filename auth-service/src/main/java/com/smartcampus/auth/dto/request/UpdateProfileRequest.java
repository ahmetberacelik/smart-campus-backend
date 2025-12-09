package com.smartcampus.auth.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
    private String firstName;

    @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
    private String lastName;

    @Size(max = 20, message = "Telefon numarası en fazla 20 karakter olabilir")
    private String phoneNumber;
}

