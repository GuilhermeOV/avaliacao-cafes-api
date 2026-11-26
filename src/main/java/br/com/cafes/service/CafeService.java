package br.com.cafes.service;

import br.com.cafes.dao.CafeDAO;
import br.com.cafes.dao.ReviewDAO;
import br.com.cafes.exception.ApiException;
import br.com.cafes.model.Cafe;

import java.util.List;

/**
 * SERVICE de Cafe — regras de negocio e validacoes.
 *
 * O Controller nunca fala com o DAO diretamente: ele chama este Service,
 * que valida os dados e decide o que pode ou nao ser feito.
 */
public class CafeService {

    private final CafeDAO cafeDAO;
    private final ReviewDAO reviewDAO;

    public CafeService() {
        this.cafeDAO = new CafeDAO();
        this.reviewDAO = new ReviewDAO();
    }

    /** Lista com filtros opcionais por bairro e tipo de cafe. ordenar: "nome" (padrao) ou "media". */
    public List<Cafe> listar(String bairro, String tipo, String ordenar) {
        String ordem = Texto.limpar(ordenar);
        if (ordem != null && !ordem.equals("nome") && !ordem.equals("media")) {
            throw ApiException.requisicaoInvalida("Parametro 'ordenar' deve ser 'nome' ou 'media'.");
        }
        return this.cafeDAO.listar(Texto.limpar(bairro), Texto.minusculo(tipo), "media".equals(ordem));
    }

    public Cafe buscarPorId(Long id) {
        Cafe cafe = this.cafeDAO.buscarPorId(id);
        if (cafe == null) {
            throw ApiException.naoEncontrado("Cafe " + id + " nao encontrado.");
        }
        return cafe;
    }

    /** POST — cria e devolve o cafe ja com o id gerado. */
    public Cafe criar(Cafe cafe) {
        if (cafe == null) {
            throw ApiException.requisicaoInvalida("Corpo da requisicao e obrigatorio.");
        }
        this.normalizar(cafe);
        this.validar(cafe);
        long id = this.cafeDAO.inserir(cafe);
        return this.buscarPorId(id);
    }

    /** PUT — substitui TODOS os campos (todos sao obrigatorios). */
    public Cafe atualizar(Long id, Cafe dados) {
        this.buscarPorId(id);
        if (dados == null) {
            throw ApiException.requisicaoInvalida("Corpo da requisicao e obrigatorio.");
        }
        dados.setId(id);
        this.normalizar(dados);
        this.validar(dados);
        this.cafeDAO.alterar(dados);
        return this.buscarPorId(id);
    }

    /** PATCH — altera somente os campos enviados no JSON. */
    public Cafe atualizarParcial(Long id, Cafe parcial) {
        Cafe atual = this.buscarPorId(id);
        if (parcial == null) {
            throw ApiException.requisicaoInvalida("Corpo da requisicao e obrigatorio.");
        }
        if (parcial.getNome() != null) atual.setNome(parcial.getNome());
        if (parcial.getEndereco() != null) atual.setEndereco(parcial.getEndereco());
        if (parcial.getBairro() != null) atual.setBairro(parcial.getBairro());
        if (parcial.getTipoCafePrincipal() != null) atual.setTipoCafePrincipal(parcial.getTipoCafePrincipal());

        this.normalizar(atual);
        this.validar(atual);
        this.cafeDAO.alterar(atual);
        return this.buscarPorId(id);
    }

    /** DELETE — regra: nao pode excluir cafeteria que ja possui avaliacoes. */
    public void deletar(Long id) {
        this.buscarPorId(id);
        int reviews = this.reviewDAO.contarPorCafe(id);
        if (reviews > 0) {
            throw ApiException.conflito("Nao e possivel excluir: o cafe possui " + reviews
                    + " avaliacao(oes). Exclua as avaliacoes antes.");
        }
        this.cafeDAO.deletar(id);
    }

    private void normalizar(Cafe cafe) {
        cafe.setNome(Texto.limpar(cafe.getNome()));
        cafe.setEndereco(Texto.limpar(cafe.getEndereco()));
        cafe.setBairro(Texto.limpar(cafe.getBairro()));
        cafe.setTipoCafePrincipal(Texto.minusculo(cafe.getTipoCafePrincipal()));
    }

    private void validar(Cafe cafe) {
        Texto.obrigatorio(cafe.getNome(), "nome", 100);
        Texto.obrigatorio(cafe.getEndereco(), "endereco", 255);
        Texto.obrigatorio(cafe.getBairro(), "bairro", 100);
        Texto.obrigatorio(cafe.getTipoCafePrincipal(), "tipoCafePrincipal", 50);
    }
}
