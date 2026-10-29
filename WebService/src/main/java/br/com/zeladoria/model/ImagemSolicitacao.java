package br.com.zeladoria.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "ImagemSolicitacao")
@SQLDelete(sql = "UPDATE ImagemSolicitacao SET blnExcluido = true, dtmExclusao = CURRENT_TIMESTAMP WHERE lngId = ?")
@SQLRestriction("blnExcluido = false")
public class ImagemSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "genImagem")
    @TableGenerator(
            name = "genImagem",
            table = "JpaSequences",
            pkColumnName = "strSequenceName",
            valueColumnName = "lngNextVal",
            pkColumnValue = "seqImagemSolicitacao",
            initialValue = 1,
            allocationSize = 1
    )
    @Column(name = "lngId", nullable = false)
    private Long lngId;

    @Column(name = "strNomeArquivo", nullable = false, length = 255)
    private String strNomeArquivo;

    @Column(name = "strTipoConteudo", nullable = false, length = 100)
    private String strTipoConteudo;

    @Column(name = "lngTamanhoBytes", nullable = false)
    private Long lngTamanhoBytes;

    @Lob
    @Column(name = "strDadosBase64", nullable = false, columnDefinition = "LONGTEXT")
    private String strDadosBase64;

    @Column(name = "blnExcluido", nullable = false)
    private Boolean blnExcluido = Boolean.FALSE;

    @Column(name = "dtmExclusao")
    private LocalDateTime dtmExclusao;

    @Column(name = "dtmCriacao", nullable = false, updatable = false)
    private LocalDateTime dtmCriacao;

    public ImagemSolicitacao() {
    }

    public ImagemSolicitacao(String strNomeArquivo, String strTipoConteudo, Long lngTamanhoBytes, String strDadosBase64) {
        this.strNomeArquivo = strNomeArquivo;
        this.strTipoConteudo = strTipoConteudo;
        this.lngTamanhoBytes = lngTamanhoBytes;
        this.strDadosBase64 = strDadosBase64;
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

    public String getStrNomeArquivo() {
        return strNomeArquivo;
    }

    public void setStrNomeArquivo(String strNomeArquivo) {
        this.strNomeArquivo = strNomeArquivo;
    }

    public String getStrTipoConteudo() {
        return strTipoConteudo;
    }

    public void setStrTipoConteudo(String strTipoConteudo) {
        this.strTipoConteudo = strTipoConteudo;
    }

    public Long getLngTamanhoBytes() {
        return lngTamanhoBytes;
    }

    public void setLngTamanhoBytes(Long lngTamanhoBytes) {
        this.lngTamanhoBytes = lngTamanhoBytes;
    }

    public String getStrDadosBase64() {
        return strDadosBase64;
    }

    public void setStrDadosBase64(String strDadosBase64) {
        this.strDadosBase64 = strDadosBase64;
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
