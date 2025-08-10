package dev.abykov.sender;

import dev.abykov.sdk.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class SenderConfig {

    @Bean
    public ApiClient apiClient(
            @Value("${sdk.base-url:http://localhost:8080}") String baseUrl,
            @Value("${sdk.api-key:}") String apiKey,
            @Value("${sdk.window.unit:SECONDS}") TimeUnit unit,
            @Value("${sdk.window.amount:1}") int unitAmount,
            @Value("${sdk.limit-per-window:5}") int limit
    ) {
        return new ApiClient(baseUrl, apiKey, unit, unitAmount, limit);
    }
}
