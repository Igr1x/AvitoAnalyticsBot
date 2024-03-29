package ru.avitoAnalytics.AvitoAnalyticsBot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_data")
@Getter
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

    public AccountData() {
    }

    public AccountData(User userOwner, Long userId, String clientId, String clientSecret, String sheetsRef, String accountName) {
        this.userOwner = userOwner;
        this.userId = userId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sheetsRef = sheetsRef;
        this.accountName = accountName;
    }

    @Override
    public String toString() {
        return "Данные аккаунта:\n" +
                "clientId - " + clientId + '\n' +
                "clientSecret - " + clientSecret + '\n' +
                "Ссылка на таблицу - " + sheetsRef;
    }
}
