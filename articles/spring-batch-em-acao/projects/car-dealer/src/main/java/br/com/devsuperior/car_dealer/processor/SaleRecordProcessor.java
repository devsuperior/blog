package br.com.devsuperior.car_dealer.processor;

import br.com.devsuperior.car_dealer.domain.SaleRecord;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class SaleRecordProcessor implements ItemProcessor<SaleRecord, SaleRecord> {

    // Normaliza dados do CSV antes da escrita.
    @Override
    public SaleRecord process(SaleRecord item) {
        String model = item.model() == null ? null : item.model().trim();
        String paymentType = item.paymentType() == null ? null : item.paymentType().trim();
        return new SaleRecord(
                item.dealerId(),
                item.saleDate(),
                model,
                paymentType,
                item.salePriceBrl()
        );
    }
}
