package br.com.cafes.filter;

import br.com.cafes.config.JwtUtil;
import br.com.cafes.controller.BaseController;
import br.com.cafes.model.Usuario;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * FILTRO de seguranca JWT — roda ANTES dos controllers.
 *
 *  - Rotas publicas: qualquer GET, OPTIONS e /api/auth/* (login/cadastro).
 *  - Demais (POST, PUT, PATCH, DELETE): exige o cabecalho
 *        Authorization: Bearer <token>
 *    Se o token faltar ou for invalido/expirado, responde 401 e o
 *    controller nem chega a ser executado.
 *  - Se o token for valido, coloca o usuario na requisicao para o controller usar.
 */
public class JwtFilter extends HttpFilter {

    private static final String PREFIXO = "Bearer ";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        String rota = req.getRequestURI().substring(req.getContextPath().length());
        boolean publica = req.getMethod().equals("GET")
                || req.getMethod().equals("OPTIONS")
                || rota.startsWith("/api/auth/");

        if (publica) {
            chain.doFilter(req, resp);
            return;
        }

        String cabecalho = req.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO)) {
            this.negar(resp, "Token JWT obrigatorio. Envie o cabecalho 'Authorization: Bearer <token>'.");
            return;
        }

        try {
            DecodedJWT jwt = JwtUtil.validar(cabecalho.substring(PREFIXO.length()).trim());
            Usuario usuario = new Usuario();
            usuario.setId(Long.valueOf(jwt.getSubject()));
            usuario.setNome(jwt.getClaim("nome").asString());
            req.setAttribute(BaseController.ATRIBUTO_USUARIO, usuario);
        } catch (JWTVerificationException | NumberFormatException e) {
            this.negar(resp, "Token JWT invalido ou expirado.");
            return;
        }

        chain.doFilter(req, resp);
    }

    private void negar(HttpServletResponse resp, String mensagem) throws IOException {
        resp.setStatus(401);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("WWW-Authenticate", "Bearer");
        resp.getWriter().write("{\"status\": 401, \"erro\": \"" + mensagem.replace("\"", "'") + "\"}");
    }
}
