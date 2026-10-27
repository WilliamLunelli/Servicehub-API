package com.servicehub.api.controller;

import com.servicehub.api.config.SecurityConfig;
import com.servicehub.api.exception.ProviderNotFoundException;
import com.servicehub.api.model.dto.ProviderRequest;
import com.servicehub.api.model.dto.ProviderResponse;
import com.servicehub.api.security.CustomUserDetailsService;
import com.servicehub.api.security.JwtService;
import com.servicehub.api.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderController.class)
@Import(SecurityConfig.class)
class ProviderControllerTest {

    private static final String VALID_BODY = """
            {"name":"Carlos Souza","email":"carlos@example.com","phone":"(11) 98765-4321",
             "specialty":"Eletricista","description":"Instalacoes","hourlyRate":85.50}
            """;

    private static final String INVALID_EMAIL_BODY = """
            {"name":"Carlos Souza","email":"email-invalido","specialty":"Eletricista","hourlyRate":85.50}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderService providerService;

    // exigidos pelo contexto de seguranca (filtro JWT e autenticacao), simulados nesta camada
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private ProviderResponse response() {
        return ProviderResponse.builder()
                .id(1L).name("Carlos Souza").email("carlos@example.com")
                .specialty("Eletricista").hourlyRate(new BigDecimal("85.50")).active(true).build();
    }

    @Test
    void deveRetornar401QuandoRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/providers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deveRetornar200ComListaQuandoClienteAutenticado() throws Exception {
        when(providerService.findAll(null)).thenReturn(List.of(response()));

        mockMvc.perform(get("/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Carlos Souza"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deveRetornar403QuandoClienteTentaCriarPrestador() throws Exception {
        mockMvc.perform(post("/providers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(providerService, never()).create(any(ProviderRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201ComLocationQuandoAdminCriaPrestador() throws Exception {
        when(providerService.create(any(ProviderRequest.class))).thenReturn(response());

        mockMvc.perform(post("/providers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/providers/1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400ComDetalhesQuandoEmailInvalido() throws Exception {
        mockMvc.perform(post("/providers").contentType(MediaType.APPLICATION_JSON).content(INVALID_EMAIL_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deveRetornar404QuandoPrestadorNaoExiste() throws Exception {
        when(providerService.findById(99L)).thenThrow(new ProviderNotFoundException(99L));

        mockMvc.perform(get("/providers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    void deveRetornar403QuandoPrestadorTentaRemover() throws Exception {
        mockMvc.perform(delete("/providers/1"))
                .andExpect(status().isForbidden());

        verify(providerService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204QuandoAdminRemovePrestador() throws Exception {
        mockMvc.perform(delete("/providers/1"))
                .andExpect(status().isNoContent());

        verify(providerService).delete(1L);
    }
}
