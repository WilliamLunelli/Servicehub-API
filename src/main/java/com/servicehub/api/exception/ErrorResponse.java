package com.servicehub.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Corpo padronizado de resposta para erros da API")
@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @Schema(description = "Codigo de status HTTP", example = "404")
    private int status;

    @Schema(description = "Mensagem resumida do erro", example = "Prestador de servico com id 99 nao foi encontrado")
    private String message;

    @Schema(description = "Momento em que o erro ocorreu", example = "2026-09-22T14:35:10.123")
    private LocalDateTime timestamp;

    @Schema(description = "Detalhes adicionais do erro, como campos invalidos")
    private List<String> details;
}
