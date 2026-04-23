package com.example.aisales_backend.service.implement;

import com.example.aisales_backend.dto.chat.ChatRequestDto;
import com.example.aisales_backend.dto.chat.ChatResponseDto;
import com.example.aisales_backend.service.interfaces.IChatService;
import com.example.aisales_backend.service.n8n.N8nWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;


@Service
public class ChatService implements IChatService {


    private final N8nWebhookService n8nWebhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public ChatService(N8nWebhookService n8nWebhookService) {
        this.n8nWebhookService = n8nWebhookService;
    }


    @Override
    public ChatResponseDto ask(Long orderId, ChatRequestDto request) {
        String raw = n8nWebhookService.triggerForOrder(orderId, request.getQuestion());
// Try to parse a structured answer out of the webhook response if possible
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.has("answer")) {
                return new ChatResponseDto(node.get("answer").asText(), raw);
            }
        } catch (Exception e) {
// ignore parse errors — return raw
        }
        return new ChatResponseDto(raw, raw);
    }
}