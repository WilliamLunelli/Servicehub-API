package com.servicehub.api.integration;

import com.jayway.jsonpath.JsonPath;
import com.servicehub.api.model.Role;
import com.servicehub.api.model.User;
import com.servicehub.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integracao com PostgreSQL REAL em container (Testcontainers). Exige Docker em execucao;
 * sem Docker o teste e ignorado (disabledWithoutDocker) em vez de falhar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ProviderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    private static final String PASSWORD = "SenhaForte123";

    private static final String PROVIDER_BODY = """
            {"name":"Carlos Souza","email":"%s","phone":"(11) 98765-4321",
             "specialty":"%s","description":"Servicos","hourlyRate":85.50}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken() throws Exception {
        userRepository.save(User.builder()
                .name("Admin Teste")
                .email("admin@example.com")
                .password(passwordEncoder.encode(PASSWORD))
                .role(Role.ADMIN)
                .active(true)
                .build());
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + JsonPath.<String>read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void deveCriarEConsultarPrestadorNoPostgresReal() throws Exception {
        String token = adminToken();

        MvcResult created = mockMvc.perform(post("/providers")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("carlos@example.com", "Eletricista")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/providers/" + id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carlos@example.com"));
    }

    @Test
    void deveBuscarPorEspecialidadeComConsultaNomeadaNoPostgres() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/providers").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("eletricista@example.com", "Eletricista")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/providers").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("encanador@example.com", "Encanador")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/providers").param("specialty", "ELETRI")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveRetornar409QuandoEmailDuplicadoNoPostgres() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/providers").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("carlos@example.com", "Eletricista")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/providers").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("carlos@example.com", "Pintor")))
                .andExpect(status().isConflict());
    }
}
