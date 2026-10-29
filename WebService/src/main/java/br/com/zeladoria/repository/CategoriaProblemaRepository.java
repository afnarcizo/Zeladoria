package br.com.zeladoria.repository;

import br.com.zeladoria.model.CategoriaProblema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaProblemaRepository extends JpaRepository<CategoriaProblema, Long> {
    Optional<CategoriaProblema> findByStrNomeIgnoreCase(String strNome);
    boolean existsByStrNomeIgnoreCase(String strNome);
}
