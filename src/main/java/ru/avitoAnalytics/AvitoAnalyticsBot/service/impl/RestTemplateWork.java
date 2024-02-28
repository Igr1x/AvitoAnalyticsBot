package ru.avitoAnalytics.AvitoAnalyticsBot.service.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RestTemplateWork {

    public MultiValueMap<String, String> getParams(String perPage, String page, String statusAdvertisement) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("per_page", perPage);
        map.add("page", page);
        map.add("status", statusAdvertisement);
        return map;
    }

    public ResponseEntity<String> getResponse(String URL, MultiValueMap<String, String> map,
                                               HttpEntity<MultiValueMap<String, String>> request) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(URL).queryParams(map);
        UriComponents components = builder.build().encode();
        return restTemplate.exchange(components.toUri(), HttpMethod.GET, request, String.class);
    }

}
