package com.devsuperior.billing.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CurrencyConverter {

    private static final Logger log = LoggerFactory.getLogger(CurrencyConverter.class);
    private static final String FRANKFURTER_URL = "https://api.frankfurter.dev/v1/latest";

    private final RestClient restClient;

    public CurrencyConverter() {
        this.restClient = RestClient.builder().baseUrl(FRANKFURTER_URL).build();
    }

    @SuppressWarnings("unchecked")
    public BigDecimal toUsd(BigDecimal amount, String fromCurrency) {
        if ("USD".equalsIgnoreCase(fromCurrency)) {
            return amount;
        }

        try {
            log.info("Consultando taxa de cambio {} -> USD na Frankfurter API", fromCurrency);

            Map<String, Object> response = restClient.get()
                    .uri("?base={base}&symbols=USD", fromCurrency)
                    .retrieve()
                    .body(Map.class);

            Map<String, Number> rates = (Map<String, Number>) response.get("rates");
            BigDecimal rate = new BigDecimal(rates.get("USD").toString());

            BigDecimal converted = amount.multiply(rate);
            log.info("Conversao: {} {} = {} USD (taxa: {})", amount, fromCurrency, converted, rate);
            return converted;

        } catch (Exception e) {
            log.warn("Falha ao consultar cambio, usando valor original: {}", e.getMessage());
            return amount;
        }
    }
}
