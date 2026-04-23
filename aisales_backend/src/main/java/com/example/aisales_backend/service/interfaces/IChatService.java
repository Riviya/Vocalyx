package com.example.aisales_backend.service.interfaces;


import com.example.aisales_backend.dto.chat.ChatRequestDto;
import com.example.aisales_backend.dto.chat.ChatResponseDto;

public interface IChatService {
    ChatResponseDto ask(Long orderId, ChatRequestDto request);
}