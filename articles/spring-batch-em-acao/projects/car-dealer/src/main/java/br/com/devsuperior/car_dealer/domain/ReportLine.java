package br.com.devsuperior.car_dealer.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ReportLine(
        String dealerName,
        String model,
        int unitsSold,
        BigDecimal revenueBrl
) {

    // Cria uma linha agregada com valores iniciais zerados.
    public ReportLine(String dealerName, String model) {
        this(dealerName, model, 0, BigDecimal.ZERO);
    }

    // Soma uma venda e retorna um novo agregado.
    public ReportLine addSale(BigDecimal salePrice) {
        return new ReportLine(dealerName, model, unitsSold + 1, revenueBrl.add(salePrice));
    }

    // Converte o agregado para uma linha CSV.
    public String toCsv() {
        BigDecimal rounded = revenueBrl.setScale(2, RoundingMode.HALF_UP);
        return dealerName + "," + model + "," + unitsSold + "," + rounded.toPlainString();
    }
}
