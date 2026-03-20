package br.com.devsuperior.fipe_search.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "referencias")
public class ReferenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id")
    private ModeloEntity modelo;

    @Column(name = "ano_modelo")
    private Integer anoModelo;

    private BigDecimal preco;

    @Column(name = "mes_referencia")
    private String mesReferencia;

    public ReferenciaEntity() {
    }

    public ReferenciaEntity(Long id, ModeloEntity modelo, Integer anoModelo, BigDecimal preco, String mesReferencia) {
        this.id = id;
        this.modelo = modelo;
        this.anoModelo = anoModelo;
        this.preco = preco;
        this.mesReferencia = mesReferencia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ModeloEntity getModelo() {
        return modelo;
    }

    public void setModelo(ModeloEntity modelo) {
        this.modelo = modelo;
    }

    public Integer getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }
}
