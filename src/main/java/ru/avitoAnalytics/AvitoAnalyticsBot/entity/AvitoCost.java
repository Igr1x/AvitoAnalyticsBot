package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity(name= "avito_cost")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvitoCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String subcategory;
    private String subcategory1;
    private String subcategory2;
    private String subcategory3;
    private String subcategory4;
    private String subcategory5;

    @Column(name = "cost", precision = 4, scale = 2)
    private BigDecimal cost;
}
