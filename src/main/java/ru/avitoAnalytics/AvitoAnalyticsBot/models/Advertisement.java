package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Advertisement {

    private Long id;
    private String address;

}
