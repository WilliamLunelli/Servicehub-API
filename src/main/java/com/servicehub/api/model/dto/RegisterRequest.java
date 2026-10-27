package com.servicehub.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Dados de entrada para cadastro de um novo usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Schema(description = "Nome completo", example = "Maria Oliveira")
    @NotBlank(message = "O nome e obrigatorio")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Schema(description = "E-mail usado para login", example = "maria@example.com")
    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "O e-mail informado nao e valido")
    private String email;

    @Schema(description = "Senha com no minimo 8 caracteres", example = "SenhaForte123")
    @NotBlank(message = "A senha e obrigatoria")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    private String password;

    @Schema(description = "Tipo de conta (CLIENT ou PROVIDER). Se omitido, a conta e CLIENT", example = "CLIENT")
    private AccountType accountType;
}
