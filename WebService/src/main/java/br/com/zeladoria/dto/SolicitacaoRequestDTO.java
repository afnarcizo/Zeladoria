package br.com.zeladoria.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "Dados de entrada para registrar uma nova solicitação de zeladoria urbana")
public record SolicitacaoRequestDTO(

        @Schema(description = "Telefone de contato do cidadão solicitante (str)", example = "(11) 98765-4321", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O telefone do usuário (strTelefoneUsuario) é obrigatório")
        @Size(max = 25, message = "O telefone deve ter no máximo 25 caracteres")
        String strTelefoneUsuario,

        @Schema(description = "E-mail do cidadão solicitante (str)", example = "cidadao@exemplo.com.br", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O e-mail do usuário (strEmailUsuario) é obrigatório")
        @Email(message = "Informe um endereço de e-mail válido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String strEmailUsuario,

        @Schema(description = "Latitude da geolocalização do problema (-90.0 a +90.0) (dbl)", example = "-23.5505199", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A latitude da geolocalização (dblLatitude) é obrigatória")
        @DecimalMin(value = "-90.0", message = "A latitude deve ser maior ou igual a -90.0")
        @DecimalMax(value = "90.0", message = "A latitude deve ser menor ou igual a 90.0")
        Double dblLatitude,

        @Schema(description = "Longitude da geolocalização do problema (-180.0 a +180.0) (dbl)", example = "-46.6333094", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A longitude da geolocalização (dblLongitude) é obrigatória")
        @DecimalMin(value = "-180.0", message = "A longitude deve ser maior ou igual a -180.0")
        @DecimalMax(value = "180.0", message = "A longitude deve ser menor ou igual a 180.0")
        Double dblLongitude,

        @Schema(description = "Endereço ou ponto de referência opcional próximo à geolocalização (str)", example = "Av. Paulista, altura do nº 1500")
        @Size(max = 255, message = "O endereço de referência deve ter no máximo 255 caracteres")
        String strEnderecoReferencia,

        @Schema(description = "ID da categoria do problema urbano (lng)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O ID da categoria do problema (lngCategoriaId) é obrigatório")
        Long lngCategoriaId,

        @Schema(description = "Descrição detalhada do problema relatado pelo cidadão (str)", example = "Buraco profundo na faixa da direita causando risco de acidentes", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A descrição do problema (strDescricao) é obrigatória")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String strDescricao,

        @Schema(description = "Imagem comprobatória do problema (será armazenada na tabela separada ImagemSolicitacao)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A imagem do problema é obrigatória")
        @Valid
        ImagemRequestDTO imagem
) {
}
