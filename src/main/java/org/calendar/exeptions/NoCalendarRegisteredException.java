package org.calendar.exeptions;

public class NoCalendarRegisteredException extends BotCommandException {
    public NoCalendarRegisteredException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}
