package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "telegram_id")
    private String telegramId;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;

    @OneToOne
    private Rates rate;

    @PrePersist
    protected void onDefaultValue() {
        if (this.balance == null) {
            this.balance = new BigDecimal("0.0");
        }
    }

    public User(String username, String telegramId) {
        this.username = username;
        this.telegramId = telegramId;
    }

}
