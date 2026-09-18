package dev.gustavo.produtoservice.controller;

import dev.gustavo.produtoservice.dto.ProdutoMapper;
import dev.gustavo.produtoservice.dto.ProdutoRequest;
import dev.gustavo.produtoservice.dto.ProdutoResponse;
import dev.gustavo.produtoservice.model.Produto;
import dev.gustavo.produtoservice.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    public ProdutoController(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAll()
                .stream()
                .map(produtoMapper::toDTO)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@RequestBody ProdutoRequest request) {
        Produto produto = new Produto(request.nome(), request.preco(), request.quantidade());
        Produto salvo = produtoRepository.save(produto);

        return ResponseEntity.status(HttpStatus.CREATED).body(produtoMapper.toDTO(salvo));
    }
}
