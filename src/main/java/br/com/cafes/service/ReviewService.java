package br.com.cafes.service;

import br.com.cafes.dao.CafeDAO;
import br.com.cafes.dao.ReviewDAO;
import br.com.cafes.exception.ApiException;
import br.com.cafes.model.Review;
import br.com.cafes.model.Usuario;

import java.util.List;

/**
 * SERVICE de Review — regras de negocio das avaliacoes.
 *
 * Regras:
 *  - o cafe avaliado precisa existir;
 *  - as tres notas (bebida, ambiente, atendimento) vao de 1 a 5;
 *  - cada usuario so pode avaliar o mesmo cafe UMA vez;
 *  - somente o autor da review pode altera-la ou exclui-la.
 */
public class ReviewService {

    private static final int NOTA_MINIMA = 1;
    private static final int NOTA_MAXIMA = 5;

    private final ReviewDAO reviewDAO;
    private final CafeDAO cafeDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.cafeDAO = new CafeDAO();
    }

    /** Lista todas as reviews, ou somente as de um cafe (cafeId != null). */
    public List<Review> listar(Long cafeId) {
        if (cafeId != null) {
            this.garantirCafeExiste(cafeId);
        }
        return this.reviewDAO.listar(cafeId);
    }

    public Review buscarPorId(Long id) {
        Review review = this.reviewDAO.buscarPorId(id);
        if (review == null) {
            throw ApiException.naoEncontrado("Review " + id + " nao encontrada.");
        }
        return review;
    }

    /** POST — o autor vem do token (usuario logado), nao do JSON. */
    public Review criar(Review review, Usuario logado) {
        if (review == null) {
            throw ApiException.requisicaoInvalida("Corpo da requisicao e obrigatorio.");
        }
        if (review.getCafeId() == null) {
            throw ApiException.requisicaoInvalida("Campo 'cafeId' e obrigatorio.");
        }
        this.garantirCafeExiste(review.getCafeId());

        boolean jaAvaliou = this.reviewDAO.listar(review.getCafeId()).stream()
                .anyMatch(r -> r.getUsuarioId().equals(logado.getId()));
        if (jaAvaliou) {
            throw ApiException.conflito("Voce ja avaliou este cafe. Use PUT/PATCH para alterar sua review.");
        }

        review.setUsuarioId(logado.getId());
        review.setAutor(logado.getNome());
        review.setComentario(Texto.limpar(review.getComentario()));
        this.validarNotas(review);

        long id = this.reviewDAO.inserir(review);
        return this.buscarPorId(id);
    }

    /** PUT — substitui as tres notas e o comentario. */
    public Review atualizar(Long id, Review dados, Usuario logado) {
        Review atual = this.buscarDoAutor(id, logado);
        if (dados == null) {
            throw ApiException.requisicaoInvalida("Corpo da requisicao e obrigatorio.");
        }
        atual.setNotaBebida(dados.getNotaBebida());
        atual.setNotaAmbiente(dados.getNotaAmbiente());
        atual.setNotaAtendimento(dados.getNotaAtendimento());
        atual.setComentario(Texto.limpar(dados.getComentario()));
        this.validarNotas(atual);
        this.reviewDAO.alterar(atual);
        return this.buscarPorId(id);
    }

    /** PATCH — altera somente o que foi enviado. */
    public Review atualizarParcial(Long id, Review parcial, Usuario logado) {
        Review atual = this.buscarDoAutor(id, logado);
        if (parcial == null) {
            throw ApiException.requisicaoInvalida("Corpo da requisicao e obrigatorio.");
        }
        if (parcial.getNotaBebida() != null) atual.setNotaBebida(parcial.getNotaBebida());
        if (parcial.getNotaAmbiente() != null) atual.setNotaAmbiente(parcial.getNotaAmbiente());
        if (parcial.getNotaAtendimento() != null) atual.setNotaAtendimento(parcial.getNotaAtendimento());
        if (parcial.getComentario() != null) atual.setComentario(Texto.limpar(parcial.getComentario()));
        this.validarNotas(atual);
        this.reviewDAO.alterar(atual);
        return this.buscarPorId(id);
    }

    public void deletar(Long id, Usuario logado) {
        this.buscarDoAutor(id, logado);
        this.reviewDAO.deletar(id);
    }

    /** Busca a review e garante que o usuario logado e o dono dela (senao 403). */
    private Review buscarDoAutor(Long id, Usuario logado) {
        Review review = this.buscarPorId(id);
        if (!review.getUsuarioId().equals(logado.getId())) {
            throw ApiException.proibido("Somente o autor pode alterar ou excluir esta review.");
        }
        return review;
    }

    private void garantirCafeExiste(Long cafeId) {
        if (this.cafeDAO.buscarPorId(cafeId) == null) {
            throw ApiException.naoEncontrado("Cafe " + cafeId + " nao encontrado.");
        }
    }

    private void validarNotas(Review review) {
        this.validarNota(review.getNotaBebida(), "notaBebida");
        this.validarNota(review.getNotaAmbiente(), "notaAmbiente");
        this.validarNota(review.getNotaAtendimento(), "notaAtendimento");
        Texto.maximo(review.getComentario(), "comentario", 1000);
    }

    private void validarNota(Integer nota, String campo) {
        if (nota == null) {
            throw ApiException.requisicaoInvalida("Campo '" + campo + "' e obrigatorio.");
        }
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw ApiException.requisicaoInvalida("Campo '" + campo + "' deve ser de " + NOTA_MINIMA + " a " + NOTA_MAXIMA + ".");
        }
    }
}
