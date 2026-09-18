package dev.gustavo.produtoservice.repository;

import dev.gustavo.produtoservice.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
