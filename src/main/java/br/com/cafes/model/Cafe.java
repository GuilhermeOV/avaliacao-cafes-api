package br.com.cafes.model;

/**
 * MODEL Cafe — espelha a tabela "cafes".
 *
 * Os campos de estatistica (totalReviews e medias) nao existem na tabela:
 * sao calculados pelo DAO com AVG/COUNT sobre as reviews da cafeteria.
 */
public class Cafe {

    private Long id;
    private String nome;
    private String endereco;
    private String bairro;
    private String tipoCafePrincipal;

    // Calculados (somente leitura)
    private Integer totalReviews;
    private Double mediaBebida;
    private Double mediaAmbiente;
    private Double mediaAtendimento;
    private Double mediaGeral;

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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getTipoCafePrincipal() {
        return tipoCafePrincipal;
    }

    public void setTipoCafePrincipal(String tipoCafePrincipal) {
        this.tipoCafePrincipal = tipoCafePrincipal;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Double getMediaBebida() {
        return mediaBebida;
    }

    public void setMediaBebida(Double mediaBebida) {
        this.mediaBebida = mediaBebida;
    }

    public Double getMediaAmbiente() {
        return mediaAmbiente;
    }

    public void setMediaAmbiente(Double mediaAmbiente) {
        this.mediaAmbiente = mediaAmbiente;
    }

    public Double getMediaAtendimento() {
        return mediaAtendimento;
    }

    public void setMediaAtendimento(Double mediaAtendimento) {
        this.mediaAtendimento = mediaAtendimento;
    }

    public Double getMediaGeral() {
        return mediaGeral;
    }

    public void setMediaGeral(Double mediaGeral) {
        this.mediaGeral = mediaGeral;
    }
}
