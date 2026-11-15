package com.rit.medibook;

public class BookingException extends RuntimeException {
    private final int status;

    public BookingException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
