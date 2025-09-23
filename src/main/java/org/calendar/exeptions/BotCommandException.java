package org.calendar.exeptions;

public class BotCommandException extends RuntimeException {
    public BotCommandException(String errorMessage) {
        super(errorMessage);
    }

    public String getErrorMessage() {
        return super.getMessage();
    }
}
