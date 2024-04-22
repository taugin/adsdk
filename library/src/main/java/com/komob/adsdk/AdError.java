package com.komob.adsdk;

public class AdError {
    private String message;

    private AdError(String message) {
        this.message = message;
    }

    public static AdError valueOf(String message) {
        return new AdError(message);
    }

    @Override
    public String toString() {
        return message;
    }
}
