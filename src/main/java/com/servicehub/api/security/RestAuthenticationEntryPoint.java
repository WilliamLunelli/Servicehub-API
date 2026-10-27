package com.servicehub.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicehub.api.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** Responde 401 em JSON quando a requisicao nao esta autenticada (sem token ou token invalido). */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Autenticacao necessaria: envie um token JWT valido no header Authorization")
                .timestamp(LocalDateTime.now())
                .details(List.of())
                .build());
    }
}
