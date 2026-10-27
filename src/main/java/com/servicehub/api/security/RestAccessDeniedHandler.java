package com.servicehub.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicehub.api.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** Responde 403 em JSON quando o usuario esta autenticado mas nao tem permissao. */
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("Acesso negado: seu perfil nao tem permissao para esta operacao")
                .timestamp(LocalDateTime.now())
                .details(List.of())
                .build());
    }
}
