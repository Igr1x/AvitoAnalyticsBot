package ru.avitoAnalytics.AvitoAnalyticsBot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatSummary {
    private String date;
    private Object uniqViews;
    private Object cv;
    private Object uniqContacts;
    private Object uniqFavorites;
    private Object sumViews;
    private Object sumRaise;
    private Object totalSum;
    private Object sumContact;
}
