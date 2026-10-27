package com.servicehub.api.service;

import com.servicehub.api.exception.EmailAlreadyUsedException;
import com.servicehub.api.model.Role;
import com.servicehub.api.model.User;
import com.servicehub.api.model.dto.AccountType;
import com.servicehub.api.model.dto.AuthResponse;
import com.servicehub.api.model.dto.LoginRequest;
import com.servicehub.api.model.dto.RegisterRequest;
import com.servicehub.api.model.dto.UserResponse;
import com.servicehub.api.repository.UserRepository;
import com.servicehub.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveCadastrarClienteComSenhaCriptografadaQuandoTipoDeContaOmitido() {
        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SenhaForte123")).thenReturn("HASH_BCRYPT");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserResponse response = authService.register(
                new RegisterRequest("Maria Oliveira", "  Maria@Example.com ", "SenhaForte123", null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(response.getRole()).isEqualTo(Role.CLIENT);
        assertThat(response.getEmail()).isEqualTo("maria@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("HASH_BCRYPT").isNotEqualTo("SenhaForte123");
    }

    @Test
    void deveCadastrarPrestadorQuandoTipoDeContaForProvider() {
        when(userRepository.existsByEmailIgnoreCase("joao@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SenhaForte123")).thenReturn("HASH_BCRYPT");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(
                new RegisterRequest("Joao Silva", "joao@example.com", "SenhaForte123", AccountType.PROVIDER));

        assertThat(response.getRole()).isEqualTo(Role.PROVIDER);
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Maria Oliveira", "maria@example.com", "SenhaForte123", null)))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveGerarTokenQuandoCredenciaisValidas() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "maria@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken("maria@example.com", Role.PROVIDER)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("maria@example.com", "SenhaForte123"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
    }

    @Test
    void devePropagarExcecaoQuandoCredenciaisInvalidas() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalidas"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("maria@example.com", "errada")))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }
}
