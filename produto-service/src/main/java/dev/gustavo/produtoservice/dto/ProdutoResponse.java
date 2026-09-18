package dev.gustavo.produtoservice.dto;

public record ProdutoResponse(Long id, String nome, Double preco, Integer quantidade) {
}
