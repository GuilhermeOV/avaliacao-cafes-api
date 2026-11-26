package br.com.cafes.controller;

import br.com.cafes.model.Cafe;
import br.com.cafes.service.CafeService;
import br.com.cafes.service.ReviewService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * CONTROLLER de Cafes. O curinga "/*" captura /api/cafes, /api/cafes/10 e /api/cafes/10/reviews.
 *
 *  GET    /api/cafes                  lista (filtros: ?bairro=  ?tipo=  ?ordenar=nome|media)
 *  GET    /api/cafes/{id}             detalhe com medias
 *  GET    /api/cafes/{id}/reviews     reviews da cafeteria
 *  POST   /api/cafes                  cria                 (JWT)
 *  PUT    /api/cafes/{id}             substitui tudo       (JWT)
 *  PATCH  /api/cafes/{id}             altera parcialmente  (JWT)
 *  DELETE /api/cafes/{id}             exclui               (JWT)
 */
@WebServlet("/api/cafes/*")
public class CafeController extends BaseController {

    private final CafeService cafeService = new CafeService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        if (caminho.length == 0) {
            this.responder(resp, 200, this.cafeService.listar(
                    req.getParameter("bairro"), req.getParameter("tipo"), req.getParameter("ordenar")));
        } else if (caminho.length == 1) {
            this.responder(resp, 200, this.cafeService.buscarPorId(this.id(caminho[0])));
        } else if (caminho.length == 2 && caminho[1].equals("reviews")) {
            this.responder(resp, 200, this.reviewService.listar(this.id(caminho[0])));
        } else {
            throw rotaNaoEncontrada();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        if (caminho.length != 0) {
            throw rotaNaoEncontrada();
        }
        this.usuarioLogado(req);
        Cafe novo = this.cafeService.criar(this.lerJson(req, Cafe.class));
        this.criado(req, resp, novo, novo.getId());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        this.usuarioLogado(req);
        this.responder(resp, 200, this.cafeService.atualizar(this.idUnico(caminho), this.lerJson(req, Cafe.class)));
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        this.usuarioLogado(req);
        this.responder(resp, 200, this.cafeService.atualizarParcial(this.idUnico(caminho), this.lerJson(req, Cafe.class)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp, String[] caminho) {
        this.usuarioLogado(req);
        this.cafeService.deletar(this.idUnico(caminho));
        this.semConteudo(resp);
    }

    private Long idUnico(String[] caminho) {
        if (caminho.length == 0) {
            throw metodoNaoPermitido();
        }
        if (caminho.length != 1) {
            throw rotaNaoEncontrada();
        }
        return this.id(caminho[0]);
    }
}
