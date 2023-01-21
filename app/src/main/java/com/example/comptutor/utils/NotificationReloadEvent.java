package com.example.comptutor.utils;

public class NotificationReloadEvent {
    private Boolean reload;

    public NotificationReloadEvent(Boolean reload) {
        this.reload = reload;
    }

    public Boolean getData() {
        return reload;
    }
}
