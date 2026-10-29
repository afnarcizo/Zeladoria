package br.com.zeladoria.repository;

import br.com.zeladoria.model.SolicitacaoZeladoria;
import br.com.zeladoria.model.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitacaoZeladoriaRepository extends JpaRepository<SolicitacaoZeladoria, Long> {

    @Query("SELECT s FROM SolicitacaoZeladoria s WHERE " +
           "(:strStatus IS NULL OR s.strStatus = :strStatus) AND " +
           "(:lngCategoriaId IS NULL OR s.categoria.lngId = :lngCategoriaId)")
    Page<SolicitacaoZeladoria> filtrarSolicitacoes(
            @Param("strStatus") StatusSolicitacao strStatus,
            @Param("lngCategoriaId") Long lngCategoriaId,
            Pageable pageable
    );
}
