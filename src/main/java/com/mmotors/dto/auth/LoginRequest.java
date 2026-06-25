package com.mmotors.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {
    @Email @NotBlank String email;
    @NotBlank String password;
}
