package com.community.volunteerhub.repository;

import com.community.volunteerhub.entity.EventDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventDetailsRepository
        extends JpaRepository<EventDetails, Integer> {

    List<EventDetails> findByOrganizerId(String organizerId);

//    List<EventDetails> findByRegistrationAllowedTrue();
//    
//    List<EventDetails> findByRegistrationAllowedFalse();
    
    List<EventDetails> findByRegistrationAllowed(Boolean registrationAllowed);
    
    //List<EventDetails> findByOrganizerEmail(String organizerEmail);

    
    //List<EventDetails> findByOrganizerId(String organizerId);

    List<EventDetails> findByRegistrationAllowed(boolean registrationAllowed);
    
    List<EventDetails> findByEventStartDate(LocalDate date);

    
}
