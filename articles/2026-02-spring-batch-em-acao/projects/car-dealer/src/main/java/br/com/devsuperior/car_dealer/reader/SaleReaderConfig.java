package br.com.devsuperior.car_dealer.reader;

import br.com.devsuperior.car_dealer.domain.SaleRecord;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class SaleReaderConfig {

    // Agrega multiplos arquivos CSV em um unico reader.
    @Bean
    @StepScope
    public MultiResourceItemReader<SaleRecord> saleReader(ResourcePatternResolver resolver,
                                                          FlatFileItemReader<SaleRecord> saleFileReader,
                                                          @Value("${app.filial-report-pattern}") String pattern)
            throws IOException {
        Resource[] resources = resolver.getResources(pattern);
        Arrays.sort(resources, Comparator.comparing(Resource::getFilename));
        MultiResourceItemReader<SaleRecord> reader = new MultiResourceItemReader<>(saleFileReader);
        reader.setResources(resources);
        return reader;
    }

    // Le e mapeia cada linha do CSV para SaleRecord.
    @Bean
    @StepScope
    public FlatFileItemReader<SaleRecord> saleFileReader() {
        RecordFieldSetMapper<SaleRecord> mapper = new RecordFieldSetMapper<>(SaleRecord.class);

        return new FlatFileItemReaderBuilder<SaleRecord>()
                .name("saleFileReader")
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited()
                .delimiter(DelimitedLineTokenizer.DELIMITER_COMMA)
                .names("dealerId", "saleDate", "model", "paymentType", "salePriceBrl")
                .fieldSetMapper(mapper)
                .build();
    }
}
