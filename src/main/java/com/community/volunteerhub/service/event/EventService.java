package com.community.volunteerhub.service.event;

import com.community.volunteerhub.dto.event.CreateEventRequest;
import com.community.volunteerhub.dto.event.UpdateEventRequest;
import com.community.volunteerhub.entity.EventDetails;
import com.community.volunteerhub.entity.RegistrationDetails;
import com.community.volunteerhub.entity.UserDetails;
import com.community.volunteerhub.enums.RegistrationStatus;
import com.community.volunteerhub.repository.EventDetailsRepository;
import com.community.volunteerhub.repository.RegistrationDetailsRepository;
import com.community.volunteerhub.repository.UserDetailsRepository;
import com.community.volunteerhub.service.emailService.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {

    private final EventDetailsRepository repo;
    private final RegistrationDetailsRepository registrationRepo;   // ⭐ ADD
    private final EmailService emailService;   

    @Autowired
    public EventService(
            EventDetailsRepository repo,
            RegistrationDetailsRepository registrationRepo,
            EmailService emailService
    ) {
        this.repo = repo;
        this.registrationRepo = registrationRepo;
        this.emailService = emailService;
    }


    @Autowired
    private UserDetailsRepository userRepo;

    @Transactional
    public EventDetails createEvent(CreateEventRequest req, String organizerEmail) {

        UserDetails user = userRepo.findById(organizerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"ORGANIZER".equalsIgnoreCase(user.getUserRole())) {
            throw new SecurityException("Only organizers can create events");
        }

   
        LocalDate today = LocalDate.now();

        if (req.getEventStartDate().isBefore(today)) {
            throw new IllegalArgumentException("Event start date cannot be in the past");
        }

        if (req.getEventEndDate().isBefore(req.getEventStartDate())) {
            throw new IllegalArgumentException("Event end date cannot be before start date");
        }

        EventDetails event = new EventDetails();
        event.setEventName(req.getEventName());
        event.setAddress(req.getAddress());
        event.setCity(req.getCity());
        event.setDescription(req.getDescription());
        event.setMaxAllowedRegistrations(req.getMaxAllowedRegistrations());
        event.setEventStartDate(req.getEventStartDate());
        event.setEventEndDate(req.getEventEndDate());
        event.setRegistrationAllowed(true);
        event.setOrganizerId(organizerEmail);

        return repo.save(event);
    }

    
    @Transactional
    public EventDetails updateEvent(
            Integer eventId,
            UpdateEventRequest req,
            String organizerEmail) {

        EventDetails event = repo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Organizer check
        if (!event.getOrganizerId().equals(organizerEmail)) {
            throw new SecurityException("Only organizer can update this event");
        }

        if (req.getEventName() != null)
            event.setEventName(req.getEventName());

        if (req.getAddress() != null)
            event.setAddress(req.getAddress());

        if (req.getCity() != null)
            event.setCity(req.getCity());

        if (req.getDescription() != null)
            event.setDescription(req.getDescription());

        if (req.getMaxAllowedRegistrations() != null)
            event.setMaxAllowedRegistrations(req.getMaxAllowedRegistrations());

        if (req.getEventStartDate() != null)
            event.setEventStartDate(req.getEventStartDate());

        if (req.getEventEndDate() != null)
            event.setEventEndDate(req.getEventEndDate());

        if (req.getRegistrationAllowed() != null)
            event.setRegistrationAllowed(req.getRegistrationAllowed());

        return repo.save(event);
    }

    @Transactional(readOnly = true)
    public EventDetails getEventById(Integer eventId) {

        EventDetails event = repo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        long registered =
            registrationRepo.countByEventIdAndStatus(
                eventId,
                RegistrationStatus.REGISTERED
            );

        event.setRegisteredCount((int) registered);

        return event;
    }



    @Transactional
    public void deleteEvent(Integer eventId, String organizerEmail) {

        EventDetails event = repo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getOrganizerId().equals(organizerEmail)) {
            throw new SecurityException("Only organizer can delete this event");
        }

        // Get ONLY registered volunteers BEFORE deleting event
        List<RegistrationDetails> registrations =
                registrationRepo.findByEventIdAndStatus(
                        eventId,
                        RegistrationStatus.REGISTERED
                );

        for (RegistrationDetails reg : registrations) {

            String volunteerEmail = reg.getVolunteerId();
            String volunteerName = volunteerEmail.split("@")[0];

            emailService.sendEventCancellationEmail(
                    volunteerEmail,
                    volunteerName,
                    event.getEventName(),
                    event.getEventStartDate().toString(),
                    organizerEmail
            );
        }

        // Now delete the event
        repo.delete(event);
    }


    @Transactional
    public EventDetails updateRegistrationStatus(
            Integer eventId,
            Boolean registrationAllowed,
            String organizerEmail) {

        EventDetails event = repo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getOrganizerId().equals(organizerEmail)) {
            throw new SecurityException("Only organizer can update registration status");
        }

        event.setRegistrationAllowed(registrationAllowed);
        return repo.save(event);
    }
    
    /*Just commented this methods in future if we need basically it dose the same things just like the below method is working*/
//    public List<EventDetails> getEventsByStatus(String status) {
//
//        if ("open".equalsIgnoreCase(status)) {
//            return repo.findByRegistrationAllowedTrue();
//        }
//
//        if ("closed".equalsIgnoreCase(status)) {
//            return repo.findByRegistrationAllowedFalse();
//        }
//
//        throw new IllegalArgumentException("Invalid status. Use open or closed");
//    }
//
//    public List<EventDetails> getEventsByOrganizer(String organizerEmail) {
//        return repo.findByOrganizerId(organizerEmail);
//    }
//
//    public List<EventDetails> getOpenEvents() {
//        return repo.findByRegistrationAllowedTrue();
//    }

    
    // For simple and better the above methods does the same but it access 
    @Transactional(readOnly = true)
    public List<EventDetails> getEventsByStatus(String status) {

        return switch (status.toUpperCase()) {

            case "OPEN" -> repo.findByRegistrationAllowed(true);

            case "CLOSED" -> repo.findByRegistrationAllowed(false);

            case "ALL" -> repo.findAll();

            default -> throw new IllegalArgumentException(
                    "Invalid status. Allowed values: OPEN, CLOSED, ALL"
            );
        };
    }
    
    @Transactional(readOnly = true)
    public List<EventDetails> getEventsByOrganizer(String organizerEmail) {
        return repo.findByOrganizerId(organizerEmail);
    }




}
