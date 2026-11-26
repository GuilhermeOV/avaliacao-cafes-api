package br.com.cafes.dao;

import br.com.cafes.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Review — SOMENTE SQL e conversao ResultSet -> {@link Review}.
 * Usa INNER JOIN com "cafes" para trazer tambem o nome da cafeteria.
 */
public class ReviewDAO extends MysqlDAO {

    private static final String SELECT_BASE =
            "SELECT r.id, r.cafe_id, r.usuario_id, r.autor, r.nota_bebida, r.nota_ambiente, "
                    + "r.nota_atendimento, r.comentario, r.criado_em, c.nome AS cafe_nome "
                    + "FROM reviews r "
                    + "INNER JOIN cafes c ON c.id = r.cafe_id ";

    /** READ (listar). cafeId e opcional: null = todas as reviews. */
    public List<Review> listar(Long cafeId) {
        String sql = SELECT_BASE + (cafeId != null ? "WHERE r.cafe_id = ? " : "") + "ORDER BY r.criado_em DESC, r.id DESC";
        Object[] parametros = cafeId != null ? new Object[]{cafeId} : new Object[]{};

        List<Review> lista = new ArrayList<>();
        try (ResultSet rs = super.executar(sql, parametros)) {
            while (rs.next()) {
                lista.add(this.mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar reviews.", e);
        }
        return lista;
    }

    /** READ (por id). */
    public Review buscarPorId(Long id) {
        try (ResultSet rs = super.executar(SELECT_BASE + "WHERE r.id = ?", id)) {
            return rs.next() ? this.mapear(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar review por id.", e);
        }
    }

    /** Quantas reviews uma cafeteria possui (usado na regra de exclusao). */
    public int contarPorCafe(Long cafeId) {
        try (ResultSet rs = super.executar("SELECT COUNT(*) FROM reviews WHERE cafe_id = ?", cafeId)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar reviews.", e);
        }
    }

    /** CREATE. Retorna o id gerado. */
    public long inserir(Review review) {
        String sql = "INSERT INTO reviews (cafe_id, usuario_id, autor, nota_bebida, nota_ambiente, "
                + "nota_atendimento, comentario) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            return super.executarInsert(sql,
                    review.getCafeId(), review.getUsuarioId(), review.getAutor(),
                    review.getNotaBebida(), review.getNotaAmbiente(), review.getNotaAtendimento(),
                    review.getComentario());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir review.", e);
        }
    }

    /** UPDATE (so as notas e o comentario podem mudar). */
    public void alterar(Review review) {
        String sql = "UPDATE reviews SET nota_bebida = ?, nota_ambiente = ?, nota_atendimento = ?, "
                + "comentario = ? WHERE id = ?";
        try {
            super.executarUpdate(sql,
                    review.getNotaBebida(), review.getNotaAmbiente(), review.getNotaAtendimento(),
                    review.getComentario(), review.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar review.", e);
        }
    }

    /** DELETE. */
    public void deletar(Long id) {
        try {
            super.executarUpdate("DELETE FROM reviews WHERE id = ?", id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar review.", e);
        }
    }

    private Review mapear(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setCafeId(rs.getLong("cafe_id"));
        review.setCafeNome(rs.getString("cafe_nome"));
        review.setUsuarioId(rs.getLong("usuario_id"));
        review.setAutor(rs.getString("autor"));
        review.setNotaBebida(rs.getInt("nota_bebida"));
        review.setNotaAmbiente(rs.getInt("nota_ambiente"));
        review.setNotaAtendimento(rs.getInt("nota_atendimento"));
        review.setComentario(rs.getString("comentario"));
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        review.setCriadoEm(criadoEm != null ? criadoEm.toLocalDateTime() : null);
        return review;
    }
}
