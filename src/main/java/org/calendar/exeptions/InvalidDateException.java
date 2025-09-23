package org.calendar.exeptions;

public class InvalidDateException extends BotCommandException {
    public InvalidDateException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}
