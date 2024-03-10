package ru.avitoAnalytics.AvitoAnalyticsBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AvitoAnalyticsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvitoAnalyticsBotApplication.class, args);
	}

}
