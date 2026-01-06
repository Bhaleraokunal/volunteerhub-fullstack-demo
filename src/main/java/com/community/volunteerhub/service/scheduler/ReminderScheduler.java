package com.community.volunteerhub.service.scheduler;

import com.community.volunteerhub.entity.EventDetails;
import com.community.volunteerhub.entity.RegistrationDetails;
import com.community.volunteerhub.enums.RegistrationStatus;
import com.community.volunteerhub.repository.EventDetailsRepository;
import com.community.volunteerhub.repository.RegistrationDetailsRepository;
import com.community.volunteerhub.service.UserService;
import com.community.volunteerhub.service.emailService.EmailService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReminderScheduler {

    private final EventDetailsRepository eventRepo;
    private final RegistrationDetailsRepository registrationRepo;
    private final EmailService emailService;
    private final UserService userService;

    public ReminderScheduler(EventDetailsRepository eventRepo,
                             RegistrationDetailsRepository registrationRepo,
                             EmailService emailService,
                             UserService userService) {
        this.eventRepo = eventRepo;
        this.registrationRepo = registrationRepo;
        this.emailService = emailService;
        this.userService = userService;
    }

    // ------------ RUNS EVERY DAY AT 9AM ----------------
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendReminders() {

        LocalDate today = LocalDate.now();

        sendReminderForDate(today.plusDays(3), "Reminder: Event in 3 Days");
        sendReminderForDate(today.plusDays(1), "Reminder: Event Tomorrow");
        sendReminderForDate(today, "Today is the Event Day!");
    }


    private void sendReminderForDate(LocalDate date, String subject) {

        List<EventDetails> events = eventRepo.findByEventStartDate(date);

        for (EventDetails event : events) {

            List<RegistrationDetails> regs =
                    registrationRepo.findByEventIdAndStatus(
                            event.getEventId(),
                            RegistrationStatus.REGISTERED);

            for (RegistrationDetails reg : regs) {

                String volunteerEmail = reg.getVolunteerId();

                var volunteer = userService.getProfile(volunteerEmail);
                var organizer = userService.getProfile(event.getOrganizerId());

                Map<String,Object> model = new HashMap<>();
                model.put("volunteerName", volunteer.getEmailId());
                model.put("eventName", event.getEventName());
                model.put("eventDate", event.getEventStartDate().toString());
                model.put("eventCity", event.getCity());
                model.put("organizerName", organizer.getEmailId());

                try {
                    emailService.sendEmail(
                            volunteerEmail,
                            subject,
                            "reminder.ftl",
                            model,
                            event.getOrganizerId(),     // reply-to
                            organizer.getEmailId()      // from-name
                    );

                } catch (Exception ex) {
                    System.out.println("Failed to send reminder email to " + volunteerEmail 
                            + " for event " + event.getEventName() 
                            + " : " + ex.getMessage());
                }

            }
        }
    }
}
