package org.calendar.services;

import org.calendar.entity.UserCalendarEntity;
import org.calendar.entity.UserEntity;
import org.calendar.exeptions.CalendarAlreadySavedException;
import org.calendar.exeptions.NoCalendarRegisteredException;
import org.calendar.exeptions.NoUserFoundException;
import org.calendar.repository.UserCalendarRepositoryImpl;
import org.calendar.repository.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class UserCalendarService {
    private final UserRepositoryImpl userRepository;
    private final UserCalendarRepositoryImpl userCalendarRepository;

    public UserCalendarService(
            UserRepositoryImpl userRepository, UserCalendarRepositoryImpl userCalendarRepository) {
        this.userRepository = userRepository;
        this.userCalendarRepository = userCalendarRepository;
    }

    public void saveCalendar(Long discordId, String username, int calendarId)
            throws CalendarAlreadySavedException, NoUserFoundException {
        UserEntity user =
                this.userRepository
                        .findByDiscordId(discordId)
                        .orElseGet(
                                () -> {
                                    this.userRepository.save(
                                            new UserEntity(null, discordId, username));

                                    return this.userRepository
                                            .findByDiscordId(discordId)
                                            .orElseThrow(
                                                    () ->
                                                            new NoUserFoundException(
                                                                    "User pas trouvée avec cette id "
                                                                            + discordId));
                                });

        Optional<UserCalendarEntity> existing =
                this.userCalendarRepository.findByUserAndCalendar(user.userId(), calendarId);

        if (existing.isPresent()) {
            throw new CalendarAlreadySavedException("Ce calendrier est déjà eregistrée");
        }
        this.userCalendarRepository.save(new UserCalendarEntity(user.userId(), calendarId));
    }

    public void unsaveCalendar(Long discordId, int calendarId)
            throws NoCalendarRegisteredException {
        Optional<UserEntity> userOpt = this.userRepository.findByDiscordId(discordId);
        if (userOpt.isEmpty())
            throw new NoCalendarRegisteredException(
                    "Tu ne peut pas unsave un calendrier pas enregistré");

        UserEntity user = userOpt.get();

        this.userCalendarRepository.deleteByUserAndCalendar(user.userId(), calendarId);

        List<UserCalendarEntity> remaining =
                this.userCalendarRepository.findByUserId(user.userId());
        if (remaining.isEmpty()) {
            this.userRepository.delete(user.userId());
        }
    }
}
