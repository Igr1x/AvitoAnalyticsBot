package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.aot.generate.GeneratedTypeReference;

import java.math.BigDecimal;
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

    @OneToMany(mappedBy = "user")
    private List<AccountData> accounts;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;

    @OneToOne
    private Rates rate;

}
