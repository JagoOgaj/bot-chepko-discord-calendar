package org.calendar.exeptions;

public class CalendarAlreadySavedException extends BotCommandException {
    public CalendarAlreadySavedException(String message) {
        super(message);
    }

    @Override
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}
