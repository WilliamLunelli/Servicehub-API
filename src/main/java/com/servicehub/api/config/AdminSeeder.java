package com.servicehub.api.config;

import com.servicehub.api.model.Role;
import com.servicehub.api.model.User;
import com.servicehub.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador a partir das variaveis de ambiente ADMIN_EMAIL e ADMIN_PASSWORD.
 * Sem elas, nada e criado. O ADMIN nunca pode ser pedido pelo endpoint publico de cadastro.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email:}") String adminEmail,
                       @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        userRepository.save(User.builder()
                .name("Administrador")
                .email(adminEmail.trim().toLowerCase())
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .active(true)
                .build());
    }
}
