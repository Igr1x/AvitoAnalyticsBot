package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ads")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ads {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long avitoId;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private AccountData ownerId;

    @Column(precision = 2, scale = 2)
    private BigDecimal cost;

    private String city;

    private LocalDate pubDate;
    private LocalDate closingDate;

    public Ads(Long avitoId, AccountData ownerId) {
        this.avitoId = avitoId;
        this.ownerId = ownerId;
    }
}
