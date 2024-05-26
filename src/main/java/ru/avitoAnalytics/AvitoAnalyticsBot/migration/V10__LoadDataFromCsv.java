package ru.avitoAnalytics.AvitoAnalyticsBot.migration;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;

@Component
public class V10__LoadDataFromCsv extends BaseJavaMigration {

    private static final String SQL = """
            INSERT INTO avito_cost (region, category, subcategory, subcategory1, subcategory2, subcategory3, subcategory4, subcategory5, cost) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::numeric)
            """;

    @Override
    public void migrate(Context context) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/cost.csv")))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());

            List<CSVRecord> records = csvParser.getRecords();

            var list = records.stream()
                    .map(r -> new Object[] {
                            r.get("region"),
                            r.get("category"),
                            r.get("subcategory"),
                            r.get("subcategory1"),
                            r.get("subcategory2"),
                            r.get("subcategory3"),
                            r.get("subcategory4"),
                            r.get("subcategory5"),
                            r.get("cost")
                    })
                    .toList();
            jdbcTemplate.batchUpdate(SQL, list);
        }
    }
}
