package org.calendar;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import org.calendar.commands.*;
import org.calendar.context.EventContext;
import org.calendar.database.Database;
import org.calendar.listeners.SlashCommandListener;
import org.calendar.manager.SlashCommandManager;
import org.calendar.mapper.SlashCommandMapper;
import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.*;
import org.calendar.repository.*;
import org.calendar.scheduler.DailyTaskScheduler;
import org.calendar.scheduler.UpdateAlertScheduler;
import org.calendar.services.*;
import org.calendar.task.CalendarUpdateTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        EventContext context = new EventContext();

        Database.init(
                System.getenv("URL_DB"),
                System.getenv("USER_DB"),
                System.getenv("PASSWORD_DB")
        );

        CalendarRepositoryImpl calendarRepository = new CalendarRepositoryImpl();
        EventRepositoryImpl eventRepository = new EventRepositoryImpl();
        UserCalendarRepositoryImpl userCalendarRepository = new UserCalendarRepositoryImpl();
        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        EventNotificationRepositoryImpl eventNotificationRepository =
                new EventNotificationRepositoryImpl();
        ServerConfigNotificationRepositoryImpl serverConfigNotificationRepository =
                new ServerConfigNotificationRepositoryImpl();
        ServerUpdateAlertRepositoryImpl serverUpdateAlertRepository =
                new ServerUpdateAlertRepositoryImpl();

        HttpClientService httpClientService = new HttpClientService();
        CalendarService calendarService =
                new CalendarService(httpClientService, eventRepository, calendarRepository);
        EventService eventService = new EventService(eventRepository);
        UserCalendarService userCalendarService =
                new UserCalendarService(userRepository, userCalendarRepository);

        SlashCommandMiddleware icsValidationMiddleware =
                new IcsValidationMiddleware("ics-url", calendarService, context);
        SlashCommandMiddleware permissionMiddleware = new PermissionMiddleware();
        SlashCommandMiddleware configExistenceMiddleware =
                new ConfigExistenceMiddleware(serverConfigNotificationRepository);
        SlashCommandMiddleware disableUpdateMiddleware =
                new DisableUpdateAlertsMiddleware(serverUpdateAlertRepository);
        SlashCommandMiddleware enableUpdateAlertsMiddleware =
                new EnableUpdateAlertsMiddleware(serverUpdateAlertRepository);
        SlashCommandMiddleware calendarExistenceMiddleware =
                new CalendarExistenceMiddleware(context, calendarService);
        SlashCommandMiddleware dateValidationMiddleware =
                new DateValidationMiddleware("date-of", context);
        SlashCommandMiddleware setupNotificationMiddleware =
                new SetupNotificationMiddleware("minute", "channel-id", calendarService);

        SlashCommandHandler registerCalendarSlashCommand =
                new RegisterCalendarSlashCommand(
                        new ArrayList<>(List.of(icsValidationMiddleware)),
                        new SlashCommandMeta(
                                "register-calendar",
                                "Enregistre ou met à jour le calendrier du serveur",
                                List.of(
                                        new OptionData(
                                                OptionType.STRING,
                                                "ics-url",
                                                "Lien du fichier ICS",
                                                true),
                                        new OptionData(
                                                OptionType.STRING,
                                                "calendar-name",
                                                "Nom du calendrier",
                                                true))),
                        calendarService,
                        context,
                        eventService
                        );

        SlashCommandHandler disableNotificationSlashCommand =
                new DisableNotificationSlashCommand(
                        new ArrayList<>(List.of(permissionMiddleware, configExistenceMiddleware)),
                        new SlashCommandMeta(
                                "disable-event-alert",
                                "Désactive les alertes d’événements pour ce serveur",
                                List.of()),
                        eventNotificationRepository,
                        serverConfigNotificationRepository);

        SlashCommandHandler disableUpdateSlashCommand =
                new DisableUpdateAlertSlashCommand(
                        new ArrayList<>(List.of(permissionMiddleware, disableUpdateMiddleware)),
                        new SlashCommandMeta(
                                "disable-update-calendar-alert",
                                "Désactive les alertes de mise à jour du calendrier",
                                List.of()),
                        serverUpdateAlertRepository);

        SlashCommandHandler enableUpdateAlertSlashCommand =
                new EnableAlertUdpateSlashCommand(
                        new ArrayList<>(
                                List.of(
                                        permissionMiddleware,
                                        enableUpdateAlertsMiddleware,
                                        configExistenceMiddleware)),
                        new SlashCommandMeta(
                                "enable-event-alert-update",
                                "Active les alertes d’événements pour ce serveur (nécessite /config-alert)",
                                List.of()),
                        serverUpdateAlertRepository);

        SlashCommandHandler nextEventSlashCommand =
                new NextEventSlashCommand(
                        new ArrayList<>(
                                List.of(dateValidationMiddleware, calendarExistenceMiddleware)),
                        new SlashCommandMeta(
                                "next-event",
                                "Retourne les prochains événements pour une date donnée",
                                List.of(
                                        new OptionData(
                                                OptionType.STRING,
                                                "date-of",
                                                "Date au format `JJ/MM/AAAA`",
                                                true))),
                        eventService,
                        calendarService,
                        context);

        SlashCommandHandler saveCalendarSlashCommand =
                new SaveCalendarSlashCommand(
                        new ArrayList<>(List.of(calendarExistenceMiddleware)),
                        new SlashCommandMeta(
                                "save-calendar",
                                "Sauvegarde le calendrier du serveur pour l’utiliser sur le site",
                                List.of()),
                        userCalendarService,
                        calendarService);

        SlashCommandHandler setupNotificationSlashCommand =
                new SetupNotificationSlashCommand(
                        new ArrayList<>(
                                List.of(
                                        permissionMiddleware,
                                        setupNotificationMiddleware,
                                        calendarExistenceMiddleware)),
                        serverConfigNotificationRepository,
                        new SlashCommandMeta(
                                "config-alert",
                                "Configure ou modifie les parametres pour alertes notif",
                                List.of(
                                        new OptionData(
                                                OptionType.CHANNEL,
                                                "channel-id",
                                                "Salon où envoyer l’alerte",
                                                true),
                                        new OptionData(
                                                OptionType.INTEGER,
                                                "minute",
                                                "Minutes avant l’événement (pour alerte de type prochain événemets)",
                                                true))));

        SlashCommandHandler showCalendar =
                new ShowCalendarSlashCommand(
                        new ArrayList<>(List.of(calendarExistenceMiddleware)),
                        new SlashCommandMeta(
                                "show-calendar",
                                "Affiche le calendrier actuellement utilisé",
                                List.of()),
                        context);

        SlashCommandHandler todayEventSlashCommand =
                new TodayEventSlashCommand(
                        new ArrayList<>(List.of(calendarExistenceMiddleware)),
                        new SlashCommandMeta(
                                "today-event",
                                "Affiche les événements restants d’aujourd’hui",
                                List.of()),
                        eventService,
                        calendarService);

        SlashCommandHandler unSaveCalendarSlashCommand =
                new UnSaveCalendarSlashCommand(
                        new ArrayList<>(List.of(calendarExistenceMiddleware)),
                        new SlashCommandMeta(
                                "unsave",
                                "Supprime le calendrier sauvegardé pour ce serveur sur le site",
                                List.of()),
                        userCalendarService,
                        calendarService);

        SlashCommandManager slashCommandManager =
                new SlashCommandManager(
                        Map.of(
                                registerCalendarSlashCommand.getCommandMeta().commandName(),
                                registerCalendarSlashCommand,
                                disableNotificationSlashCommand.getCommandMeta().commandName(),
                                disableNotificationSlashCommand,
                                disableUpdateSlashCommand.getCommandMeta().commandName(),
                                disableUpdateSlashCommand,
                                enableUpdateAlertSlashCommand.getCommandMeta().commandName(),
                                enableUpdateAlertSlashCommand,
                                nextEventSlashCommand.getCommandMeta().commandName(),
                                nextEventSlashCommand,
                                saveCalendarSlashCommand.getCommandMeta().commandName(),
                                saveCalendarSlashCommand,
                                setupNotificationSlashCommand.getCommandMeta().commandName(),
                                setupNotificationSlashCommand,
                                showCalendar.getCommandMeta().commandName(),
                                showCalendar,
                                todayEventSlashCommand.getCommandMeta().commandName(),
                                todayEventSlashCommand,
                                unSaveCalendarSlashCommand.getCommandMeta().commandName(),
                                unSaveCalendarSlashCommand),
                        new SlashCommandMapper());

        JDA jda =
                JDABuilder.createDefault(System.getenv("TOKEN_BOT"))
                        .addEventListeners(new SlashCommandListener(slashCommandManager))
                        .build();

        slashCommandManager.registerAllCommandsToJDA(jda);

        NotificationService notificationService =
                new NotificationService(
                        serverConfigNotificationRepository,
                        eventRepository,
                        eventNotificationRepository,
                        calendarService,
                        jda);

        DailyTaskScheduler dailyScheduler = new DailyTaskScheduler();
        dailyScheduler.start(notificationService::scheduleTodayNotifications);

        CalendarUpdateTask calendarUpdateTask =
                new CalendarUpdateTask(
                        calendarService,
                        eventRepository,
                        serverUpdateAlertRepository,
                        notificationService);

        UpdateAlertScheduler updateAlertScheduler = new UpdateAlertScheduler();
        updateAlertScheduler.start(calendarUpdateTask);

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    dailyScheduler.shutdown();
                                    updateAlertScheduler.shutdown();
                                }));
    }
}
