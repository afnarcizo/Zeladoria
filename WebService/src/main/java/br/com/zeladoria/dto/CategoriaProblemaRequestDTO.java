package br.com.zeladoria.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de entrada para criação ou atualização de uma categoria de problema urbano")
public record CategoriaProblemaRequestDTO(

        @Schema(description = "Nome da categoria do problema (str)", example = "Iluminação Pública", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome da categoria (strNome) é obrigatório")
        @Size(max = 100, message = "O nome da categoria deve ter no máximo 100 caracteres")
        String strNome,

        @Schema(description = "Descrição detalhada da categoria (str)", example = "Problemas relacionados a lâmpadas queimadas, postes danificados ou falta de iluminação em vias públicas")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String strDescricao
) {
}
