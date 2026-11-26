package br.com.cafes.dao;

import br.com.cafes.model.Usuario;

import java.sql.ResultSet;
import java.sql.SQLException;

/** DAO de Usuario — usado pelo cadastro e pelo login. */
public class UsuarioDAO extends MysqlDAO {

    public Usuario buscarPorEmail(String email) {
        try (ResultSet rs = super.executar("SELECT id, nome, email, senha_hash FROM usuarios WHERE email = ?", email)) {
            return rs.next() ? this.mapear(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuario por e-mail.", e);
        }
    }

    public long inserir(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, email, senha_hash) VALUES (?, ?, ?)";
        try {
            return super.executarInsert(sql, usuario.getNome(), usuario.getEmail(), usuario.getSenhaHash());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuario.", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        return usuario;
    }
}
