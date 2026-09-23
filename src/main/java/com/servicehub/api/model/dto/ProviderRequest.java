package com.servicehub.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Dados de entrada para criar ou substituir um prestador de servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRequest {

    @Schema(description = "Nome completo do prestador", example = "Carlos Eduardo Souza")
    @NotBlank(message = "O nome e obrigatorio")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Schema(description = "E-mail de contato, usado como identificador unico", example = "carlos.souza@example.com")
    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "O e-mail informado nao e valido")
    private String email;

    @Schema(description = "Telefone de contato no formato brasileiro", example = "(11) 98765-4321")
    @Pattern(regexp = "^\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}$", message = "O telefone deve seguir o formato (XX) XXXXX-XXXX")
    private String phone;

    @Schema(description = "Area de especialidade do prestador", example = "Eletricista")
    @NotBlank(message = "A especialidade e obrigatoria")
    @Size(max = 80, message = "A especialidade deve ter no maximo 80 caracteres")
    private String specialty;

    @Schema(description = "Descricao dos servicos oferecidos", example = "Instalacoes eletricas residenciais e comerciais, com 10 anos de experiencia")
    @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
    private String description;

    @Schema(description = "Valor cobrado por hora de servico", example = "85.50")
    @NotNull(message = "O valor da hora e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "O valor da hora deve ser maior que zero")
    private BigDecimal hourlyRate;
}
