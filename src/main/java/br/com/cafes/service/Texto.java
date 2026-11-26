package br.com.cafes.service;

import br.com.cafes.exception.ApiException;

import java.util.Locale;

/** Pequenos ajudantes de texto reaproveitados pelos Services (evita repetir codigo). */
final class Texto {

    private Texto() {
    }

    /** Remove espacos das pontas; texto vazio vira null. */
    static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }

    /** Igual a limpar(), mas em minusculas (ex.: "Cold Brew" -> "cold brew"). */
    static String minusculo(String valor) {
        String limpo = limpar(valor);
        return limpo == null ? null : limpo.toLowerCase(Locale.ROOT);
    }

    /** Valida campo obrigatorio e tamanho maximo. */
    static void obrigatorio(String valor, String campo, int tamanhoMaximo) {
        if (valor == null) {
            throw ApiException.requisicaoInvalida("Campo '" + campo + "' e obrigatorio.");
        }
        maximo(valor, campo, tamanhoMaximo);
    }

    static void maximo(String valor, String campo, int tamanhoMaximo) {
        if (valor != null && valor.length() > tamanhoMaximo) {
            throw ApiException.requisicaoInvalida("Campo '" + campo + "' deve ter no maximo " + tamanhoMaximo + " caracteres.");
        }
    }
}
