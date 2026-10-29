package br.com.zeladoria.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status do ciclo de vida de uma solicitação de zeladoria urbana")
public enum StatusSolicitacao {
    @Schema(description = "Solicitação recém-registrada pelo cidadão")
    ABERTA,

    @Schema(description = "Solicitação em análise técnica pela equipe responsável")
    EM_ANALISE,

    @Schema(description = "Equipe em campo executando o reparo ou serviço")
    EM_ANDAMENTO,

    @Schema(description = "Problema resolvido e serviço concluído")
    CONCLUIDA,

    @Schema(description = "Solicitação cancelada ou considerada improcedente")
    CANCELADA
}
