package ru.avitoAnalytics.AvitoAnalyticsBot.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.avitoAnalytics.AvitoAnalyticsBot.actions.*;
import ru.avitoAnalytics.AvitoAnalyticsBot.configuration.BotConfig;
import ru.avitoAnalytics.AvitoAnalyticsBot.entity.User;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.UserService;
import ru.avitoAnalytics.AvitoAnalyticsBot.util.PatternMap;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserService userService;

    private final BalanceAction balanceAction;
    private final ConnectTariff connectTariff;
    private final AccountsAction accountsAction;
    private final SelectAccountAction selectAccountAction;
    private final DeleteAccountAction deleteAccount;

    private PatternMap<String, Actions> actionsCommand = new PatternMap<>();
    private PatternMap<String, Actions> actionsKeyboard = new PatternMap<>();

    @PostConstruct
    public void init() {
        actionsCommand.put("/start", new StartAction());
        actionsCommand.put("/help", new HelpAction());
        actionsCommand.put("/tariffs", new TariffsAction());
        actionsCommand.put("/balance", balanceAction);
        actionsCommand.put("/accounts", accountsAction);
        actionsKeyboard.put("/start", new StartAction());
        actionsKeyboard.put("/help", new HelpAction());
        actionsKeyboard.put("/tariffs", new TariffsAction());
        actionsKeyboard.put("/balance", balanceAction);
        actionsKeyboard.put("/accounts", accountsAction);
        actionsKeyboard.put("backToTariffs", new TariffsAction());
        actionsKeyboard.putPattern(key -> key.startsWith("accountId-"), selectAccountAction);
        actionsKeyboard.putPattern(key -> key.startsWith("back-"), accountsAction);
        actionsKeyboard.putPattern(key -> key.startsWith("deleteAccountId-"), deleteAccount);
        actionsKeyboard.putPattern(key -> key.startsWith("tariff"), new TariffAction());
        actionsKeyboard.putPattern(key -> key.startsWith("connect"), connectTariff);
    }

    public TelegramBot(BotConfig botConfig, UserService userService, BalanceAction balanceAction, ConnectTariff connectTariff, AccountsAction accountsAction, SelectAccountAction selectAccountAction, DeleteAccountAction deleteAccount) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.balanceAction = balanceAction;
        this.connectTariff = connectTariff;
        this.accountsAction = accountsAction;
        this.selectAccountAction = selectAccountAction;
        this.deleteAccount = deleteAccount;
        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", ""));
        listOfCommand.add(new BotCommand("/help", ""));
        listOfCommand.add(new BotCommand("/accounts", ""));
        listOfCommand.add(new BotCommand("/tariffs", ""));
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            var key = update.getMessage().getText();
            if (key.equals("/start")) {
                registrationUser(update.getMessage().getChat().getUserName(), chatId);
            }
            if (actionsCommand.containsKey(key)) {
                    var msg = actionsCommand.get(key).handleMessage(update, chatId);
                    if (key.equals("/help")) {
                        executeAsync((SendDocument) msg);
                        return;
                    }
                    executeAsync((SendPhoto) msg);
            }
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            var key = update.getCallbackQuery().getData();
            if (actionsKeyboard.containsKey(key)) {
                    var msg = actionsKeyboard.get(key).handleMessage(update, chatId);
                    if (key.equals("/help")) {
                        executeAsync((SendDocument) msg);
                        return;
                    }
                    if (key.contains("connect") ||
                            key.equals("/accounts") ||
                            key.contains("accountId-") ||
                            key.contains("back-") ||
                            key.contains("deleteAccountId-")) {
                        try {
                            executeAsync((SendMessage) msg);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    executeAsync((SendPhoto) msg);
            }
        }
    }

    private void registrationUser(String username, Long chatId) {
        User user = userService.getUser(chatId).orElseGet(() ->
        {
            User added = new User(username, chatId.toString());
            log.info("Added new user with name {} and id {}", username, chatId);
            return userService.saveUser(added);
        });
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

}

