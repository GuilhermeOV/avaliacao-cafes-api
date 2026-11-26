package br.com.cafes.controller;

import br.com.cafes.model.Review;
import br.com.cafes.service.ReviewService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * CONTROLLER de Reviews.
 *
 *  GET    /api/reviews             lista (filtro opcional: ?cafeId=)
 *  GET    /api/reviews/{id}        detalhe
 *  POST   /api/reviews             cria (autor = usuario do token)   (JWT)
 *  PUT    /api/reviews/{id}        substitui notas e comentario      (JWT, so o autor)
 *  PATCH  /api/reviews/{id}        altera parcialmente               (JWT, so o autor)
 *  DELETE /api/reviews/{id}        exclui                            (JWT, so o autor)
 */
@WebServlet("/api/reviews/*")
public class ReviewController extends BaseController {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        if (caminho.length == 0) {
            this.responder(resp, 200, this.reviewService.listar(this.paramLong(req, "cafeId")));
        } else {
            this.responder(resp, 200, this.reviewService.buscarPorId(this.idUnico(caminho)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        if (caminho.length != 0) {
            throw rotaNaoEncontrada();
        }
        Review nova = this.reviewService.criar(this.lerJson(req, Review.class), this.usuarioLogado(req));
        this.criado(req, resp, nova, nova.getId());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        this.responder(resp, 200, this.reviewService.atualizar(
                this.idUnico(caminho), this.lerJson(req, Review.class), this.usuarioLogado(req)));
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        this.responder(resp, 200, this.reviewService.atualizarParcial(
                this.idUnico(caminho), this.lerJson(req, Review.class), this.usuarioLogado(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp, String[] caminho) {
        this.reviewService.deletar(this.idUnico(caminho), this.usuarioLogado(req));
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
