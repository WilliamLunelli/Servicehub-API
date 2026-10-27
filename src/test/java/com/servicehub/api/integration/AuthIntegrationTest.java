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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integracao (contexto completo + H2): cadastro, login, JWT e permissoes por perfil.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

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

    private void register(String email, String accountType) throws Exception {
        String body = """
                {"name":"Usuario Teste","email":"%s","password":"%s","accountType":"%s"}
                """.formatted(email, PASSWORD, accountType);
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        MvcResult result = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String createAdminAndLogin() throws Exception {
        userRepository.save(User.builder()
                .name("Admin Teste")
                .email("admin@example.com")
                .password(passwordEncoder.encode(PASSWORD))
                .role(Role.ADMIN)
                .active(true)
                .build());
        return login("admin@example.com", PASSWORD);
    }

    @Test
    void deveGravarSenhaComoHashBcryptQuandoUsuarioSeCadastra() throws Exception {
        register("maria@example.com", "CLIENT");

        User saved = userRepository.findByEmailIgnoreCase("maria@example.com").orElseThrow();
        assertThat(saved.getPassword()).startsWith("$2").isNotEqualTo(PASSWORD);
    }

    @Test
    void deveGerarTokenEPermitirLeituraQuandoClienteFazLogin() throws Exception {
        register("maria@example.com", "CLIENT");
        String token = login("maria@example.com", PASSWORD);

        mockMvc.perform(get("/providers").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401QuandoRotaProtegidaChamadaSemToken() throws Exception {
        mockMvc.perform(get("/providers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401QuandoTokenForAdulterado() throws Exception {
        register("maria@example.com", "CLIENT");
        String token = login("maria@example.com", PASSWORD);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        mockMvc.perform(get("/providers").header(HttpHeaders.AUTHORIZATION, bearer(tampered)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401ComMensagemUnicaQuandoSenhaIncorreta() throws Exception {
        register("maria@example.com", "CLIENT");
        String body = """
                {"email":"maria@example.com","password":"senha-errada"}
                """;

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais invalidas"));
    }

    @Test
    void deveRetornar409QuandoEmailJaCadastrado() throws Exception {
        register("maria@example.com", "CLIENT");
        String body = """
                {"name":"Outra Maria","email":"maria@example.com","password":"%s"}
                """.formatted(PASSWORD);

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoTentaCadastrarComoAdmin() throws Exception {
        String body = """
                {"name":"Invasor","email":"invasor@example.com","password":"%s","accountType":"ADMIN"}
                """.formatted(PASSWORD);

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveIgnorarCampoRoleEnviadoNoCorpoDoCadastro() throws Exception {
        String body = """
                {"name":"Invasor","email":"invasor@example.com","password":"%s","role":"ADMIN"}
                """.formatted(PASSWORD);

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void deveRetornar403QuandoClienteTentaCriarPrestador() throws Exception {
        register("maria@example.com", "CLIENT");
        String token = login("maria@example.com", PASSWORD);

        mockMvc.perform(post("/providers")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("carlos@example.com", "Eletricista")))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirQuePrestadorCrieMasNaoRemova() throws Exception {
        register("joao@example.com", "PROVIDER");
        String token = login("joao@example.com", PASSWORD);

        MvcResult created = mockMvc.perform(post("/providers")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("carlos@example.com", "Eletricista")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/providers/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirQueAdminCrieERemovaPrestador() throws Exception {
        String token = createAdminAndLogin();

        MvcResult created = mockMvc.perform(post("/providers")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROVIDER_BODY.formatted("carlos@example.com", "Eletricista")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/providers/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/providers/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveFiltrarPorEspecialidadeEResistirATentativaDeSqlInjection() throws Exception {
        String token = createAdminAndLogin();
        mockMvc.perform(post("/providers").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROVIDER_BODY.formatted("eletricista@example.com", "Eletricista")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/providers").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROVIDER_BODY.formatted("encanador@example.com", "Encanador")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/providers").param("specialty", "eletri")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/providers").param("specialty", "' OR '1'='1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
