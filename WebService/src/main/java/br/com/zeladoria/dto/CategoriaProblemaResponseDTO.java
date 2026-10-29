package br.com.zeladoria.dto;

import br.com.zeladoria.model.CategoriaProblema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de uma categoria de problema urbano")
public record CategoriaProblemaResponseDTO(

        @Schema(description = "Identificador único da categoria gerado pelo JPA (lng)", example = "1")
        Long lngId,

        @Schema(description = "Nome da categoria do problema (str)", example = "Buraco na Via")
        String strNome,

        @Schema(description = "Descrição detalhada da categoria (str)", example = "Buracos, afundamentos ou danos no pavimento asfáltico de logradouros públicos")
        String strDescricao,

        @Schema(description = "Data e hora de criação do registro (dtm)", example = "2026-10-28T19:00:00")
        LocalDateTime dtmCriacao
) {
    public static CategoriaProblemaResponseDTO fromEntity(CategoriaProblema entity) {
        return new CategoriaProblemaResponseDTO(
                entity.getLngId(),
                entity.getStrNome(),
                entity.getStrDescricao(),
                entity.getDtmCriacao()
        );
    }
}
