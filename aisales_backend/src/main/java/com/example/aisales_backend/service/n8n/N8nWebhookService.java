package com.example.aisales_backend.service.n8n;

import com.example.aisales_backend.entity.Call;
import com.example.aisales_backend.repository.CallRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
@Slf4j
public class N8nWebhookService {
    private final N8nClient n8nClient;
    private final CallRepository callRepo;

    public N8nWebhookService(N8nClient n8nClient, CallRepository callRepo) {
        this.n8nClient = n8nClient;
        this.callRepo = callRepo;
    }


    public String triggerForOrder(Long orderId, String question) {
        Call rec = callRepo.findTopByOrder_IdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No call recording for order: " + orderId));

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        payload.put("question", question);
//        payload.put("transcript", rec.getTranscript());

        ResponseEntity<String> resp = n8nClient.triggerWebhook("", payload);

        log.info("Raw n8n response: {}", resp.getBody());

        if (!resp.getStatusCode().is2xxSuccessful()) {
            log.error("N8n webhook returned error status: {}", resp.getStatusCode());
            throw new RuntimeException("Error response from n8n webhook");
        }

        String responseBody = resp.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Null response body from n8n webhook");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseBody);

            // n8n wraps response inside $json.output
            JsonNode outputNode = rootNode.path("$json").path("output");

            if (outputNode.has("success") && outputNode.get("success").asBoolean(false)) {
                if (outputNode.has("answer")) {
                    String text = outputNode.get("answer").asText();
                    log.info("Extracted response text: {}", text);
                    return text;
                } else {
                    log.error("JSON missing 'answer': {}", responseBody);
                    throw new RuntimeException("No 'answer' field in n8n response");
                }
            } else {
                log.error("n8n returned unsuccessful response: {}", responseBody);
                throw new RuntimeException("n8n webhook indicated failure");
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response: {}", responseBody, e);
            throw new RuntimeException("Failed to parse response from n8n webhook", e);
        }


    }

}
