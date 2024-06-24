package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BotButtons {
    public List<List<InlineKeyboardButton>> getStartButtons() {
        return createButtons(StartButtons.class);
    }

    public List<List<InlineKeyboardButton>> getHelpButtons() {
        return createButtons(HelpButtons.class);
    }

    public List<List<InlineKeyboardButton>> getTariffsButtons() {
        return createButtons(TariffsButtons.class);
    }

    public List<List<InlineKeyboardButton>> getBalanceButtons() {
        return createButtons(BalanceButtons.class);
    }

    public List<List<InlineKeyboardButton>> getAccountsButtons(int quantity, List<Long> accountsId) {
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            rowInLine.add(createButton("Аккаунт №" + (i + 1), "accountId-" + accountsId.get(i)));
            rowsInLine.add(rowInLine);
            rowInLine = new ArrayList<>();
        }
        rowsInLine.addAll(getHelpButtons());
        return rowsInLine;
    }

    public List<List<InlineKeyboardButton>> getSelectAccountButtons(long chatId, long accountId) {
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        rowInLine.add(createButton("Обработать отчёт", "handleReport-" + accountId));
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        rowInLine.add(createButton("Удалить", "deleteAccountId-" + accountId));
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        rowInLine.add(createButton("Назад", "back-" + chatId));
        rowsInLine.add(rowInLine);
        rowsInLine.addAll(getHelpButtons());
        return rowsInLine;
    }

    public List<List<InlineKeyboardButton>> getTariffButtons(String tariffId) {
        if (tariffId.equals(String.valueOf(1L))) return createButtons(TariffFirstButtons.class);
        if (tariffId.equals(String.valueOf(2L))) return createButtons(TariffSecondButtons.class);
        if (tariffId.equals(String.valueOf(3L))) return createButtons(TariffThirdButtons.class);
        return null;
    }


    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private <T extends Enum<T> & ButtonsInfo> List<List<InlineKeyboardButton>> createButtons(Class<T> enumClass) {
        try {
            List<InlineKeyboardButton> rowInLine = new ArrayList<>();
            List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
            Method valuesMethod = enumClass.getMethod("values");
            T[] enumValues = (T[]) valuesMethod.invoke(null);
            for (T enumValue : enumValues) {
                rowInLine.add(createButton(enumValue.getText(), enumValue.getCallbackData()));
                rowsInLine.add(rowInLine);
                rowInLine = new ArrayList<>();
            }
            return rowsInLine;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
