package com.example.blockchain.record.keeping.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class MicrosoftTranslator {
    private static final String API_KEY = EnvUtil.get("MICROSOFT_TRANSLATOR_API_KEY");
    private static final String ENDPOINT = "https://api.cognitive.microsofttranslator.com";
    private static final String LOCATION = "eastus";

    public static String translate(String text, String fromLang, String toLang) throws Exception {
        String url = ENDPOINT + "/translate?api-version=3.0&from=" + fromLang + "&to=" + toLang;

        HttpClient client = HttpClient.newHttpClient();

        String requestBody = "[{ \"Text\": \"" + text + "\" }]";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Ocp-Apim-Subscription-Key", API_KEY)
                .header("Ocp-Apim-Subscription-Region", LOCATION)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> list = mapper.readValue(response.body(), List.class);
        Map<String, Object> translation = (Map<String, Object>) ((List<?>) list.get(0).get("translations")).get(0);
        return (String) translation.get("text");
    }
}
