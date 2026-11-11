package com.prova.pf.avaliacao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record NovoArtigo(
    @NotBlank @Email @Size(max = 254) String emailCriador,
    @NotBlank @Size(max = 2000) String title,
    @NotBlank String descricao
) {}
