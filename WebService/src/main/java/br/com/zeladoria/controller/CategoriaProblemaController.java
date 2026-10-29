package br.com.zeladoria.controller;

import br.com.zeladoria.dto.CategoriaProblemaRequestDTO;
import br.com.zeladoria.dto.CategoriaProblemaResponseDTO;
import br.com.zeladoria.exception.GlobalExceptionHandler.ErroPadraoDTO;
import br.com.zeladoria.service.CategoriaProblemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias de Problemas", description = "Endpoints para gerenciamento de categorias de problemas de zeladoria urbana (ex.: Buraco na Via, Iluminação Pública, Coleta de Lixo)")
public class CategoriaProblemaController {

    private final CategoriaProblemaService categoriaService;

    public CategoriaProblemaController(CategoriaProblemaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as categorias ativas",
            description = "Retorna a lista de todas as categorias de problemas de zeladoria urbana não excluídas logicamente (blnExcluido = false)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    })
    public ResponseEntity<List<CategoriaProblemaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/{lngId}")
    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna os detalhes de uma categoria específica ativa pelo seu identificador (lngId)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou excluída logicamente",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<CategoriaProblemaResponseDTO> buscarPorId(
            @Parameter(description = "ID da categoria (lngId)", example = "1", required = true)
            @PathVariable Long lngId
    ) {
        return ResponseEntity.ok(categoriaService.buscarPorId(lngId));
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar nova categoria de problema",
            description = "Cria uma nova categoria de problema urbano com geração de ID (lngId) controlada pelo JPA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Já existe uma categoria com o mesmo nome",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<CategoriaProblemaResponseDTO> criar(
            @Valid @RequestBody CategoriaProblemaRequestDTO dto
    ) {
        CategoriaProblemaResponseDTO criada = categoriaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{lngId}")
    @Operation(
            summary = "Atualizar categoria existente",
            description = "Atualiza o nome (strNome) e a descrição (strDescricao) de uma categoria de problema ativa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Conflito de nome com outra categoria existente",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<CategoriaProblemaResponseDTO> atualizar(
            @Parameter(description = "ID da categoria (lngId)", example = "1", required = true)
            @PathVariable Long lngId,
            @Valid @RequestBody CategoriaProblemaRequestDTO dto
    ) {
        return ResponseEntity.ok(categoriaService.atualizar(lngId, dto));
    }

    @DeleteMapping("/{lngId}")
    @Operation(
            summary = "Excluir logicamente uma categoria",
            description = "Realiza a exclusão lógica (soft delete) da categoria, marcando blnExcluido=true e registrando dtmExclusao sem remover fisicamente a linha do banco de dados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída logicamente com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da categoria a ser excluída logicamente (lngId)", example = "1", required = true)
            @PathVariable Long lngId
    ) {
        categoriaService.excluirLogicamente(lngId);
        return ResponseEntity.noContent().build();
    }
}
