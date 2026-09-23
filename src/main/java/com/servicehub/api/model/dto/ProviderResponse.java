package com.servicehub.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Representacao de um prestador de servico retornada pela API")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderResponse {

    @Schema(description = "Identificador unico do prestador", example = "1")
    private Long id;

    @Schema(description = "Nome completo do prestador", example = "Carlos Eduardo Souza")
    private String name;

    @Schema(description = "E-mail de contato", example = "carlos.souza@example.com")
    private String email;

    @Schema(description = "Telefone de contato", example = "(11) 98765-4321")
    private String phone;

    @Schema(description = "Area de especialidade do prestador", example = "Eletricista")
    private String specialty;

    @Schema(description = "Descricao dos servicos oferecidos", example = "Instalacoes eletricas residenciais e comerciais, com 10 anos de experiencia")
    private String description;

    @Schema(description = "Valor cobrado por hora de servico", example = "85.50")
    private BigDecimal hourlyRate;

    @Schema(description = "Indica se o prestador esta ativo na plataforma", example = "true")
    private boolean active;
}
