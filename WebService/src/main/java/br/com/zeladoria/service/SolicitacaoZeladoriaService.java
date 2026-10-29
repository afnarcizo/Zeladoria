package br.com.zeladoria.service;

import br.com.zeladoria.model.CategoriaProblema;
import br.com.zeladoria.model.ImagemSolicitacao;
import br.com.zeladoria.model.SolicitacaoZeladoria;
import br.com.zeladoria.model.StatusSolicitacao;
import br.com.zeladoria.dto.*;
import br.com.zeladoria.exception.RecursoNaoEncontradoException;
import br.com.zeladoria.exception.RegraNegocioException;
import br.com.zeladoria.repository.ImagemSolicitacaoRepository;
import br.com.zeladoria.repository.SolicitacaoZeladoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SolicitacaoZeladoriaService {

    private final SolicitacaoZeladoriaRepository solicitacaoRepository;
    private final CategoriaProblemaService categoriaService;
    private final ImagemSolicitacaoRepository imagemRepository;

    public SolicitacaoZeladoriaService(
            SolicitacaoZeladoriaRepository solicitacaoRepository,
            CategoriaProblemaService categoriaService,
            ImagemSolicitacaoRepository imagemRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaService = categoriaService;
        this.imagemRepository = imagemRepository;
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> listar(StatusSolicitacao strStatus, Long lngCategoriaId, Pageable pageable) {
        return solicitacaoRepository.filtrarSolicitacoes(strStatus, lngCategoriaId, pageable)
                .map(SolicitacaoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(Long lngId) {
        SolicitacaoZeladoria solicitacao = buscarEntidadePorId(lngId);
        return SolicitacaoResponseDTO.fromEntity(solicitacao);
    }

    @Transactional(readOnly = true)
    public ImagemResponseDTO buscarImagemDaSolicitacao(Long lngSolicitacaoId) {
        SolicitacaoZeladoria solicitacao = buscarEntidadePorId(lngSolicitacaoId);
        ImagemSolicitacao imagem = solicitacao.getImagem();
        if (imagem == null) {
            throw new RecursoNaoEncontradoException("Nenhuma imagem encontrada para a solicitação ID: " + lngSolicitacaoId);
        }
        return ImagemResponseDTO.fromEntityCompleto(imagem);
    }

    @Transactional
    public SolicitacaoResponseDTO criar(SolicitacaoRequestDTO dto) {
        CategoriaProblema categoria = categoriaService.buscarEntidadePorId(dto.lngCategoriaId());

        ImagemRequestDTO imgDto = dto.imagem();
        long lngTamanhoCalculado = calcularTamanhoBase64(imgDto.strDadosBase64());

        ImagemSolicitacao imagem = new ImagemSolicitacao(
                imgDto.strNomeArquivo(),
                imgDto.strTipoConteudo(),
                lngTamanhoCalculado,
                imgDto.strDadosBase64()
        );

        SolicitacaoZeladoria solicitacao = new SolicitacaoZeladoria();
        solicitacao.setStrTelefoneUsuario(dto.strTelefoneUsuario());
        solicitacao.setStrEmailUsuario(dto.strEmailUsuario());
        solicitacao.setDblLatitude(dto.dblLatitude());
        solicitacao.setDblLongitude(dto.dblLongitude());
        solicitacao.setStrEnderecoReferencia(dto.strEnderecoReferencia());
        solicitacao.setCategoria(categoria);
        solicitacao.setImagem(imagem);
        solicitacao.setStrDescricao(dto.strDescricao());
        solicitacao.setStrStatus(StatusSolicitacao.ABERTA);

        SolicitacaoZeladoria salva = solicitacaoRepository.save(solicitacao);
        return SolicitacaoResponseDTO.fromEntity(salva);
    }

    @Transactional
    public SolicitacaoResponseDTO atualizarStatus(Long lngId, AtualizarStatusRequestDTO dto) {
        SolicitacaoZeladoria solicitacao = buscarEntidadePorId(lngId);
        if (solicitacao.getStrStatus() == StatusSolicitacao.CONCLUIDA || solicitacao.getStrStatus() == StatusSolicitacao.CANCELADA) {
            throw new RegraNegocioException("Não é permitido alterar o status de uma solicitação já finalizada (" + solicitacao.getStrStatus() + ").");
        }
        solicitacao.setStrStatus(dto.strStatus());
        SolicitacaoZeladoria atualizada = solicitacaoRepository.save(solicitacao);
        return SolicitacaoResponseDTO.fromEntity(atualizada);
    }

    @Transactional
    public void excluirLogicamente(Long lngId) {
        SolicitacaoZeladoria solicitacao = buscarEntidadePorId(lngId);
        if (solicitacao.getImagem() != null) {
            // Exclui logicamente a imagem vinculada na tabela separada via @SQLDelete
            imagemRepository.delete(solicitacao.getImagem());
        }
        // Exclui logicamente a solicitação via @SQLDelete
        solicitacaoRepository.delete(solicitacao);
    }

    private SolicitacaoZeladoria buscarEntidadePorId(Long lngId) {
        return solicitacaoRepository.findById(lngId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação de zeladoria não encontrada com o ID: " + lngId));
    }

    private long calcularTamanhoBase64(String strBase64) {
        try {
            String strLimpo = strBase64.contains(",") ? strBase64.substring(strBase64.indexOf(",") + 1) : strBase64;
            byte[] decoded = Base64.getDecoder().decode(strLimpo.trim().getBytes(StandardCharsets.UTF_8));
            return decoded.length;
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("O conteúdo da imagem enviado não é uma string Base64 válida.");
        }
    }
}
