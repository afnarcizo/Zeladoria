package br.com.zeladoria.dto;

import br.com.zeladoria.model.SolicitacaoZeladoria;
import br.com.zeladoria.model.StatusSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de uma solicitação de zeladoria urbana")
public record SolicitacaoResponseDTO(

        @Schema(description = "Identificador único da solicitação gerado pelo JPA (lng)", example = "1")
        Long lngId,

        @Schema(description = "Telefone de contato do cidadão solicitante (str)", example = "(11) 98765-4321")
        String strTelefoneUsuario,

        @Schema(description = "E-mail do cidadão solicitante (str)", example = "cidadao@exemplo.com.br")
        String strEmailUsuario,

        @Schema(description = "Latitude da geolocalização do problema (dbl)", example = "-23.5505199")
        Double dblLatitude,

        @Schema(description = "Longitude da geolocalização do problema (dbl)", example = "-46.6333094")
        Double dblLongitude,

        @Schema(description = "Endereço ou ponto de referência próximo ao local (str)", example = "Av. Paulista, altura do nº 1500")
        String strEnderecoReferencia,

        @Schema(description = "Data e hora em que a solicitação foi registrada (dtm)", example = "2026-10-28T19:05:00")
        LocalDateTime dtmSolicitacao,

        @Schema(description = "Categoria do problema relatado")
        CategoriaProblemaResponseDTO categoria,

        @Schema(description = "Metadados da imagem vinculada (armazenada na tabela ImagemSolicitacao)")
        ImagemResponseDTO imagem,

        @Schema(description = "Descrição detalhada do problema (str)", example = "Buraco profundo na faixa da direita causando risco de acidentes")
        String strDescricao,

        @Schema(description = "Status atual do atendimento da solicitação (str)", example = "ABERTA")
        StatusSolicitacao strStatus,

        @Schema(description = "Data da última atualização do registro (dtm)", example = "2026-10-28T19:05:00")
        LocalDateTime dtmAtualizacao
) {
    public static SolicitacaoResponseDTO fromEntity(SolicitacaoZeladoria entity) {
        return new SolicitacaoResponseDTO(
                entity.getLngId(),
                entity.getStrTelefoneUsuario(),
                entity.getStrEmailUsuario(),
                entity.getDblLatitude(),
                entity.getDblLongitude(),
                entity.getStrEnderecoReferencia(),
                entity.getDtmSolicitacao(),
                CategoriaProblemaResponseDTO.fromEntity(entity.getCategoria()),
                ImagemResponseDTO.fromEntityResumo(entity.getImagem()),
                entity.getStrDescricao(),
                entity.getStrStatus(),
                entity.getDtmAtualizacao()
        );
    }
}
