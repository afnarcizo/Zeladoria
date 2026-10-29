package br.com.zeladoria.dto;

import br.com.zeladoria.model.StatusSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualização do status de atendimento de uma solicitação")
public record AtualizarStatusRequestDTO(

        @Schema(description = "Novo status da solicitação (str)", example = "EM_ANDAMENTO", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O novo status da solicitação (strStatus) é obrigatório")
        StatusSolicitacao strStatus
) {
}
