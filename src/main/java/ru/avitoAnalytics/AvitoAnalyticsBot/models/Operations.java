package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Operations {
    private String operationType;
    private double amountTotal;
    private String itemId;

    @Setter
    private String updatedAt;

    @Override
    public String toString() {
        return "Operations{" +
                "operationType='" + operationType + '\'' +
                ", amountTotal=" + amountTotal +
                ", updatedAt='" + updatedAt + '\'' +
                ", itemId=" + itemId +
                '}';
    }
}
