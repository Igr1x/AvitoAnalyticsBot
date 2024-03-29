package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AvitoItems {
    private Long id;
    private Long itemId;
    private String sheetsLink;
}
