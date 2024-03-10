package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "favourite_items")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteItems {
    @Id
    private Long id;
    @Column(precision = 2, scale = 2)
    private BigDecimal cost;
}
