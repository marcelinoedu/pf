package com.prova.pf.avaliacao;

import com.prova.pf.security.SecurityConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/artigos")
class ArtigoController {
    private final ArtigoService service;

    ArtigoController(ArtigoService service) {
        this.service = service;
    }

    @GetMapping
    ArtigoService.PaginaArtigos listar(
        @RequestParam(defaultValue = "0") @Min(0) @Max(1_000_000) int pagina,
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int tamanho
    ) {
        return service.listar(pagina, tamanho);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ArtigoResposta cadastrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NovoArtigo entrada) {
        Object emailClaim = jwt.getClaims().get(SecurityConfig.EMAIL_CLAIM);
        if (!(emailClaim instanceof String email) || email.isBlank() || email.length() > 254) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verifique seu e-mail no Auth0 para cadastrar avaliações.");
        }
        return service.cadastrar(entrada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        service.deletar(id);
    }

}
