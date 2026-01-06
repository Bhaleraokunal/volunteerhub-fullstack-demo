package com.community.volunteerhub.dto.registration;

import jakarta.validation.constraints.NotNull;

public class EventRegistrationRequest {

    @NotNull(message = "Event ID is required")
    private Integer eventId;

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }
}
