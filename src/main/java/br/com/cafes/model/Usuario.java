package br.com.cafes.model;

/**
 * MODEL Usuario — espelha a tabela "usuarios".
 *
 * O campo senhaHash e "transient": o Gson ignora esse campo, entao o hash
 * da senha NUNCA aparece em uma resposta JSON da API.
 */
public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private transient String senhaHash;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}
