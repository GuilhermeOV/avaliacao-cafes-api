package br.com.cafes.service;

import br.com.cafes.config.JwtUtil;
import br.com.cafes.dao.UsuarioDAO;
import br.com.cafes.dto.LoginRequest;
import br.com.cafes.dto.LoginResponse;
import br.com.cafes.dto.RegistroRequest;
import br.com.cafes.exception.ApiException;
import br.com.cafes.model.Usuario;
import br.com.cafes.util.SenhaUtil;

/** SERVICE de autenticacao — cadastro de usuario e login com JWT. */
public class AuthService {

    private static final int SENHA_MINIMA = 6;

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public Usuario registrar(RegistroRequest req) {
        if (req == null) {
            throw ApiException.requisicaoInvalida("Corpo da requisicao e obrigatorio.");
        }
        String nome = Texto.limpar(req.nome());
        String email = Texto.minusculo(req.email());

        Texto.obrigatorio(nome, "nome", 100);
        Texto.obrigatorio(email, "email", 100);
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw ApiException.requisicaoInvalida("E-mail invalido.");
        }
        if (req.senha() == null || req.senha().length() < SENHA_MINIMA) {
            throw ApiException.requisicaoInvalida("A senha deve ter pelo menos " + SENHA_MINIMA + " caracteres.");
        }
        if (this.usuarioDAO.buscarPorEmail(email) != null) {
            throw ApiException.conflito("Ja existe um usuario com este e-mail.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(SenhaUtil.gerarHash(req.senha()));
        usuario.setId(this.usuarioDAO.inserir(usuario));
        return usuario;
    }

    public LoginResponse login(LoginRequest req) {
        if (req == null || req.email() == null || req.senha() == null) {
            throw ApiException.requisicaoInvalida("Informe 'email' e 'senha'.");
        }
        Usuario usuario = this.usuarioDAO.buscarPorEmail(Texto.minusculo(req.email()));

        // Mesma mensagem para e-mail inexistente e senha errada (nao revela qual dos dois falhou).
        if (usuario == null || !SenhaUtil.confere(req.senha(), usuario.getSenhaHash())) {
            throw ApiException.naoAutorizado("E-mail ou senha invalidos.");
        }
        return new LoginResponse(JwtUtil.gerarToken(usuario), "Bearer", JwtUtil.horasValidade(), usuario);
    }
}
