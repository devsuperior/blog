package br.com.devsuperior.fipe_search.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "modelos")
public class ModeloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id")
    private MarcaEntity marca;

    private String nome;

    public ModeloEntity() {
    }

    public ModeloEntity(Long id, MarcaEntity marca, String nome) {
        this.id = id;
        this.marca = marca;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarcaEntity getMarca() {
        return marca;
    }

    public void setMarca(MarcaEntity marca) {
        this.marca = marca;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
