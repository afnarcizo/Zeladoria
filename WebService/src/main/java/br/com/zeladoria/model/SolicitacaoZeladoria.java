package br.com.zeladoria.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "SolicitacaoZeladoria")
@SQLDelete(sql = "UPDATE SolicitacaoZeladoria SET blnExcluido = true, dtmExclusao = CURRENT_TIMESTAMP WHERE lngId = ?")
@SQLRestriction("blnExcluido = false")
public class SolicitacaoZeladoria {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "genSolicitacao")
    @TableGenerator(
            name = "genSolicitacao",
            table = "JpaSequences",
            pkColumnName = "strSequenceName",
            valueColumnName = "lngNextVal",
            pkColumnValue = "seqSolicitacaoZeladoria",
            initialValue = 1,
            allocationSize = 1
    )
    @Column(name = "lngId", nullable = false)
    private Long lngId;

    @Column(name = "strTelefoneUsuario", nullable = false, length = 25)
    private String strTelefoneUsuario;

    @Column(name = "strEmailUsuario", nullable = false, length = 150)
    private String strEmailUsuario;

    @Column(name = "dblLatitude", nullable = false)
    private Double dblLatitude;

    @Column(name = "dblLongitude", nullable = false)
    private Double dblLongitude;

    @Column(name = "strEnderecoReferencia", length = 255)
    private String strEnderecoReferencia;

    @Column(name = "dtmSolicitacao", nullable = false, updatable = false)
    private LocalDateTime dtmSolicitacao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lngCategoriaId", nullable = false, foreignKey = @ForeignKey(name = "fkSolicitacaoCategoria"))
    private CategoriaProblema categoria;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "lngImagemId", foreignKey = @ForeignKey(name = "fkSolicitacaoImagem"))
    private ImagemSolicitacao imagem;

    @Column(name = "strDescricao", nullable = false, length = 1000)
    private String strDescricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "strStatus", nullable = false, length = 30)
    private StatusSolicitacao strStatus = StatusSolicitacao.ABERTA;

    @Column(name = "blnExcluido", nullable = false)
    private Boolean blnExcluido = Boolean.FALSE;

    @Column(name = "dtmExclusao")
    private LocalDateTime dtmExclusao;

    @Column(name = "dtmAtualizacao")
    private LocalDateTime dtmAtualizacao;

    public SolicitacaoZeladoria() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.dtmSolicitacao == null) {
            this.dtmSolicitacao = LocalDateTime.now();
        }
        if (this.strStatus == null) {
            this.strStatus = StatusSolicitacao.ABERTA;
        }
        if (this.blnExcluido == null) {
            this.blnExcluido = Boolean.FALSE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.dtmAtualizacao = LocalDateTime.now();
    }

    public Long getLngId() {
        return lngId;
    }

    public void setLngId(Long lngId) {
        this.lngId = lngId;
    }

    public String getStrTelefoneUsuario() {
        return strTelefoneUsuario;
    }

    public void setStrTelefoneUsuario(String strTelefoneUsuario) {
        this.strTelefoneUsuario = strTelefoneUsuario;
    }

    public String getStrEmailUsuario() {
        return strEmailUsuario;
    }

    public void setStrEmailUsuario(String strEmailUsuario) {
        this.strEmailUsuario = strEmailUsuario;
    }

    public Double getDblLatitude() {
        return dblLatitude;
    }

    public void setDblLatitude(Double dblLatitude) {
        this.dblLatitude = dblLatitude;
    }

    public Double getDblLongitude() {
        return dblLongitude;
    }

    public void setDblLongitude(Double dblLongitude) {
        this.dblLongitude = dblLongitude;
    }

    public String getStrEnderecoReferencia() {
        return strEnderecoReferencia;
    }

    public void setStrEnderecoReferencia(String strEnderecoReferencia) {
        this.strEnderecoReferencia = strEnderecoReferencia;
    }

    public LocalDateTime getDtmSolicitacao() {
        return dtmSolicitacao;
    }

    public void setDtmSolicitacao(LocalDateTime dtmSolicitacao) {
        this.dtmSolicitacao = dtmSolicitacao;
    }

    public CategoriaProblema getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProblema categoria) {
        this.categoria = categoria;
    }

    public ImagemSolicitacao getImagem() {
        return imagem;
    }

    public void setImagem(ImagemSolicitacao imagem) {
        this.imagem = imagem;
    }

    public String getStrDescricao() {
        return strDescricao;
    }

    public void setStrDescricao(String strDescricao) {
        this.strDescricao = strDescricao;
    }

    public StatusSolicitacao getStrStatus() {
        return strStatus;
    }

    public void setStrStatus(StatusSolicitacao strStatus) {
        this.strStatus = strStatus;
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

    public LocalDateTime getDtmAtualizacao() {
        return dtmAtualizacao;
    }

    public void setDtmAtualizacao(LocalDateTime dtmAtualizacao) {
        this.dtmAtualizacao = dtmAtualizacao;
    }
}
