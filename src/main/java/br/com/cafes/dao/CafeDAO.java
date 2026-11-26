package br.com.cafes.dao;

import br.com.cafes.model.Cafe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Cafe — SOMENTE SQL e conversao ResultSet -> {@link Cafe}.
 *
 * Usa LEFT JOIN com reviews + AVG/COUNT para devolver cada cafeteria
 * ja com a quantidade de avaliacoes e as medias por criterio.
 */
public class CafeDAO extends MysqlDAO {

    private static final String SELECT_BASE =
            "SELECT c.id, c.nome, c.endereco, c.bairro, c.tipo_cafe_principal, "
                    + "COUNT(r.id) AS total_reviews, "
                    + "AVG(r.nota_bebida) AS media_bebida, "
                    + "AVG(r.nota_ambiente) AS media_ambiente, "
                    + "AVG(r.nota_atendimento) AS media_atendimento, "
                    + "AVG((r.nota_bebida + r.nota_ambiente + r.nota_atendimento) / 3) AS media_geral "
                    + "FROM cafes c "
                    + "LEFT JOIN reviews r ON r.cafe_id = c.id ";

    /**
     * READ (listar com filtros opcionais).
     *
     * @param bairro   filtra pelo bairro (ignora maiusculas/minusculas); null = todos
     * @param tipo     filtra pelo tipo de cafe principal; null = todos
     * @param ordenarPorMedia true = melhores avaliados primeiro; false = ordem alfabetica
     */
    public List<Cafe> listar(String bairro, String tipo, boolean ordenarPorMedia) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (bairro != null) {
            sql.append("AND c.bairro = ? ");
            parametros.add(bairro);
        }
        if (tipo != null) {
            sql.append("AND c.tipo_cafe_principal = ? ");
            parametros.add(tipo);
        }

        sql.append("GROUP BY c.id ");
        sql.append(ordenarPorMedia ? "ORDER BY media_geral IS NULL, media_geral DESC, c.nome" : "ORDER BY c.nome");

        List<Cafe> lista = new ArrayList<>();
        try (ResultSet rs = super.executar(sql.toString(), parametros.toArray())) {
            while (rs.next()) {
                lista.add(this.mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar cafes.", e);
        }
        return lista;
    }

    /** READ (por id). */
    public Cafe buscarPorId(Long id) {
        String sql = SELECT_BASE + "WHERE c.id = ? GROUP BY c.id";
        try (ResultSet rs = super.executar(sql, id)) {
            return rs.next() ? this.mapear(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cafe por id.", e);
        }
    }

    /** CREATE. Retorna o id gerado. */
    public long inserir(Cafe cafe) {
        String sql = "INSERT INTO cafes (nome, endereco, bairro, tipo_cafe_principal) VALUES (?, ?, ?, ?)";
        try {
            return super.executarInsert(sql,
                    cafe.getNome(), cafe.getEndereco(), cafe.getBairro(), cafe.getTipoCafePrincipal());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir cafe.", e);
        }
    }

    /** UPDATE. */
    public void alterar(Cafe cafe) {
        String sql = "UPDATE cafes SET nome = ?, endereco = ?, bairro = ?, tipo_cafe_principal = ? WHERE id = ?";
        try {
            super.executarUpdate(sql,
                    cafe.getNome(), cafe.getEndereco(), cafe.getBairro(), cafe.getTipoCafePrincipal(), cafe.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar cafe.", e);
        }
    }

    /** DELETE. */
    public void deletar(Long id) {
        try {
            super.executarUpdate("DELETE FROM cafes WHERE id = ?", id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar cafe.", e);
        }
    }

    private Cafe mapear(ResultSet rs) throws SQLException {
        Cafe cafe = new Cafe();
        cafe.setId(rs.getLong("id"));
        cafe.setNome(rs.getString("nome"));
        cafe.setEndereco(rs.getString("endereco"));
        cafe.setBairro(rs.getString("bairro"));
        cafe.setTipoCafePrincipal(rs.getString("tipo_cafe_principal"));
        cafe.setTotalReviews(rs.getInt("total_reviews"));
        cafe.setMediaBebida(this.lerDouble(rs, "media_bebida"));
        cafe.setMediaAmbiente(this.lerDouble(rs, "media_ambiente"));
        cafe.setMediaAtendimento(this.lerDouble(rs, "media_atendimento"));
        cafe.setMediaGeral(this.lerDouble(rs, "media_geral"));
        return cafe;
    }
}
