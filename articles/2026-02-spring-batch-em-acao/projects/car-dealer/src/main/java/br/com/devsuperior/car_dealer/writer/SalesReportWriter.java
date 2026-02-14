package br.com.devsuperior.car_dealer.writer;

import br.com.devsuperior.car_dealer.domain.ReportLine;
import br.com.devsuperior.car_dealer.domain.SaleRecord;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SalesReportWriter implements ItemWriter<SaleRecord>, StepExecutionListener {

    private final JdbcTemplate jdbcTemplate;
    private final String outputFile;
    private Map<String, String> dealerNames = new HashMap<>();
    private final Map<String, ReportLine> report = new LinkedHashMap<>();

    // Injecao de dependencias e caminho do arquivo de saida.
    public SalesReportWriter(JdbcTemplate jdbcTemplate,
                             @Value("${app.matriz-report-file}") String outputFile) {
        this.jdbcTemplate = jdbcTemplate;
        this.outputFile = outputFile;
    }

    // Carrega o mapa de filiais antes do step iniciar.
    @Override
    public void beforeStep(StepExecution stepExecution) {
        dealerNames = jdbcTemplate.query("SELECT dealer_id, dealer_name FROM dealers", rs -> {
            Map<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("dealer_id"), rs.getString("dealer_name"));
            }
            return map;
        });
    }

    // Agrega vendas por filial e modelo durante o processamento.
    @Override
    public void write(Chunk<? extends SaleRecord> items) {
        for (SaleRecord item : items) {
            String dealerName = dealerNames.getOrDefault(item.dealerId(), item.dealerId());
            String key = dealerName + "|" + item.model();
            report.compute(key, (k, existing) -> {
                ReportLine base = existing == null ? new ReportLine(dealerName, item.model()) : existing;
                return base.addSale(item.salePriceBrl());
            });
        }
    }

    // Grava o relatorio final ao termino do step.
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        writeReport();
        return ExitStatus.COMPLETED;
    }

    // Escreve o CSV consolidado no destino.
    private void writeReport() {
        Path path = Path.of(outputFile);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<ReportLine> lines = new ArrayList<>(report.values());
            lines.sort(Comparator.comparing(ReportLine::dealerName)
                    .thenComparing(ReportLine::model));
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write("dealer_name,model,units_sold,revenue_brl");
                writer.newLine();
                for (ReportLine line : lines) {
                    writer.write(line.toCsv());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputFile, e);
        }
    }
}
