package com.community.volunteerhub.dto.registration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EventFeedbackRequest {

    @NotNull
    private Integer eventId;

    @NotNull
    @Min(1)
    @Max(5)
    private Float rating;

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }
}
