package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rates")
public class Rates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(precision = 18, scale = 2)
    private BigDecimal cost;

    @Column(columnDefinition = "text")
    private String description;

}
