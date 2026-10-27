package com.servicehub.api.controller;

import com.servicehub.api.exception.ProviderNotFoundException;
import com.servicehub.api.model.dto.ProviderResponse;
import com.servicehub.api.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@GraphQlTest(ProviderGraphQLController.class)
class ProviderGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private ProviderService providerService;

    @Test
    void deveRetornarApenasOsCamposSolicitadosNaQuery() {
        when(providerService.findAll(null)).thenReturn(List.of(ProviderResponse.builder()
                .id(1L).name("Carlos Souza").email("carlos@example.com")
                .specialty("Eletricista").hourlyRate(new BigDecimal("85.50")).active(true).build()));

        graphQlTester.document("{ providers { name specialty } }")
                .execute()
                .path("providers[*].name").entityList(String.class).containsExactly("Carlos Souza")
                .path("providers[*].specialty").entityList(String.class).containsExactly("Eletricista");
    }

    @Test
    void deveRetornarErroNotFoundQuandoPrestadorNaoExiste() {
        when(providerService.findById(99L)).thenThrow(new ProviderNotFoundException(99L));

        graphQlTester.document("{ provider(id: 99) { name } }")
                .execute()
                .errors()
                .expect(error -> error.getErrorType() == ErrorType.NOT_FOUND)
                .verify();
    }
}
