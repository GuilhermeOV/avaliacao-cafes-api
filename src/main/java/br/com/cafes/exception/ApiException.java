package br.com.cafes.exception;

/**
 * Excecao de negocio que ja sabe qual STATUS HTTP deve ser devolvido.
 *
 * O Service lanca, por exemplo, ApiException.naoEncontrado("Cafe nao encontrado.")
 * e o BaseController transforma isso em uma resposta JSON com o status certo.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    /** 400 - dados invalidos enviados pelo cliente. */
    public static ApiException requisicaoInvalida(String mensagem) {
        return new ApiException(400, mensagem);
    }

    /** 401 - nao autenticado (token ausente/invalido ou login errado). */
    public static ApiException naoAutorizado(String mensagem) {
        return new ApiException(401, mensagem);
    }

    /** 403 - autenticado, mas sem permissao para esta acao. */
    public static ApiException proibido(String mensagem) {
        return new ApiException(403, mensagem);
    }

    /** 404 - recurso nao existe. */
    public static ApiException naoEncontrado(String mensagem) {
        return new ApiException(404, mensagem);
    }

    /** 409 - conflito com o estado atual (ex.: e-mail ja cadastrado). */
    public static ApiException conflito(String mensagem) {
        return new ApiException(409, mensagem);
    }
}
