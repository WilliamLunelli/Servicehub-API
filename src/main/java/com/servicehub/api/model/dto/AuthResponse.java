package com.servicehub.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Resposta do login com o token JWT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    @Schema(description = "Token JWT assinado")
    private String token;

    @Schema(description = "Tipo do token, usado no header Authorization", example = "Bearer")
    private String tokenType;

    @Schema(description = "Validade do token em segundos", example = "86400")
    private long expiresIn;
}
