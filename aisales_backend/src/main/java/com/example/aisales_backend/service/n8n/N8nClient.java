package com.example.aisales_backend.service.n8n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class N8nClient {

    private final RestTemplate restTemplate = new RestTemplate();

    // This should be the full production URL of your n8n webhook
    @Value("${vocalyx.n8n.webhook-url}")
    private String n8nWebhookUrl;

    /**
     * Trigger the n8n webhook.
     *
     * @param webhookPath Optional additional path (usually empty if using full URL)
     * @param payload    JSON payload to send
     * @return ResponseEntity with the JSON response
     */
    public ResponseEntity<String> triggerWebhook(String webhookPath, Map<String, Object> payload) {
        // If webhookPath is empty, use the base URL as-is
        String url = (webhookPath == null || webhookPath.isEmpty()) ? n8nWebhookUrl : joinUrl(n8nWebhookUrl, webhookPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        return restTemplate.postForEntity(url, entity, String.class);
    }

    /**
     * Join base URL and path correctly.
     */
    private String joinUrl(String base, String path) {
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (!path.startsWith("/")) path = "/" + path;
        return base + path;
    }
}
