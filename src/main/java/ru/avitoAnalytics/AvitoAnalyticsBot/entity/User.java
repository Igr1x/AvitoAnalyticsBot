package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;
import org.springframework.aot.generate.GeneratedTypeReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    private List<AccountData> accounts;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "state")
    private String state;

    @OneToOne
    private Rates rate;

    @PrePersist
    protected void onDefaultValue() {
        if (this.balance == null) {
            this.balance = new BigDecimal("0.0");
        }
        if (this.accounts == null) {
            this.accounts = new ArrayList<>();
        }
    }

    public User(String username, String telegramId) {
        this.username = username;
        this.telegramId = telegramId;
    }

}
