package com.mmotors.dto.auth;

import lombok.*;

@Data @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String id;
    private String email;
    private String nom;
    private String prenom;
    private String role;
}
