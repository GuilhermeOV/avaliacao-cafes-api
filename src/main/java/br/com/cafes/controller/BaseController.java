package br.com.cafes.controller;

import br.com.cafes.exception.ApiException;
import br.com.cafes.model.Usuario;
import br.com.cafes.util.JsonUtil;
import com.google.gson.JsonParseException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Classe BASE de todos os controllers REST.
 *
 * Responsabilidades (somente "infraestrutura HTTP", nada de regra de negocio):
 *  - rotear o metodo HTTP, incluindo PATCH (que o HttpServlet nao tem nativamente);
 *  - ler o corpo JSON e escrever respostas JSON com o status correto;
 *  - extrair o id da URL (ex.: /api/cafes/10 -> 10);
 *  - transformar excecoes em respostas de erro padronizadas: {"status": 404, "erro": "..."}.
 */
public abstract class BaseController extends HttpServlet {

    public static final String ATRIBUTO_USUARIO = "usuarioLogado";

    /**
     * Ponto unico de entrada: o HttpServlet nao possui doPatch(), entao
     * sobrescrevemos service() e fazemos o roteamento manualmente.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        try {
            String[] caminho = this.segmentos(req);
            switch (req.getMethod()) {
                case "GET" -> this.doGet(req, resp, caminho);
                case "POST" -> this.doPost(req, resp, caminho);
                case "PUT" -> this.doPut(req, resp, caminho);
                case "PATCH" -> this.doPatch(req, resp, caminho);
                case "DELETE" -> this.doDelete(req, resp, caminho);
                default -> throw metodoNaoPermitido();
            }
        } catch (ApiException e) {
            this.erro(resp, e.getStatus(), e.getMessage());
        } catch (JsonParseException e) {
            this.erro(resp, 400, "JSON invalido no corpo da requisicao (verifique a sintaxe e se o texto esta em UTF-8).");
        } catch (Exception e) {
            this.log("Erro inesperado em " + req.getMethod() + " " + req.getRequestURI(), e);
            this.erro(resp, 500, "Erro interno no servidor.");
        }
    }

    // Os controllers filhos sobrescrevem apenas os metodos que suportam.
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        throw metodoNaoPermitido();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        throw metodoNaoPermitido();
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        throw metodoNaoPermitido();
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        throw metodoNaoPermitido();
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        throw metodoNaoPermitido();
    }

    // ------------------------------------------------------------------ ajudantes

    /** "/10/reviews" -> ["10", "reviews"]; sem pathInfo -> []. */
    private String[] segmentos(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) {
            return new String[0];
        }
        return Arrays.stream(info.split("/")).filter(s -> !s.isBlank()).toArray(String[]::new);
    }

    /** Converte o segmento da URL em id numerico (400 se nao for numero). */
    protected Long id(String valor) {
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw ApiException.requisicaoInvalida("Id invalido na URL: '" + valor + "'.");
        }
    }

    /** Le um parametro numerico opcional da query string (ex.: ?cafeId=2). */
    protected Long paramLong(HttpServletRequest req, String nome) {
        String valor = req.getParameter(nome);
        return (valor == null || valor.isBlank()) ? null : this.id(valor);
    }

    /** Le o corpo JSON e converte para a classe informada. */
    protected <T> T lerJson(HttpServletRequest req, Class<T> classe) throws IOException {
        return JsonUtil.GSON.fromJson(req.getReader(), classe);
    }

    /** Escreve o objeto como JSON com o status informado. */
    protected void responder(HttpServletResponse resp, int status, Object corpo) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(JsonUtil.GSON.toJson(corpo));
    }

    /** 201 Created + cabecalho Location apontando para o novo recurso. */
    protected void criado(HttpServletRequest req, HttpServletResponse resp, Object corpo, Long id) throws IOException {
        resp.setHeader("Location", req.getRequestURL().append("/").append(id).toString());
        this.responder(resp, 201, corpo);
    }

    /** 204 No Content (usado apos DELETE). */
    protected void semConteudo(HttpServletResponse resp) {
        resp.setStatus(204);
    }

    /** Usuario colocado na requisicao pelo JwtFilter. */
    protected Usuario usuarioLogado(HttpServletRequest req) {
        Usuario usuario = (Usuario) req.getAttribute(ATRIBUTO_USUARIO);
        if (usuario == null) {
            throw ApiException.naoAutorizado("Token JWT obrigatorio.");
        }
        return usuario;
    }

    private void erro(HttpServletResponse resp, int status, String mensagem) throws IOException {
        this.responder(resp, status, Map.of("status", status, "erro", mensagem));
    }

    protected static ApiException rotaNaoEncontrada() {
        return ApiException.naoEncontrado("Rota nao encontrada.");
    }

    protected static ApiException metodoNaoPermitido() {
        return new ApiException(405, "Metodo HTTP nao permitido para esta rota.");
    }
}
