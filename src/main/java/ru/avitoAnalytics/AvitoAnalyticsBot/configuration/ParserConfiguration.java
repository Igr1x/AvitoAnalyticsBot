package ru.avitoAnalytics.AvitoAnalyticsBot.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableRetry
@Getter
public class ParserConfiguration {

    @Value("${parser.url:http://127.0.0.1:8000}")
    private String parserUri;

    @Bean
    public RestTemplate parserRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri(parserUri)
                .build();
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder().create();
    }
}
