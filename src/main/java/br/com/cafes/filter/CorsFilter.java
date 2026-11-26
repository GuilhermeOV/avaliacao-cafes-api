package br.com.cafes.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * FILTRO de CORS — permite que um front-end em outro endereco (ou o Swagger
 * Editor no navegador) consuma a API. Responde direto as requisicoes OPTIONS
 * ("preflight") que o navegador envia antes de um POST/PUT/PATCH/DELETE.
 */
public class CorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Expose-Headers", "Location");

        if ("OPTIONS".equals(req.getMethod())) {
            resp.setStatus(204);
            return;
        }
        chain.doFilter(req, resp);
    }
}
