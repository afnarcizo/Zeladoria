package br.com.zeladoria.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Schema(description = "Estrutura padronizada de erro da API")
    public record ErroPadraoDTO(
            @Schema(description = "Data e hora do erro (dtm)", example = "2026-10-28T19:10:00")
            LocalDateTime dtmTimestamp,
            @Schema(description = "Código HTTP de status (int)", example = "400")
            int intStatus,
            @Schema(description = "Descrição resumida do status (str)", example = "Bad Request")
            String strErro,
            @Schema(description = "Mensagem explicativa do erro (str)", example = "Erro de validação nos campos enviados")
            String strMensagem,
            @Schema(description = "Mapa opcional de campos inválidos")
            Map<String, String> mapDetalhesValidacao
    ) {}

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroPadraoDTO> handleNotFound(RecursoNaoEncontradoException ex) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso Não Encontrado",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroPadraoDTO> handleRegraNegocio(RegraNegocioException ex) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Violação de Regra de Negócio",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoDTO> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erros.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação",
                "Um ou mais campos estão inválidos. Verifique os detalhes.",
                erros
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}
