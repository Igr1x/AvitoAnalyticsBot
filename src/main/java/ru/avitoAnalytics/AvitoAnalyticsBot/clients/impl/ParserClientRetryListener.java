package ru.avitoAnalytics.AvitoAnalyticsBot.clients.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import ru.avitoAnalytics.AvitoAnalyticsBot.clients.AstroProxyClient;
import ru.avitoAnalytics.AvitoAnalyticsBot.exceptions.ParserProxyException;

@Component("parserRetryListener")
@RequiredArgsConstructor
public class ParserClientRetryListener implements RetryListener {
    private final AstroProxyClient astroProxyClient;

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable instanceof ParserProxyException) {
            try {
                astroProxyClient.changeProxy();
            } catch (HttpServerErrorException.InternalServerError ignored) {

            }
        }
    }
}
