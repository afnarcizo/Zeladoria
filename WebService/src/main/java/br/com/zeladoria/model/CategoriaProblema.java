package br.com.zeladoria.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "CategoriaProblema")
@SQLDelete(sql = "UPDATE CategoriaProblema SET blnExcluido = true, dtmExclusao = CURRENT_TIMESTAMP WHERE lngId = ?")
@SQLRestriction("blnExcluido = false")
public class CategoriaProblema {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "genCategoria")
    @TableGenerator(
            name = "genCategoria",
            table = "JpaSequences",
            pkColumnName = "strSequenceName",
            valueColumnName = "lngNextVal",
            pkColumnValue = "seqCategoriaProblema",
            initialValue = 1,
            allocationSize = 1
    )
    @Column(name = "lngId", nullable = false)
    private Long lngId;

    @Column(name = "strNome", nullable = false, length = 100, unique = true)
    private String strNome;

    @Column(name = "strDescricao", length = 255)
    private String strDescricao;

    @Column(name = "blnExcluido", nullable = false)
    private Boolean blnExcluido = Boolean.FALSE;

    @Column(name = "dtmExclusao")
    private LocalDateTime dtmExclusao;

    @Column(name = "dtmCriacao", nullable = false, updatable = false)
    private LocalDateTime dtmCriacao;

    public CategoriaProblema() {
    }

    public CategoriaProblema(String strNome, String strDescricao) {
        this.strNome = strNome;
        this.strDescricao = strDescricao;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dtmCriacao == null) {
            this.dtmCriacao = LocalDateTime.now();
        }
        if (this.blnExcluido == null) {
            this.blnExcluido = Boolean.FALSE;
        }
    }

    public Long getLngId() {
        return lngId;
    }

    public void setLngId(Long lngId) {
        this.lngId = lngId;
    }

    public String getStrNome() {
        return strNome;
    }

    public void setStrNome(String strNome) {
        this.strNome = strNome;
    }

    public String getStrDescricao() {
        return strDescricao;
    }

    public void setStrDescricao(String strDescricao) {
        this.strDescricao = strDescricao;
    }

    public Boolean getBlnExcluido() {
        return blnExcluido;
    }

    public void setBlnExcluido(Boolean blnExcluido) {
        this.blnExcluido = blnExcluido;
    }

    public LocalDateTime getDtmExclusao() {
        return dtmExclusao;
    }

    public void setDtmExclusao(LocalDateTime dtmExclusao) {
        this.dtmExclusao = dtmExclusao;
    }

    public LocalDateTime getDtmCriacao() {
        return dtmCriacao;
    }

    public void setDtmCriacao(LocalDateTime dtmCriacao) {
        this.dtmCriacao = dtmCriacao;
    }
}
