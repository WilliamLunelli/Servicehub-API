package com.servicehub.api.controller;

import com.servicehub.api.exception.ProviderNotFoundException;
import com.servicehub.api.model.dto.ProviderResponse;
import com.servicehub.api.service.ProviderService;
import graphql.GraphQLError;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Endpoint alternativo em GraphQL (/graphql): o cliente escolhe exatamente os campos que quer,
 * evitando over-fetching. Protegido pelo mesmo JWT e pelas mesmas regras de perfil do REST.
 */
@Controller
public class ProviderGraphQLController {

    private final ProviderService providerService;

    public ProviderGraphQLController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','CLIENT')")
    public List<ProviderResponse> providers(@Argument String specialty) {
        return providerService.findAll(specialty);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','CLIENT')")
    public ProviderResponse provider(@Argument Long id) {
        return providerService.findById(id);
    }

    @GraphQlExceptionHandler
    public GraphQLError handleNotFound(ProviderNotFoundException ex) {
        return GraphQLError.newError()
                .errorType(ErrorType.NOT_FOUND)
                .message(ex.getMessage())
                .build();
    }
}
