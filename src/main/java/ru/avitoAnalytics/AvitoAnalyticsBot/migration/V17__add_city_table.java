package ru.avitoAnalytics.AvitoAnalyticsBot.migration;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class V17__add_city_table extends BaseJavaMigration {

    private static final String SQL = """
            INSERT INTO city (name_city)
            VALUES (?);
            """;

    @Override
    public void migrate(Context context) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));

        List<String> cities = readFileToList("/city.txt");

        var list = cities.stream()
                .map(c -> new Object[]{c})
                .toList();

        jdbcTemplate.batchUpdate(SQL, list);
    }

    public static List<String> readFileToList(String filePath) {
        List<String> list = new ArrayList<>();

        try (InputStream inputStream = Objects.requireNonNull(V17__add_city_table.class.getResourceAsStream(filePath));
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
