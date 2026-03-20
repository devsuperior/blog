package br.com.devsuperior.fipe_search.dto;

import java.math.BigDecimal;
import java.io.Serializable;

public record ConsultaFipeDTO(
        String marca,
        String modelo,
        Integer anoModelo,
        BigDecimal preco,
        String mesReferencia) implements Serializable {

    private static final long serialVersionUID = 1L;
}
