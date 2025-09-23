package org.calendar.exeptions;

public class NoUserFoundException extends BotCommandException {
    public NoUserFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}
