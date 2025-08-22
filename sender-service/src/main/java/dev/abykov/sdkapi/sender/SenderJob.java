package dev.abykov.sdkapi.sender;

import dev.abykov.sdkapi.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SenderJob {

    private static final Logger LOG = LoggerFactory.getLogger(SenderJob.class);

    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final ApiClient client;

    public SenderJob(ApiClient client) {
        this.client = client;
    }

    @Scheduled(fixedRateString = "${sender.batch.fixed-rate-ms:5000}")
    public void sendBatch() {
        LOG.info("Sending batch...");
        for (int i = 0; i < 50; i++) {
            final int id = i;
            pool.submit(() -> sendOne(id));
        }
    }

    private void sendOne(int id) {
        try {
            String resp = client.sendMessage("Message " + id);
            LOG.info("Resp: {}", resp);
        } catch (Exception e) {
            LOG.error("Send failed for id={}", id, e);
        }
    }

    @PreDestroy
    void shutdown() {
        pool.shutdown();
    }
}
