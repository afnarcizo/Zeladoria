package br.com.zeladoria.repository;

import br.com.zeladoria.model.ImagemSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagemSolicitacaoRepository extends JpaRepository<ImagemSolicitacao, Long> {
}
