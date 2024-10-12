package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.AccountNotFoundException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.PaymentService;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserService userService;

    @Override
    public AnswerPreCheckoutQuery processPayment(PreCheckoutQuery query) {
        var userId = query.getFrom().getId();
        var totalAmount = query.getTotalAmount() / 100;
        var user = userService.getUser(userId).orElseThrow();
        var currentBalance = user.getBalance();
        user.setBalance(currentBalance.add(BigDecimal.valueOf(totalAmount)));
        userService.updateUserData(user);
        var sb = new StringBuilder();
        sb.append("На ваш счёт зачисленно: ").append(totalAmount).append(" руб.");
        var errmsg = "При оплате возникла ошибка";
        var req = new AnswerPreCheckoutQuery();
        req.setPreCheckoutQueryId(query.getId());
        req.setOk(true);
        return req;
    }
}
