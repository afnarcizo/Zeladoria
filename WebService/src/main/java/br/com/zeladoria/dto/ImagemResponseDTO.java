package br.com.zeladoria.dto;

import br.com.zeladoria.model.ImagemSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Metadados e conteúdo da imagem armazenada na tabela separada ImagemSolicitacao")
public record ImagemResponseDTO(

        @Schema(description = "Identificador único da imagem gerado pelo JPA (lng)", example = "1")
        Long lngId,

        @Schema(description = "Nome do arquivo da imagem (str)", example = "buraco_avenida_central.jpg")
        String strNomeArquivo,

        @Schema(description = "MIME type da imagem (str)", example = "image/jpeg")
        String strTipoConteudo,

        @Schema(description = "Tamanho estimado da imagem em bytes (lng)", example = "68")
        Long lngTamanhoBytes,

        @Schema(description = "Conteúdo da imagem em Base64 (str - incluído apenas na consulta direta de imagem)")
        String strDadosBase64,

        @Schema(description = "Data de criação do registro da imagem (dtm)", example = "2026-10-28T19:00:00")
        LocalDateTime dtmCriacao
) {
    public static ImagemResponseDTO fromEntityResumo(ImagemSolicitacao entity) {
        if (entity == null) return null;
        return new ImagemResponseDTO(
                entity.getLngId(),
                entity.getStrNomeArquivo(),
                entity.getStrTipoConteudo(),
                entity.getLngTamanhoBytes(),
                null,
                entity.getDtmCriacao()
        );
    }

    public static ImagemResponseDTO fromEntityCompleto(ImagemSolicitacao entity) {
        if (entity == null) return null;
        return new ImagemResponseDTO(
                entity.getLngId(),
                entity.getStrNomeArquivo(),
                entity.getStrTipoConteudo(),
                entity.getLngTamanhoBytes(),
                entity.getStrDadosBase64(),
                entity.getDtmCriacao()
        );
    }
}
