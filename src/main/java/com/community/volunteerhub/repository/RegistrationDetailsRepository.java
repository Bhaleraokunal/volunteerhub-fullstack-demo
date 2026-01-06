package com.community.volunteerhub.repository;

import com.community.volunteerhub.entity.RegistrationDetails;
import com.community.volunteerhub.entity.RegistrationId;
import com.community.volunteerhub.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegistrationDetailsRepository
        extends JpaRepository<RegistrationDetails, RegistrationId> {

    long countByEventIdAndStatus(Integer eventId, RegistrationStatus status);

    List<RegistrationDetails> findByVolunteerId(String volunteerId);
    
    List<RegistrationDetails> findByVolunteerIdAndStatus(String email, RegistrationStatus status);

    List<RegistrationDetails> findByEventIdAndStatus(Integer eventId, RegistrationStatus status);

    long countByEventId(Integer eventId);
    
    @Query("""
        SELECT r.volunteerId
        FROM RegistrationDetails r
        WHERE r.eventId = :eventId AND r.status = 'REGISTERED'
    """)
    List<String> findVolunteerIdByEventIdAndStatus(Integer eventId);

    @Query("""
        SELECT r.volunteerId
        FROM RegistrationDetails r
        WHERE r.eventId = :eventId AND r.checkIn = true
    """)
    List<String> findCheckedInVolunteers(Integer eventId);
}
