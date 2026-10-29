package br.com.zeladoria.service;

import br.com.zeladoria.model.CategoriaProblema;
import br.com.zeladoria.dto.CategoriaProblemaRequestDTO;
import br.com.zeladoria.dto.CategoriaProblemaResponseDTO;
import br.com.zeladoria.exception.RecursoNaoEncontradoException;
import br.com.zeladoria.exception.RegraNegocioException;
import br.com.zeladoria.repository.CategoriaProblemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaProblemaService {

    private final CategoriaProblemaRepository categoriaRepository;

    public CategoriaProblemaService(CategoriaProblemaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaProblemaResponseDTO> listarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaProblemaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaProblemaResponseDTO buscarPorId(Long lngId) {
        CategoriaProblema categoria = buscarEntidadePorId(lngId);
        return CategoriaProblemaResponseDTO.fromEntity(categoria);
    }

    @Transactional(readOnly = true)
    public CategoriaProblema buscarEntidadePorId(Long lngId) {
        return categoriaRepository.findById(lngId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria de problema não encontrada com o ID: " + lngId));
    }

    @Transactional
    public CategoriaProblemaResponseDTO criar(CategoriaProblemaRequestDTO dto) {
        if (categoriaRepository.existsByStrNomeIgnoreCase(dto.strNome())) {
            throw new RegraNegocioException("Já existe uma categoria ativa cadastrada com o nome: " + dto.strNome());
        }
        CategoriaProblema nova = new CategoriaProblema(dto.strNome(), dto.strDescricao());
        CategoriaProblema salva = categoriaRepository.save(nova);
        return CategoriaProblemaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public CategoriaProblemaResponseDTO atualizar(Long lngId, CategoriaProblemaRequestDTO dto) {
        CategoriaProblema existente = buscarEntidadePorId(lngId);
        categoriaRepository.findByStrNomeIgnoreCase(dto.strNome())
                .filter(c -> !c.getLngId().equals(lngId))
                .ifPresent(c -> {
                    throw new RegraNegocioException("Já existe outra categoria ativa com o nome: " + dto.strNome());
                });

        existente.setStrNome(dto.strNome());
        existente.setStrDescricao(dto.strDescricao());
        CategoriaProblema atualizada = categoriaRepository.save(existente);
        return CategoriaProblemaResponseDTO.fromEntity(atualizada);
    }

    @Transactional
    public void excluirLogicamente(Long lngId) {
        CategoriaProblema existente = buscarEntidadePorId(lngId);
        // O @SQLDelete na entidade intercepta o delete e realiza o UPDATE lógico (blnExcluido = true, dtmExclusao = CURRENT_TIMESTAMP)
        categoriaRepository.delete(existente);
    }
}
