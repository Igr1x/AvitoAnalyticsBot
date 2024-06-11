package ru.avitoAnalytics.AvitoAnalyticsBot.models;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class Product {

    private String url;
    @SerializedName("is_new")
    private boolean isNew;
    private String address;
    private List<String> categories;

}
