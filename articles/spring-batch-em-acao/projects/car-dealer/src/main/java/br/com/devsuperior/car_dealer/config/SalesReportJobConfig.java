package br.com.devsuperior.car_dealer.config;

import br.com.devsuperior.car_dealer.domain.SaleRecord;
import br.com.devsuperior.car_dealer.writer.SalesReportWriter;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SalesReportJobConfig {

    // Define o job principal do batch.
    @Bean
    public Job salesReportJob(JobRepository jobRepository, Step salesReportStep) {
        return new JobBuilder("salesReportJob", jobRepository)
                .start(salesReportStep)
                .build();
    }

    // Monta o step com reader, processor, writer e transacao.
    @Bean
    public Step salesReportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                MultiResourceItemReader<SaleRecord> saleReader,
                                ItemProcessor<SaleRecord, SaleRecord> saleProcessor,
                                SalesReportWriter writer) {
        return new StepBuilder("salesReportStep", jobRepository)
                .<SaleRecord, SaleRecord>chunk(100)
                .reader(saleReader)
                .processor(saleProcessor)
                .writer(writer)
                .transactionManager(transactionManager)
                .build();
    }
}
