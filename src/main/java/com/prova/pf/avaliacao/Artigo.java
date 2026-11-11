package com.prova.pf.avaliacao;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "artigos")
class Artigo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String emailCriador;

    @Column(nullable = false, length = 2000)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;


    protected Artigo() {}

    Artigo(NovoArtigo entrada) {
        this.emailCriador = entrada.emailCriador();
        this.titulo = entrada.title();
        this.descricao = entrada.descricao();
    }

    ArtigoResposta resposta() {
        return new ArtigoResposta(id, emailCriador, titulo, descricao);
    }
}
