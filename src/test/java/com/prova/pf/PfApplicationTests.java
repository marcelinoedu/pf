package com.prova.pf;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PfApplicationTests {
    private static final String VALIDA = """
        {"emailAvaliado":"pessoa@empresa.com","comentarios":"Ótima colaboração","nota":10}
        """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    JwtDecoder decoder;

    @Test
    void exigeAutenticacaoNaListagemENoCadastro() throws Exception {
        mvc.perform(get("/avaliacoes")).andExpect(status().isUnauthorized());
        mvc.perform(post("/avaliacoes").contentType(MediaType.APPLICATION_JSON).content(VALIDA))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioPodeListarMasNaoCadastrar() throws Exception {
        token("usuario", "USER", "usuario@empresa.com");
        mvc.perform(get("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer usuario"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.itens").isArray());
        mvc.perform(post("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer usuario")
            .contentType(MediaType.APPLICATION_JSON).content(VALIDA))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCadastraEAListagemRetornaAvaliacaoPersistida() throws Exception {
        token("admin", "ADMIN", "admin@empresa.com");
        mvc.perform(post("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer admin")
            .contentType(MediaType.APPLICATION_JSON).content(VALIDA))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.emailAvaliador").value("admin@empresa.com"))
            .andExpect(jsonPath("$.dataAvaliacao").isString());

        mvc.perform(get("/avaliacoes?tamanho=1").header(HttpHeaders.AUTHORIZATION, "Bearer admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itens[0].emailAvaliado").value("pessoa@empresa.com"))
            .andExpect(jsonPath("$.itens[0].comentarios").value("Ótima colaboração"))
            .andExpect(jsonPath("$.itens[0].nota").value(10))
            .andExpect(jsonPath("$.tamanho").value(1))
            .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void rejeitaNotaInvalidaEEmailDoAvaliadorNoCorpo() throws Exception {
        token("admin", "ADMIN", "admin@empresa.com");
        mvc.perform(post("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer admin")
            .contentType(MediaType.APPLICATION_JSON).content(VALIDA.replace("10", "11")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nota").isString());
        mvc.perform(post("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer admin")
            .contentType(MediaType.APPLICATION_JSON).content(VALIDA.replace("10", "9.5")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer admin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALIDA.replace("{", "{\"emailAvaliador\":\"outra@empresa.com\",")))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/avaliacoes?tamanho=101").header(HttpHeaders.AUTHORIZATION, "Bearer admin"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void bloqueiaPerfilDesconhecidoEAdminSemEmailVerificado() throws Exception {
        token("outro", "OUTRO", null);
        token("sem-email", "ADMIN", null);
        mvc.perform(get("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer outro"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/avaliacoes").header(HttpHeaders.AUTHORIZATION, "Bearer sem-email")
            .contentType(MediaType.APPLICATION_JSON).content(VALIDA))
            .andExpect(status().isForbidden());
    }

    private void token(String token, String role, String email) {
        var jwt = Jwt.withTokenValue(token).header("alg", "RS256").subject(token)
            .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300))
            .claim("https://avaliacoes.example.com/roles", List.of(role));
        if (email != null) {
            jwt.claim("https://avaliacoes.example.com/email", email);
        }
        when(decoder.decode(token)).thenReturn(jwt.build());
    }
}
