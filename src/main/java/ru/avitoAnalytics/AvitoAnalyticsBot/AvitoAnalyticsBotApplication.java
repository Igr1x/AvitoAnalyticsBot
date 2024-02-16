package ru.avitoAnalytics.AvitoAnalyticsBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.stereotype.Controller;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class AvitoAnalyticsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvitoAnalyticsBotApplication.class, args);
	}

}
