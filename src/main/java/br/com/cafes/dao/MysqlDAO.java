package br.com.cafes.dao;

import br.com.cafes.config.MysqlSingleton;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe BASE dos DAOs (Data Access Object).
 *
 * Guarda a referencia ao {@link MysqlSingleton}. Os DAOs filhos herdam daqui
 * e sao a UNICA camada do sistema que escreve SQL.
 */
public abstract class MysqlDAO {

    protected final MysqlSingleton banco;

    protected MysqlDAO() {
        this.banco = MysqlSingleton.getInstance();
    }

    protected ResultSet executar(String sql, Object... parametros) throws SQLException {
        return this.banco.executar(sql, parametros);
    }

    protected int executarUpdate(String sql, Object... parametros) throws SQLException {
        return this.banco.executarUpdate(sql, parametros);
    }

    protected long executarInsert(String sql, Object... parametros) throws SQLException {
        return this.banco.executarInsert(sql, parametros);
    }

    /** Le uma coluna numerica que pode ser NULL (ex.: AVG sem reviews). */
    protected Double lerDouble(ResultSet rs, String coluna) throws SQLException {
        double valor = rs.getDouble(coluna);
        return rs.wasNull() ? null : Math.round(valor * 100) / 100.0;
    }
}
