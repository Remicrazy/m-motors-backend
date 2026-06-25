package com.mmotors.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank String nom;
    @NotBlank String prenom;
    @Email @NotBlank String email;
    @NotBlank String telephone;
    @NotBlank @Size(min = 8) String password;
}
