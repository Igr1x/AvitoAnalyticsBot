package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AutoRenewalRateController {

    private final UserService userService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void autoRenewal() {
        List<User> allUser = userService.getAllUser();
        allUser.forEach(userService::extensionRate);
    }

}
