package com.community.volunteerhub.controller.registration;

import com.community.volunteerhub.dto.ApiResponse;
import com.community.volunteerhub.dto.registration.EventFeedbackRequest;
import com.community.volunteerhub.dto.registration.EventRegistrationRequest;
import com.community.volunteerhub.exception.UnauthorizedException;
import com.community.volunteerhub.service.UserService;
import com.community.volunteerhub.service.registration.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/event/registration")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;

    public RegistrationController(RegistrationService registrationService,
                                  UserService userService) {
        this.registrationService = registrationService;
        this.userService = userService;
    }

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization token missing");
        }

        String token = authHeader.substring(7);

        if (!userService.isValidToken(token)) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userService.getEmailFromToken(token);
    }

    //VOLUNTEER ACTIONS

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody EventRegistrationRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String email = extractEmail(authHeader);
        registrationService.registerForEvent(email, request.getEventId());

        return ResponseEntity.ok(new ApiResponse(true, "Registered successfully"));
    }

    @PostMapping("/unregister")
    public ResponseEntity<ApiResponse> unregister(
            @Valid @RequestBody EventRegistrationRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String email = extractEmail(authHeader);
        registrationService.unregisterFromEvent(email, request.getEventId());

        return ResponseEntity.ok(new ApiResponse(true, "Unregistered successfully"));
    }

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse> checkIn(
            @Valid @RequestBody EventRegistrationRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String email = extractEmail(authHeader);
        registrationService.checkIn(email, request.getEventId());

        return ResponseEntity.ok(new ApiResponse(true, "Check-in successful"));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse> submitFeedback(
            @Valid @RequestBody EventFeedbackRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String email = extractEmail(authHeader);
        registrationService.submitFeedback(
                email,
                request.getEventId(),
                request.getRating()
        );

        return ResponseEntity.ok(new ApiResponse(true, "Feedback submitted"));
    }

    /*ORGANIZER ACTIONS  */

    @GetMapping("/event/{eventId}/registrations")
    public ResponseEntity<ApiResponse> getRegistrations(
            @PathVariable Integer eventId,
            @RequestHeader("Authorization") String authHeader) {

        extractEmail(authHeader); // role validation assumed
        List<String> volunteers = registrationService.getRegistrations(eventId);

        return ResponseEntity.ok(
                new ApiResponse(true, "Registrations fetched", volunteers)
        );
    }

    @GetMapping("/event/{eventId}/participants")
    public ResponseEntity<ApiResponse> getParticipants(
            @PathVariable Integer eventId,
            @RequestHeader("Authorization") String authHeader) {

        extractEmail(authHeader);
        List<String> participants = registrationService.getParticipants(eventId);

        return ResponseEntity.ok(
                new ApiResponse(true, "Participants fetched", participants)
        );
        
        
    }
}
