package dev.abykov.sdkapi.client;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ApiClient implements AutoCloseable {

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient http;
    private final CustomRateLimiter limiter;

    public ApiClient(
            String baseUrl,
            String apiKey,
            TimeUnit unit,
            int unitAmount,
            int limitPerWindow
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        this.limiter = new CustomRateLimiter(unit, unitAmount, limitPerWindow, true);
    }

    public String sendMessage(String payload) throws Exception {
        limiter.acquire();

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/messages"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "text/plain");

        if (apiKey != null && !apiKey.isBlank()) {
            reqBuilder.header("X-API-Key", apiKey);
        }

        HttpRequest req = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(payload)).build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
        }

        return resp.body();
    }

    @Override
    public void close() {
        limiter.shutdown();
    }
}
