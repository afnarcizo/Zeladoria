package br.com.zeladoria.controller;

import br.com.zeladoria.model.StatusSolicitacao;
import br.com.zeladoria.dto.AtualizarStatusRequestDTO;
import br.com.zeladoria.dto.ImagemResponseDTO;
import br.com.zeladoria.dto.SolicitacaoRequestDTO;
import br.com.zeladoria.dto.SolicitacaoResponseDTO;
import br.com.zeladoria.exception.GlobalExceptionHandler.ErroPadraoDTO;
import br.com.zeladoria.service.SolicitacaoZeladoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@Tag(name = "Solicitações de Zeladoria", description = "Endpoints para registro, consulta, acompanhamento de status, obtenção de imagem e exclusão lógica de solicitações de zeladoria urbana")
public class SolicitacaoZeladoriaController {

    private final SolicitacaoZeladoriaService solicitacaoService;

    public SolicitacaoZeladoriaController(SolicitacaoZeladoriaService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar solicitações de zeladoria (paginado)",
            description = "Retorna uma lista paginada de solicitações ativas (blnExcluido = false), permitindo filtrar por strStatus e lngCategoriaId."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de solicitações retornada com sucesso")
    })
    public ResponseEntity<Page<SolicitacaoResponseDTO>> listar(
            @Parameter(description = "Filtro opcional pelo status da solicitação (strStatus)", example = "ABERTA")
            @RequestParam(required = false) StatusSolicitacao strStatus,
            @Parameter(description = "Filtro opcional pelo ID da categoria do problema (lngCategoriaId)", example = "1")
            @RequestParam(required = false) Long lngCategoriaId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "dtmSolicitacao", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(solicitacaoService.listar(strStatus, lngCategoriaId, pageable));
    }

    @GetMapping("/{lngId}")
    @Operation(
            summary = "Buscar solicitação de zeladoria por ID",
            description = "Retorna os dados completos de uma solicitação ativa (lngId) e os metadados de sua imagem vinculada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada ou excluída logicamente",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(
            @Parameter(description = "ID da solicitação (lngId)", example = "1", required = true)
            @PathVariable Long lngId
    ) {
        return ResponseEntity.ok(solicitacaoService.buscarPorId(lngId));
    }

    @GetMapping("/{lngId}/imagem")
    @Operation(
            summary = "Obter a imagem de uma solicitação",
            description = "Recupera o registro completo da imagem (incluindo o payload strDadosBase64) armazenada na tabela separada ImagemSolicitacao."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagem retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação ou imagem não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<ImagemResponseDTO> buscarImagem(
            @Parameter(description = "ID da solicitação (lngId)", example = "1", required = true)
            @PathVariable Long lngId
    ) {
        return ResponseEntity.ok(solicitacaoService.buscarImagemDaSolicitacao(lngId));
    }

    @PostMapping
    @Operation(
            summary = "Registrar nova solicitação de zeladoria urbana",
            description = "Registra um novo relato de problema urbano contendo strTelefoneUsuario, strEmailUsuario, geolocalização (dblLatitude/dblLongitude), lngCategoriaId, strDescricao e imagem (persistida em tabela separada ImagemSolicitacao)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados informados",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria informada não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Violação de regra de negócio (ex.: Base64 inválido)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<SolicitacaoResponseDTO> criar(
            @Valid @RequestBody SolicitacaoRequestDTO dto
    ) {
        SolicitacaoResponseDTO criada = solicitacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PatchMapping("/{lngId}/status")
    @Operation(
            summary = "Atualizar status da solicitação",
            description = "Atualiza o status de andamento (strStatus) de uma solicitação ativa (ex.: ABERTA -> EM_ANALISE -> EM_ANDAMENTO -> CONCLUIDA)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Transição de status não permitida para solicitação já finalizada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<SolicitacaoResponseDTO> atualizarStatus(
            @Parameter(description = "ID da solicitação (lngId)", example = "1", required = true)
            @PathVariable Long lngId,
            @Valid @RequestBody AtualizarStatusRequestDTO dto
    ) {
        return ResponseEntity.ok(solicitacaoService.atualizarStatus(lngId, dto));
    }

    @DeleteMapping("/{lngId}")
    @Operation(
            summary = "Excluir logicamente uma solicitação de zeladoria",
            description = "Realiza a exclusão lógica (soft delete) da solicitação e de sua respectiva imagem na tabela separada, mantendo os registros físicos no banco com blnExcluido=true."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitação e imagem excluídas logicamente com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da solicitação a ser excluída logicamente (lngId)", example = "1", required = true)
            @PathVariable Long lngId
    ) {
        solicitacaoService.excluirLogicamente(lngId);
        return ResponseEntity.noContent().build();
    }
}
