package com.example.aisales_backend.dto.chat;

public class ChatResponseDto {
    private String answer;
    private String raw;


    public ChatResponseDto() {}
    public ChatResponseDto(String answer, String raw) {
        this.answer = answer;
        this.raw = raw;
    }


    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }


    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }
}