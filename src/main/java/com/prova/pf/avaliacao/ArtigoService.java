package com.prova.pf.avaliacao;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class ArtigoService {
    private final ArtigoRepository repository;

    ArtigoService(ArtigoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    PaginaArtigos listar(int pagina, int tamanho) {
        var resultado = repository.findAll(PageRequest.of(pagina, tamanho, Sort.by("id").descending()));
        return new PaginaArtigos(
                resultado.map(Artigo::resposta).getContent(), pagina, tamanho,
                resultado.getTotalElements(), resultado.getTotalPages()
        );
    }


    @Transactional
    ArtigoResposta cadastrar(NovoArtigo entrada) {
        return repository.save(new Artigo(entrada)).resposta();
    }

    @Transactional
    void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artigo não encontrado.");
        }
        repository.deleteById(id);
    }

    record PaginaArtigos(
            List<ArtigoResposta> itens, int pagina, int tamanho, long total, int totalPaginas
    ) {}

}
