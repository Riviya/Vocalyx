package com.example.aisales_backend.controller;

import com.example.aisales_backend.dto.*;
import com.example.aisales_backend.dto.history.CallDto;
import com.example.aisales_backend.service.CallService;
import com.example.aisales_backend.service.interfaces.ICallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final CallService callService;
    private final RestTemplate restTemplate;
    private final ICallService callHistory;


    @Value("${n8n.webhook.url:http://localhost:5678/webhook/5804a9a4-e172-482f-b9e0-064058ad7d86}")
    private String n8nWebhookUrl;

    // Upload call metadata + file and trigger N8N analysis
    @PostMapping
    public ResponseEntity<CallResponse> createCall(@RequestBody CallRequest request) {
        CallResponse response = callService.saveCall(request);

        try {
            // Send actual file to N8N
            File file = new File(request.getRecordingFilePath());
            if (!file.exists()) {
                log.error("Audio file not found at {}", request.getRecordingFilePath());
                throw new RuntimeException("Audio file not found at " + request.getRecordingFilePath());
            }

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            body.add("callId", response.getId());
            body.add("webhookUrl", "http://host.docker.internal:8080/api/calls/public-webhook/" + response.getId());      //changed fetch data url because n8n is running inside docker


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(n8nWebhookUrl, entity, String.class);
            log.info("Triggered N8N workflow for callId {}", response.getId());

        } catch (Exception e) {
            log.error("Failed to trigger N8N webhook for callId {}: {}", response.getId(), e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{callId}/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @PathVariable Long callId,
            @RequestBody Object webhookPayload) {

        log.info("Received webhook for callId {} with payload: {}", callId, webhookPayload);

        Map<String, Object> output = extractOutput(webhookPayload);

        if (output == null) {
            log.error("Failed to extract 'output' from payload for callId {}", callId);
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'output' field"));
        } else {
            log.info("Extracted output for callId {}: {}", callId, output);
        }

        WebhookResponse webhookResponse = buildWebhookResponse(output);
        log.info("Built webhook response: {}", webhookResponse);

        try {
            callService.updateWithWebhook(callId, webhookResponse);
            log.info("Successfully updated call {} with webhook data", callId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "callId", callId,
                    "message", "Call analysis completed successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to update call {} with webhook data: ", callId, e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update call with webhook data: " + e.getMessage()));
        }
    }

    // Public webhook endpoint (no authentication required) for N8N
    @PostMapping("/public-webhook/{callId}")
    public ResponseEntity<Map<String, Object>> handlePublicWebhook(
            @PathVariable Long callId,
            @RequestBody Object webhookPayload) {

        log.info("Received public webhook for callId {} with payload: {}", callId, webhookPayload);

        Map<String, Object> output = extractOutput(webhookPayload);

        if (output == null) {
            log.error("Failed to extract 'output' from payload for callId {}", callId);
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'output' field"));
        } else {
            log.info("Extracted output for callId {}: {}", callId, output);
        }

        WebhookResponse webhookResponse = buildWebhookResponse(output);
        log.info("Built webhook response: {}", webhookResponse);

        try {
            callService.updateWithWebhook(callId, webhookResponse);
            log.info("Successfully updated call {} with webhook data", callId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "callId", callId,
                    "message", "Call analysis completed successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to update call {} with webhook data: ", callId, e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update call with webhook data: " + e.getMessage()));
        }
    }


    // Get call by ID
    @GetMapping("/{callId}")
    public ResponseEntity<CallResponse> getCall(@PathVariable Long callId) {
        CallResponse call = callService.getCallById(callId);
        log.info("Fetching call {} → {}", callId, call);
        return ResponseEntity.ok(call);
    }

    // Test webhook (manual)
    @PostMapping("/test-webhook/{callId}")
    public ResponseEntity<Map<String, Object>> testWebhook(@PathVariable Long callId) {
        WebhookResponse testWebhook = WebhookResponse.builder()
                .transcript("Test transcript")
                .summary("Test summary")
                .sentimentPercentage(85)
                .sentimentLabel("POSITIVE")
                .build();
        CallResponse updatedCall = callService.updateWithWebhook(callId, testWebhook);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "callId", callId,
                "message", "Test webhook completed",
                "updatedCall", updatedCall
        ));
    }

    // Test webhook with your actual N8N response format
    @PostMapping("/test-n8n-webhook/{callId}")
    public ResponseEntity<Map<String, Object>> testN8nWebhook(@PathVariable Long callId) {
        try {
            // Simulate the exact N8N response format you provided
            Object n8nPayload = List.of(Map.of(
                    "output", Map.of(
                            "sentimentLabel", "Extremely Positive",
                            "sentimentPercentage", 98,
                            "summary", "The client is extremely satisfied with the service, highlighting the quick setup and helpful support team as key positive aspects of their experience.",
                            "transcript", "Hello, this is John from ABC Solutions.\\n[ 0m3s477ms ] I wanted to thank you for choosing our services last week.\\n[ 0m7s247ms ] How was your experience so far?\\n[ 0m10s527ms ] Oh, hi John.\\n[ 0m11s647ms ] Thanks for checking in.\\n[ 0m13s547ms ] Honestly, I am very happy with the service.\\n[ 0m16s347ms ] The setup was quick and your support team was very helpful.\\n[ 0m19s837ms ] That's great to hear.\\n[ 0m21s197ms ] We'll keep in touch to make sure everything continues smoothly.\\n[ 0m24s877ms ] Have a nice day."
                    )
            ));

            return handlePublicWebhook(callId, n8nPayload);
        } catch (Exception e) {
            log.error("Error in test webhook: ", e);
            return ResponseEntity.status(500).body(Map.of("error", "Test webhook failed: " + e.getMessage()));
        }
    }

    // Simple debug endpoint
    @PostMapping("/debug-webhook/{callId}")
    public ResponseEntity<Map<String, Object>> debugWebhook(@PathVariable Long callId, @RequestBody Object payload) {
        try {
            log.info("Debug webhook received for callId {} with payload: {}", callId, payload);

            Map<String, Object> output = extractOutput(payload);
            log.info("Extracted output: {}", output);

            if (output == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Could not extract output from payload"));
            }

            WebhookResponse webhookResponse = buildWebhookResponse(output);
            log.info("Built webhook response: {}", webhookResponse);

            callService.updateWithWebhook(callId, webhookResponse);
            log.info("Successfully updated call {} with webhook data", callId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "callId", callId,
                    "message", "Debug webhook completed successfully"
            ));
        } catch (Exception e) {
            log.error("Error in debug webhook: ", e);
            return ResponseEntity.status(500).body(Map.of("error", "Debug webhook failed: " + e.getMessage()));
        }
    }

    // Get calls by order number
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<List<CallResponse>> getCallsByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(callService.getCallsByOrderNumber(orderNumber));
    }

    // Get calls by contact ID
    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<CallResponse>> getCallsByContact(@PathVariable Long contactId) {
        return ResponseEntity.ok(callService.getCallsByContact(contactId));
    }

    // --- Helper methods ---
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractOutput(Object webhookPayload) {
        if (webhookPayload instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> map) {
                Object outputObj = map.get("output");
                if (outputObj instanceof Map<?, ?> outputMap) {
                    return (Map<String, Object>) outputMap;
                }
                Object jsonObj = map.get("json");
                if (jsonObj instanceof Map<?, ?> jsonMap) {
                    return (Map<String, Object>) jsonMap;
                }
            }
        } else if (webhookPayload instanceof Map<?, ?> map) {
            Object outputObj = map.get("output");
            if (outputObj instanceof Map<?, ?> outputMap) {
                return (Map<String, Object>) outputMap;
            }

            Object jsonObj = map.get("json");
            if (jsonObj instanceof Map<?, ?> jsonMap) {
                return (Map<String, Object>) jsonMap;
            }
        }
        return null;
    }


    private WebhookResponse buildWebhookResponse(Map<String, Object> output) {

        if (output == null) {
            log.error("Webhook output is null");
            return WebhookResponse.builder().build();
        }

        String transcript = (String) output.get("transcript");
        String summary = (String) output.get("summary");
        Object rawPercentage = output.get("sentimentPercentage");
        String sentimentLabel = (String) output.get("sentimentLabel");

        log.info("Extracted fields -> transcript: {}, summary: {}, sentimentPercentage: {}, sentimentLabel: {}",
                transcript, summary, rawPercentage, sentimentLabel);

        Integer percentage = null;
        if (rawPercentage instanceof Number num) {
            percentage = num.intValue();
        }

        WebhookResponse response = WebhookResponse.builder()
                .transcript(transcript)
                .summary(summary)
                .sentimentPercentage(percentage)
                .sentimentLabel(sentimentLabel)
                .build();

        log.info("Built WebhookResponse: {}", response);
        return response;
    }

    @PostMapping("/test-simple-webhook/{callId}")
    public ResponseEntity<Map<String, Object>> testSimpleWebhook(@PathVariable Long callId, @RequestBody Map<String, Object> payload) {
        try {
            log.info("Simple webhook received for callId {} with payload: {}", callId, payload);

            // Simple response for testing
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("callId", callId);
            response.put("message", "Simple webhook test successful");
            response.put("receivedData", payload);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in simple webhook: ", e);
            return ResponseEntity.status(500).body(Map.of("error", "Simple webhook failed: " + e.getMessage()));
        }

    }


    @GetMapping("/history")
    public ResponseEntity<List<CallDto>> getAllCallsLatestFirst() {
        return ResponseEntity.ok(callHistory.getAllCallsLatestFirst());
    }


}