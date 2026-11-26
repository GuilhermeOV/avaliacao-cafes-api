package br.com.cafes.dto;

/** Corpo JSON recebido em POST /api/auth/login. */
public record LoginRequest(String email, String senha) {
}
