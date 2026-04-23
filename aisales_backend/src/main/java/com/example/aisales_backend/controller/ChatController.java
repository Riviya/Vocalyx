package com.example.aisales_backend.controller;


import com.example.aisales_backend.dto.chat.ChatRequestDto;
import com.example.aisales_backend.dto.chat.ChatResponseDto;
import com.example.aisales_backend.service.interfaces.IChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/chat")
public class ChatController {


    private final IChatService chatService;


    public ChatController(IChatService chatService) {
        this.chatService = chatService;
    }


    @PostMapping("/{orderId}/ask")
    public ResponseEntity<ChatResponseDto> ask(@PathVariable Long orderId, @RequestBody ChatRequestDto request) {
        ChatResponseDto resp = chatService.ask(orderId, request);
        return ResponseEntity.ok(resp);
    }
}