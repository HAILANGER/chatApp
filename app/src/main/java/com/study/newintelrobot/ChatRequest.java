package com.study.newintelrobot;

public class ChatRequest {
    private String prompt;

    private String model;

    public ChatRequest(String model, String prompt) {
        this.model = model;
        this.prompt = prompt;
    }
}
