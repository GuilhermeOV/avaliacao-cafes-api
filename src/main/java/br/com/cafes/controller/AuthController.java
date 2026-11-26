package br.com.cafes.controller;

import br.com.cafes.dto.LoginRequest;
import br.com.cafes.dto.RegistroRequest;
import br.com.cafes.model.Usuario;
import br.com.cafes.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * CONTROLLER de Autenticacao (rotas publicas).
 *
 *  POST /api/auth/registrar   cria usuario  -> 201
 *  POST /api/auth/login       gera token JWT -> 200
 */
@WebServlet("/api/auth/*")
public class AuthController extends BaseController {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp, String[] caminho) throws IOException {
        String acao = caminho.length == 1 ? caminho[0] : "";
        switch (acao) {
            case "registrar" -> {
                Usuario usuario = this.authService.registrar(this.lerJson(req, RegistroRequest.class));
                this.responder(resp, 201, usuario);
            }
            case "login" -> this.responder(resp, 200, this.authService.login(this.lerJson(req, LoginRequest.class)));
            default -> throw rotaNaoEncontrada();
        }
    }
}
