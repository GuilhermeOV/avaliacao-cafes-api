package br.com.cafes.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SINGLETON de conexao com o MySQL.
 *
 * Garante UMA unica instancia desta classe em toda a aplicacao, reaproveitando
 * a mesma conexao. Os dados de acesso vem de variaveis de ambiente (definidas no
 * docker-compose.yml), com valores padrao para rodar localmente.
 *
 * Metodos oferecidos aos DAOs:
 *  - executar()        -> SELECT (retorna ResultSet)
 *  - executarUpdate()  -> UPDATE / DELETE (retorna linhas afetadas)
 *  - executarInsert()  -> INSERT (retorna o id gerado)
 */
public class MysqlSingleton {

    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3307/avaliacao_cafes?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = env("DB_USER", "cafes_user");
    private static final String PASSWORD = env("DB_PASSWORD", "cafes123");

    private static MysqlSingleton instance;
    private Connection conexao;

    // Construtor PRIVADO: so a propria classe cria a instancia (regra do Singleton).
    private MysqlSingleton() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL nao encontrado no projeto.", e);
        }
    }

    public static synchronized MysqlSingleton getInstance() {
        if (instance == null) {
            instance = new MysqlSingleton();
        }
        return instance;
    }

    private synchronized Connection obterConexao() throws SQLException {
        if (this.conexao == null || !this.conexao.isValid(2)) {
            this.conexao = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return this.conexao;
    }

    /** SELECT. O PreparedStatement e fechado automaticamente quando o ResultSet for fechado. */
    public ResultSet executar(String sql, Object... parametros) throws SQLException {
        PreparedStatement ps = this.obterConexao().prepareStatement(sql);
        this.preencher(ps, parametros);
        ps.closeOnCompletion();
        return ps.executeQuery();
    }

    /** UPDATE / DELETE. */
    public int executarUpdate(String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = this.obterConexao().prepareStatement(sql)) {
            this.preencher(ps, parametros);
            return ps.executeUpdate();
        }
    }

    /** INSERT. Retorna o id (AUTO_INCREMENT) gerado pelo banco. */
    public long executarInsert(String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = this.obterConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            this.preencher(ps, parametros);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                return chaves.next() ? chaves.getLong(1) : 0L;
            }
        }
    }

    private void preencher(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
    }

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}
