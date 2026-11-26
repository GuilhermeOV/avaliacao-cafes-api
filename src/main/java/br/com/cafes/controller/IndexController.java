package br.com.cafes.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** GET /api — "pagina inicial" da API, lista os endpoints disponiveis. */
@WebServlet("/api")
public class IndexController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("api", "Plataforma de Avaliacao de Cafes Locais");
        info.put("versao", "1.0");
        info.put("endpoints", List.of(
                "POST   /api/auth/registrar",
                "POST   /api/auth/login",
                "GET    /api/cafes?bairro=&tipo=&ordenar=nome|media",
                "GET    /api/cafes/{id}",
                "GET    /api/cafes/{id}/reviews",
                "POST   /api/cafes              (JWT)",
                "PUT    /api/cafes/{id}         (JWT)",
                "PATCH  /api/cafes/{id}         (JWT)",
                "DELETE /api/cafes/{id}         (JWT)",
                "GET    /api/reviews?cafeId=",
                "GET    /api/reviews/{id}",
                "POST   /api/reviews            (JWT)",
                "PUT    /api/reviews/{id}       (JWT, autor)",
                "PATCH  /api/reviews/{id}       (JWT, autor)",
                "DELETE /api/reviews/{id}       (JWT, autor)"));
        this.responder(resp, 200, info);
    }
}
