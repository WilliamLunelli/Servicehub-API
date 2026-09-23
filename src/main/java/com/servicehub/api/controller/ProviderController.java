package com.servicehub.api.controller;

import com.servicehub.api.exception.ErrorResponse;
import com.servicehub.api.model.dto.ProviderPatchRequest;
import com.servicehub.api.model.dto.ProviderRequest;
import com.servicehub.api.model.dto.ProviderResponse;
import com.servicehub.api.service.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Providers", description = "Gerenciamento de prestadores de servico")
@RestController
@RequestMapping("/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @Operation(
            summary = "Lista todos os prestadores",
            description = "Retorna todos os prestadores de servico cadastrados. "
                    + "Caso nao haja nenhum cadastro, retorna uma lista vazia com status 200."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de prestadores retornada com sucesso",
                    content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                            schema = @Schema(implementation = ProviderResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<ProviderResponse>> findAll() {
        return ResponseEntity.ok(providerService.findAll());
    }

    @Operation(
            summary = "Busca um prestador por id",
            description = "Retorna os dados de um unico prestador de servico a partir do seu identificador numerico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prestador encontrado",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum prestador foi encontrado com o id informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> findById(
            @Schema(description = "Identificador do prestador", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(providerService.findById(id));
    }

    @Operation(
            summary = "Cadastra um novo prestador",
            description = "Cria um novo prestador de servico. O e-mail deve ser unico entre todos os prestadores cadastrados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Prestador criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada invalidos ou campos obrigatorios ausentes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ja existe um prestador cadastrado com o mesmo e-mail",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ProviderResponse> create(@Valid @RequestBody ProviderRequest request) {
        ProviderResponse created = providerService.create(request);
        return ResponseEntity.created(URI.create("/providers/" + created.getId())).body(created);
    }

    @Operation(
            summary = "Substitui um prestador por inteiro",
            description = "Atualiza todos os campos de um prestador existente, substituindo o registro por completo. "
                    + "Todos os campos obrigatorios devem ser enviados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prestador atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada invalidos ou campos obrigatorios ausentes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum prestador foi encontrado com o id informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "O e-mail informado ja pertence a outro prestador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProviderResponse> replace(
            @Schema(description = "Identificador do prestador", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProviderRequest request) {
        return ResponseEntity.ok(providerService.replace(id, request));
    }

    @Operation(
            summary = "Atualiza campos especificos de um prestador",
            description = "Atualiza parcialmente um prestador existente. Apenas os campos enviados no corpo da "
                    + "requisicao sao alterados; os demais permanecem inalterados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prestador atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProviderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum prestador foi encontrado com o id informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "O e-mail informado ja pertence a outro prestador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProviderResponse> patch(
            @Schema(description = "Identificador do prestador", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProviderPatchRequest request) {
        return ResponseEntity.ok(providerService.patch(id, request));
    }

    @Operation(
            summary = "Remove um prestador",
            description = "Remove definitivamente um prestador de servico a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Prestador removido com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nenhum prestador foi encontrado com o id informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Schema(description = "Identificador do prestador", example = "1") @PathVariable Long id) {
        providerService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
