package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.Getter;

@Getter
public class Operations {
    private String operationType;
    private double amountTotal;
    private String updatedAt;
    private String itemId;

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

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
