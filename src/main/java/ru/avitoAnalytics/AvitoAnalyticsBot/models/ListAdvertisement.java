package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class ListAdvertisement {

    @JsonSetter("resources")
    private List<Advertisement> advertisementList;

}
