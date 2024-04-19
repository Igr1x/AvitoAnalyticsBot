package ru.avitoAnalytics.AvitoAnalyticsBot.util;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class AvitoParserSelenium {

    public static List<String> getDataForTable(String link) throws InterruptedException {
        List<String> forTable = new ArrayList<>(List.of("другой регион"));

        System.setProperty("webdriver.chrome.driver", "selenium\\geckodriver.exe");
        WebDriver driver = new FirefoxDriver();

        driver.get(link);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        try {
            driver.get(link);

            List<WebElement> addressElements = driver.findElements(By.cssSelector("[itemtype='http:\\/\\/schema\\.org\\/PostalAddress'] span[class='style-item-address__string-wt61A']"));

            for (WebElement element : addressElements) {
                String addr = element.getText().replaceAll("обл.", "область");
                forTable.addAll(Arrays.asList(addr.split(", ")));
            }

            List<WebElement> breadcrumbs = driver.findElements(By.cssSelector("[itemtype='http:\\/\\/schema\\.org\\/BreadcrumbList'] [itemprop='name']"));
            for (WebElement breadcrumb : breadcrumbs) {
                forTable.add(breadcrumb.getText());
            }

            WebElement stateElement = driver.findElement(By.xpath("//li[contains(@class, 'params-paramsList__item-_2Y2O') and .//span[contains(text(), 'Состояние')]]"));
            String state = stateElement.getText();
            String[] parts = state.split(":");
            if (parts.length > 1) {
                state = parts[1].trim();
            }
            forTable.add(state);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        //Thread.sleep(20000L);
        return forTable;
    }
}
