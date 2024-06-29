package ru.avitoAnalytics.AvitoAnalyticsBot.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Product {
    @JsonProperty("item_id")
    private Long id;
    private String address;
    private List<String> categories;
    private List<Characteristic> characteristics;
}
