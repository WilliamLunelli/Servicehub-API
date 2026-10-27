package com.servicehub.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Credenciais de login")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(description = "E-mail cadastrado", example = "maria@example.com")
    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "O e-mail informado nao e valido")
    private String email;

    @Schema(description = "Senha do usuario", example = "SenhaForte123")
    @NotBlank(message = "A senha e obrigatoria")
    private String password;
}
