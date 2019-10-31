package com.xpinjection.springboot.exception;

public class InvalidRecommendationException extends RuntimeException {
    public InvalidRecommendationException(String message) {
        super(message);
    }
}
