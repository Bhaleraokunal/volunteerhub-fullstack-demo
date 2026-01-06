package com.community.volunteerhub.controller.event;

import com.community.volunteerhub.dto.ApiResponse;
import com.community.volunteerhub.dto.event.CreateEventRequest;
import com.community.volunteerhub.dto.event.EventResponse;
import com.community.volunteerhub.dto.event.UpdateEventRequest;
import com.community.volunteerhub.entity.EventDetails;
import com.community.volunteerhub.service.UserService;
import com.community.volunteerhub.service.event.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/event")   
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    //  CREATE EVENT 
    // POST /event/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);

        if (!userService.isValidToken(token)) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "Unauthorized"));
        }

        String organizerEmail = userService.getEmailFromToken(token);

        try {
            EventDetails event = eventService.createEvent(request, organizerEmail);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Event created", mapToResponse(event))
            );
        } catch (SecurityException ex) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

    //  UPDATE EVENT 
    // PUT /event/update/{eventId}
    @PutMapping("/update/{eventId}")
    public ResponseEntity<ApiResponse> updateEvent(
            @PathVariable Integer eventId,
            @RequestBody UpdateEventRequest request,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);

        if (!userService.isValidToken(token)) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "Unauthorized"));
        }

        String organizerEmail = userService.getEmailFromToken(token);
        EventDetails event = eventService.updateEvent(eventId, request, organizerEmail);

        return ResponseEntity.ok(
                new ApiResponse(true, "Event updated successfully", mapToResponse(event))
        );
    }
    
    /*This methods is meant to get the event which is organized by or created by the organizer
     * so if we want to use this methods , first create a @Transactional(readOnly = true)
		public List<EventDetails> getEventsByOrganizer(String organizerEmail) {
		    return eventRepo.findByOrganizerEmail(organizerEmail);
		}
     * and add the  List<EventDetails> findByOrganizerEmail(String organizerEmail);

     * */
    
    //  ORGANIZER EVENTS
    // GET /event/organizer
    @GetMapping("/organizer") 
    public ResponseEntity<ApiResponse> getOrganizerEvents(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);

        if (!userService.isValidToken(token)) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "Unauthorized"));
        }

        String organizerEmail = userService.getEmailFromToken(token);

        List<EventResponse> events = eventService.getEventsByOrganizer(organizerEmail)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse(true, "Events fetched", events)
        );
    }

    //OPEN EVENTS 
    // GET /event/open
//    @GetMapping("/open")
//    public ResponseEntity<ApiResponse> getOpenEvents() {
//
//        List<EventResponse> events = eventService.getOpenEvents()
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(
//                new ApiResponse(true, "Open events fetched", events)
//        );
//    }

	 //  GET EVENT BY ID 
	 // GET /event/{eventId}
    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse> getEvent(@PathVariable Integer eventId) {

        EventDetails event = eventService.getEventById(eventId);

        return ResponseEntity.ok(
            new ApiResponse(true, "Event fetched", event)
        );
    }


	// DELETE EVENT 
	// DELETE /event/delete/{eventId}
	@DeleteMapping("/delete/{eventId}")
	public ResponseEntity<ApiResponse> deleteEvent(
	        @PathVariable Integer eventId,
	        @RequestHeader(name = "Authorization", required = false) String authHeader) {

	    String token = extractToken(authHeader);

	    if (!userService.isValidToken(token)) {
	        return ResponseEntity.status(401)
	                .body(new ApiResponse(false, "Unauthorized"));
	    }

	    String organizerEmail = userService.getEmailFromToken(token);

	    eventService.deleteEvent(eventId, organizerEmail);

	    return ResponseEntity.ok(
	            new ApiResponse(true, "Event deleted successfully")
	    );
	}

	//  OPEN / CLOSE REGISTRATION 
	// PUT /event/{eventId}/status
	@PutMapping("/{eventId}/status")
	public ResponseEntity<ApiResponse> updateRegistrationStatus(
	        @PathVariable Integer eventId,
	        @RequestParam Boolean registrationAllowed,
	        @RequestHeader(name = "Authorization", required = false) String authHeader) {

	    String token = extractToken(authHeader);

	    if (!userService.isValidToken(token)) {
	        return ResponseEntity.status(401)
	                .body(new ApiResponse(false, "Unauthorized"));
	    }

	    String organizerEmail = userService.getEmailFromToken(token);

	    EventDetails event =
	            eventService.updateRegistrationStatus(eventId, registrationAllowed, organizerEmail);

	    return ResponseEntity.ok(
	            new ApiResponse(true, "Registration status updated", mapToResponse(event))
	    );
	}

	/*This is methods is deprecated by my side which has some issue.*/
	// LIST EVENTS BY STATUS 
	// GET /event/list/{status}
//	@GetMapping("/list/{status}")
//	public ResponseEntity<ApiResponse> listEventsByStatus(@PathVariable String status) {
//
//	    List<EventResponse> events = eventService.getEventsByStatus(status)
//	            .stream()
//	            .map(this::mapToResponse)
//	            .collect(Collectors.toList());
//
//	    return ResponseEntity.ok(
//	            new ApiResponse(true, "Events fetched", events)
//	    );
//	}
	
	@GetMapping("/list/{status}")
	public ResponseEntity<ApiResponse> listEventsByStatus(
	        @PathVariable String status) {

	    return ResponseEntity.ok(
	            new ApiResponse(
	                    true,
	                    "Events fetched",
	                    eventService.getEventsByStatus(status)
	            )
	    );
	}


    //  ENTITY → DTO MAPPER 
    private EventResponse mapToResponse(EventDetails event) {
        EventResponse res = new EventResponse();
        res.setEventId(event.getEventId());
        res.setEventName(event.getEventName());
        res.setAddress(event.getAddress());
        res.setCity(event.getCity());
        res.setOrganizerId(event.getOrganizerId());
        res.setDescription(event.getDescription());
        res.setMaxAllowedRegistrations(event.getMaxAllowedRegistrations());
        res.setEventStartDate(event.getEventStartDate());
        res.setEventEndDate(event.getEventEndDate());
        res.setRating(event.getRating());
        res.setRegistrationAllowed(event.getRegistrationAllowed());
        return res;
    }
}
