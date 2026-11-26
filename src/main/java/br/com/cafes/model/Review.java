package br.com.cafes.model;

import java.time.LocalDateTime;

/**
 * MODEL Review — espelha a tabela "reviews".
 *
 * Cada review avalia uma cafeteria em tres criterios (nota de 1 a 5):
 * bebida, ambiente e atendimento. O "autor" e o "usuarioId" vem do token JWT,
 * nunca do corpo da requisicao.
 */
public class Review {

    private Long id;
    private Long cafeId;
    private String cafeNome;
    private Long usuarioId;
    private String autor;
    private Integer notaBebida;
    private Integer notaAmbiente;
    private Integer notaAtendimento;
    private String comentario;
    private LocalDateTime criadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCafeId() {
        return cafeId;
    }

    public void setCafeId(Long cafeId) {
        this.cafeId = cafeId;
    }

    public String getCafeNome() {
        return cafeNome;
    }

    public void setCafeNome(String cafeNome) {
        this.cafeNome = cafeNome;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Integer getNotaBebida() {
        return notaBebida;
    }

    public void setNotaBebida(Integer notaBebida) {
        this.notaBebida = notaBebida;
    }

    public Integer getNotaAmbiente() {
        return notaAmbiente;
    }

    public void setNotaAmbiente(Integer notaAmbiente) {
        this.notaAmbiente = notaAmbiente;
    }

    public Integer getNotaAtendimento() {
        return notaAtendimento;
    }

    public void setNotaAtendimento(Integer notaAtendimento) {
        this.notaAtendimento = notaAtendimento;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
