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
import com.servicehub.api.util.InputSanitizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        AccountType accountType = request.getAccountType() != null ? request.getAccountType() : AccountType.CLIENT;

        User user = User.builder()
                .name(InputSanitizer.clean(request.getName()))
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(accountType.name()))
                .active(true)
                .build();

        User saved = userRepository.save(user);

        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword()));

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        Role role = Role.valueOf(authority.substring(ROLE_PREFIX.length()));

        String token = jwtService.generateToken(authentication.getName(), role);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .build();
    }
}
