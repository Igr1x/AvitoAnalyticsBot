package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "account_data")
public class AccountData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "shets_ref")
    private String shets_ref;
}
