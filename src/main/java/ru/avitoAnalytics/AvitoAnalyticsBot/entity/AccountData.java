package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account_data")
@Getter
@Setter
public class AccountData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_owner_id", referencedColumnName = "id")
    private User userOwner;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "sheets_ref")
    private String sheetsRef;

    @Column(name = "account_name")
    private String accountName;

    @Override
    public String toString() {
        return "Данные аккаунта:\n" +
                "clientId - " + clientId + '\n' +
                "clientSecret - " + clientSecret + '\n' +
                "Ссылка на таблицу - " + sheetsRef;
    }
}
