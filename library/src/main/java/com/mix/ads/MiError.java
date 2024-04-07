package com.mix.ads;

public class MiError {
    private String message;

    private MiError(String message) {
        this.message = message;
    }

    public static MiError valueOf(String message) {
        return new MiError(message);
    }

    @Override
    public String toString() {
        return "message : " + message;
    }
}
