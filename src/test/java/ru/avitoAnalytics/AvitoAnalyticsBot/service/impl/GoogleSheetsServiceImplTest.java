package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.GoogleSheetsInsertException;
import ru.avitoAnalytics.AvitoAnalyticsBot.service.GoogleSheetsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor
class GoogleSheetsServiceImplTest {

    private final GoogleSheetsService googleSheetsService;

    @Test
    void insertStatisticIntoTable() {
        Assertions.assertThrows(GoogleSheetsInsertException.class, () -> googleSheetsService.insertStatisticIntoTable(List.of(List.of()), "RANGE", "Id"));
    }
}