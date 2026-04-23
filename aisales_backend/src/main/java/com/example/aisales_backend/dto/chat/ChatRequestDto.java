package com.example.aisales_backend.dto.chat;

public class ChatRequestDto {
    private String userId;
    private String question;


    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }


    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}