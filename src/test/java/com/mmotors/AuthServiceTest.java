package com.mmotors;

import com.mmotors.dto.auth.LoginRequest;
import com.mmotors.dto.auth.RegisterRequest;
import com.mmotors.entity.User;
import com.mmotors.repository.UserRepository;
import com.mmotors.security.JwtService;
import com.mmotors.service.AuthService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Tests unitaires")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @Spy PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks AuthService authService;

    @Test
    @DisplayName("register — succès avec données valides")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setNom("Dupont"); req.setPrenom("Jean");
        req.setEmail("jean@test.fr"); req.setTelephone("0612345678");
        req.setPassword("MotDePasse123!");

        when(userRepository.existsByEmail("jean@test.fr")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId("test-uuid-123");
            return u;
        });
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        var response = authService.register(req);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("jean@test.fr");
        assertThat(response.getRole()).isEqualTo("CLIENT");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register — échec si email déjà utilisé")
    void register_emailAlreadyExists_throwsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existant@test.fr");
        req.setPassword("MotDePasse123!");

        when(userRepository.existsByEmail("existant@test.fr")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà utilisé");
    }

    @Test
    @DisplayName("register — mot de passe est haché (BCrypt)")
    void register_passwordIsHashed() {
        RegisterRequest req = new RegisterRequest();
        req.setNom("Test"); req.setPrenom("User");
        req.setEmail("test@test.fr"); req.setTelephone("0600000000");
        req.setPassword("MotDePasse123!");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(jwtService.generateToken(any(), any())).thenReturn("token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId("test-uuid-456");
            return u;
        });

        authService.register(req);

        User savedUser = captor.getValue();
        assertThat(savedUser.getPassword()).isNotEqualTo("MotDePasse123!");
        assertThat(passwordEncoder.matches("MotDePasse123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("login — succès avec bons identifiants")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("jean@test.fr");
        req.setPassword("MotDePasse123!");

        User user = User.builder()
                .email("jean@test.fr")
                .nom("Dupont").prenom("Jean")
                .password(passwordEncoder.encode("MotDePasse123!"))
                .role(User.Role.CLIENT)
                .build();

        user.setId("test-uuid-789");
        when(userRepository.findByEmail("jean@test.fr")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        var response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("jean@test.fr");
    }

    @Test
    @DisplayName("login — échec si utilisateur introuvable")
    void login_userNotFound_throwsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("inconnu@test.fr");
        req.setPassword("password");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}
