package com.p3.employeeHistory;

import java.time.LocalDateTime;

public class ShiftSequence {
    String styleClass;
    int duration;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String sequenceType;
    boolean edited;

    public ShiftSequence(String styleClass, int duration, LocalDateTime startTime, LocalDateTime endTime,  String sequenceType, boolean edited) {
        this.styleClass = styleClass;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sequenceType = sequenceType;
        this.edited = edited;
    }

    public String getStyleClass() {
        return styleClass;
    }
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public String getSequenceType() {
        return sequenceType;
    }
    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

    public boolean getEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }


}
