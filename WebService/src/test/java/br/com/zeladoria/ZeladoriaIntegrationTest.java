package br.com.zeladoria;

import br.com.zeladoria.model.StatusSolicitacao;
import br.com.zeladoria.dto.AtualizarStatusRequestDTO;
import br.com.zeladoria.dto.CategoriaProblemaRequestDTO;
import br.com.zeladoria.dto.ImagemRequestDTO;
import br.com.zeladoria.dto.SolicitacaoRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ZeladoriaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private static final String SAMPLE_BASE64_PNG = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @Test
    @DisplayName("Deve gerar especificacao OpenAPI 3 (Swagger) e validar ciclo completo com IDs pelo JPA, atributos com prefixos CamelCase e exclusao logica")
    void deveValidarFluxoCompletoEExclusaoLogica() throws Exception {
        // 1. Exportar especificação OpenAPI 3 (Swagger) para docs/swagger/openapi.json
        MvcResult openApiResult = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String openApiJson = openApiResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Path swaggerPath = Path.of("docs/swagger/openapi.json");
        Files.createDirectories(swaggerPath.getParent());
        Files.writeString(swaggerPath, objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(openApiJson)));

        assertThat(new File("docs/swagger/openapi.json")).exists();

        // 2. Verificar que categorias iniciais foram criadas com IDs gerados pela tabela JpaSequences
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        Number lngNextValCategoria = (Number) entityManager
                .createNativeQuery("SELECT lngNextVal FROM JpaSequences WHERE strSequenceName = 'seqCategoriaProblema'")
                .getSingleResult();
        assertThat(lngNextValCategoria.longValue()).isGreaterThanOrEqualTo(5L);

        // 3. Criar nova categoria
        CategoriaProblemaRequestDTO novaCat = new CategoriaProblemaRequestDTO(
                "Calçada Danificada",
                "Calçadas quebradas ou irregulares dificultando a mobilidade de pedestres."
        );
        MvcResult catResult = mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaCat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lngId").isNumber())
                .andExpect(jsonPath("$.strNome").value("Calçada Danificada"))
                .andReturn();

        Long lngCategoriaId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("lngId").asLong();

        // 4. Criar solicitação de zeladoria com imagem em tabela separada (ImagemSolicitacao)
        ImagemRequestDTO imagemDto = new ImagemRequestDTO(
                "buraco_rua_flores.png",
                "image/png",
                SAMPLE_BASE64_PNG
        );
        SolicitacaoRequestDTO solicitacaoDto = new SolicitacaoRequestDTO(
                "(11) 99887-6655",
                "morador@cidade.gov.br",
                -23.5505199,
                -46.6333094,
                "Rua das Flores, 123",
                lngCategoriaId,
                "Buraco grande no meio da via prejudicando o trânsito.",
                imagemDto
        );

        MvcResult solResult = mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitacaoDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lngId").isNumber())
                .andExpect(jsonPath("$.strStatus").value("ABERTA"))
                .andExpect(jsonPath("$.imagem.lngId").isNumber())
                .andExpect(jsonPath("$.imagem.strNomeArquivo").value("buraco_rua_flores.png"))
                .andReturn();

        Long lngSolicitacaoId = objectMapper.readTree(solResult.getResponse().getContentAsString()).get("lngId").asLong();
        Long lngImagemId = objectMapper.readTree(solResult.getResponse().getContentAsString()).get("imagem").get("lngId").asLong();

        // 5. Consultar imagem na tabela separada via endpoint dedicado
        mockMvc.perform(get("/api/v1/solicitacoes/{lngId}/imagem", lngSolicitacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lngId").value(lngImagemId))
                .andExpect(jsonPath("$.strDadosBase64").value(SAMPLE_BASE64_PNG));

        // 6. Atualizar status da solicitação
        AtualizarStatusRequestDTO statusDto = new AtualizarStatusRequestDTO(StatusSolicitacao.EM_ANDAMENTO);
        mockMvc.perform(patch("/api/v1/solicitacoes/{lngId}/status", lngSolicitacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strStatus").value("EM_ANDAMENTO"));

        // 7. Excluir logicamente a solicitação e comprovar que continua fisicamente no banco com blnExcluido = true
        mockMvc.perform(delete("/api/v1/solicitacoes/{lngId}", lngSolicitacaoId))
                .andExpect(status().isNoContent());

        // Consulta via API deve retornar 404 (filtrada por @SQLRestriction("blnExcluido = false"))
        mockMvc.perform(get("/api/v1/solicitacoes/{lngId}", lngSolicitacaoId))
                .andExpect(status().isNotFound());

        // Consulta física nativa no banco comprova que o registro NÃO foi apagado fisicamente
        Boolean blnSolExcluida = (Boolean) entityManager
                .createNativeQuery("SELECT blnExcluido FROM SolicitacaoZeladoria WHERE lngId = :lngId")
                .setParameter("lngId", lngSolicitacaoId)
                .getSingleResult();
        assertThat(blnSolExcluida).isTrue();

        Boolean blnImgExcluida = (Boolean) entityManager
                .createNativeQuery("SELECT blnExcluido FROM ImagemSolicitacao WHERE lngId = :lngId")
                .setParameter("lngId", lngImagemId)
                .getSingleResult();
        assertThat(blnImgExcluida).isTrue();
    }
}
