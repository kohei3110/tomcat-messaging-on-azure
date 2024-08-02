package com.example;

public class SimpleMessageProvider implements MessageProvider {
    @Override
    public String getMessage() {
        return "Hello, Spring Framework!";
    }
}