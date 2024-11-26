package com.p3.event;

import java.time.LocalDateTime;

public class Event {     // TODO Lav class i sin egen fil
    private LocalDateTime eventTime;
    private String eventType;

    public Event(LocalDateTime eventTime, String eventType) {
        this.eventTime = eventTime;
        this.eventType = eventType;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public String getEventType() {
        return eventType;
    }
}