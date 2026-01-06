package com.community.volunteerhub.service.registration;

import com.community.volunteerhub.entity.EventDetails;
import com.community.volunteerhub.entity.RegistrationDetails;
import com.community.volunteerhub.entity.RegistrationId;
import com.community.volunteerhub.enums.RegistrationStatus;
import com.community.volunteerhub.repository.EventDetailsRepository;
import com.community.volunteerhub.repository.RegistrationDetailsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.volunteerhub.service.emailService.EmailService;

@Service
public class RegistrationService {

    private final RegistrationDetailsRepository registrationRepo;
    private final EventDetailsRepository eventRepo;
    private final EmailService emailService;

    public RegistrationService(RegistrationDetailsRepository registrationRepo,
            EventDetailsRepository eventRepo,
            EmailService emailService) {
				this.registrationRepo = registrationRepo;
				this.eventRepo = eventRepo;
				this.emailService = emailService;
				}


    @Transactional
    public void registerForEvent(String volunteerEmail, Integer eventId) {

        EventDetails event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getRegistrationAllowed()) {
            throw new IllegalStateException("Registrations are closed");
        }

        RegistrationId id = new RegistrationId(volunteerEmail, eventId);

        // Either load existing record or create a new one
        RegistrationDetails registration = registrationRepo.findById(id)
                .orElse(new RegistrationDetails());

        // If already registered → block
        if (registration.getStatus() == RegistrationStatus.REGISTERED) {
            throw new IllegalStateException("Already registered");
        }

        // Capacity check (only counts active registrations)
        long count = registrationRepo.countByEventIdAndStatus(
                eventId, RegistrationStatus.REGISTERED);

        if (count >= event.getMaxAllowedRegistrations()) {
            throw new IllegalStateException("Registration limit reached");
        }

        registration.setVolunteerId(volunteerEmail);
        registration.setEventId(eventId);

        // Reactivate and reset state
        registration.setStatus(RegistrationStatus.REGISTERED);
        registration.setCheckIn(false);
        registration.setRating(null);

        registrationRepo.save(registration);

        // EMAIL — keep your existing logic
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("organizerName", event.getOrganizerId());
            model.put("volunteerName", volunteerEmail);
            model.put("eventName", event.getEventName());
            model.put("eventDate", event.getEventStartDate().toString());

            emailService.sendEmail(
                    event.getOrganizerId(),
                    "New Volunteer Registration",
                    "register.ftl",
                    model,
                    event.getOrganizerId(),
                    event.getOrganizerId()
            );

        } catch (Exception ex) {
            System.out.println("Failed to send registration email: " + ex.getMessage());
        }
    }


    
    @Transactional(readOnly = true)
    public List<RegistrationDetails> getMyRegistrations(String email) {
        return registrationRepo.findByVolunteerIdAndStatus(
                email,
                RegistrationStatus.REGISTERED
        );
    }



    @Transactional(readOnly = true)
    public List<String> getRegistrations(Integer eventId) {
        return registrationRepo.findVolunteerIdByEventIdAndStatus(eventId);
    }

    @Transactional(readOnly = true)
    public List<String> getParticipants(Integer eventId) {
        return registrationRepo.findCheckedInVolunteers(eventId);
    }

    @Transactional
    public void submitFeedback(String email, Integer eventId, Float rating) {

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        RegistrationId id = new RegistrationId(email, eventId);

        RegistrationDetails registration = registrationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not registered"));

        if (!Boolean.TRUE.equals(registration.getCheckIn())) {
            throw new IllegalStateException("Feedback allowed only after check-in");
        }

        registration.setRating(rating);
        registrationRepo.save(registration);
    }

    
    @Transactional
    public void unregisterFromEvent(String volunteerEmail, Integer eventId) {

        RegistrationId id = new RegistrationId(volunteerEmail, eventId);

        RegistrationDetails registration = registrationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // OPTIONAL RULE:
        // If you want to block unregister after check-in, UNCOMMENT:
        //
        // if (Boolean.TRUE.equals(registration.getCheckIn())) {
        //     throw new IllegalStateException("Cannot unregister after check-in");
        // }

        registration.setStatus(RegistrationStatus.WITHDRAWN);

        // IMPORTANT — reset state
        registration.setCheckIn(false);
        registration.setRating(null);

        registrationRepo.save(registration);

        EventDetails event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("organizerName", event.getOrganizerId());
            model.put("volunteerName", volunteerEmail);
            model.put("eventName", event.getEventName());

            emailService.sendEmail(
                    event.getOrganizerId(),
                    "Volunteer Unregistered",
                    "unregister.ftl",
                    model,
                    event.getOrganizerId(),
                    event.getOrganizerId()
            );

        } catch (Exception ex) {
            System.out.println("Failed to send unregister email: " + ex.getMessage());
        }
    }

    
    @Transactional
    public void checkIn(String volunteerEmail, Integer eventId) {

        RegistrationId id = new RegistrationId(volunteerEmail, eventId);

        RegistrationDetails registration = registrationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            throw new IllegalStateException("Check-in not allowed");
        }

        if (Boolean.TRUE.equals(registration.getCheckIn())) {
            throw new IllegalStateException("Already checked in");
        }

        registration.setCheckIn(true);
        registrationRepo.save(registration);
    }

}
