package br.com.cafes.dto;

import br.com.cafes.model.Usuario;

/** Resposta do login: o token JWT e os dados basicos do usuario. */
public record LoginResponse(String token, String tipo, long expiraEmHoras, Usuario usuario) {
}
