package ru.avitoAnalytics.AvitoAnalyticsBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByTelegramId(String telegramId);
    boolean existsByTelegramId(String telegramId);
}
