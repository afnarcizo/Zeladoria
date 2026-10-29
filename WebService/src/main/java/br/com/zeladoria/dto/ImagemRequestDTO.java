package br.com.zeladoria.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados da imagem do problema relatado para armazenamento em tabela separada")
public record ImagemRequestDTO(

        @Schema(description = "Nome original do arquivo de imagem (str)", example = "buraco_avenida_central.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do arquivo da imagem (strNomeArquivo) é obrigatório")
        @Size(max = 255, message = "O nome do arquivo deve ter no máximo 255 caracteres")
        String strNomeArquivo,

        @Schema(description = "MIME type da imagem (str)", example = "image/jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O tipo de conteúdo (strTipoConteudo) é obrigatório")
        @Size(max = 100, message = "O tipo de conteúdo deve ter no máximo 100 caracteres")
        String strTipoConteudo,

        @Schema(description = "Conteúdo da imagem codificado em Base64 (str)", example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Os dados da imagem em Base64 (strDadosBase64) são obrigatórios")
        String strDadosBase64
) {
}
