package dev.abykov.sdkapi.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

    @PostMapping
    public String receive(@RequestBody String body) {
        LOG.info("Received: {}", body);
        return "OK";
    }
}
