package com.community.volunteerhub.controller.registration;

import com.community.volunteerhub.dto.ApiResponse;
import com.community.volunteerhub.entity.RegistrationDetails;
import com.community.volunteerhub.exception.UnauthorizedException;
import com.community.volunteerhub.service.UserService;
import com.community.volunteerhub.service.registration.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event/registration")
public class MyRegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;

    public MyRegistrationController(RegistrationService registrationService,
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

    @GetMapping("/my")
    public ResponseEntity<ApiResponse> myRegistrations(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null) {
            throw new UnauthorizedException("Authorization token missing");
        }

        String email = extractEmail(authHeader);

        List<RegistrationDetails> registrations =
                registrationService.getMyRegistrations(email);

        return ResponseEntity.ok(
                new ApiResponse(true, "My registrations fetched", registrations)
        );
    }

    }

