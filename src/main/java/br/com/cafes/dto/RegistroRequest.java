package br.com.cafes.dto;

/** Corpo JSON recebido em POST /api/auth/registrar. */
public record RegistroRequest(String nome, String email, String senha) {
}
