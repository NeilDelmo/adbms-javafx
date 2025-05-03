package com.mycompany.karaoke_rental_system;

import javafx.beans.property.*;
import java.sql.Timestamp;

public class ActivityLog {
    private final ObjectProperty<Timestamp> timestamp;
    private final StringProperty username;
    private final StringProperty action;
    private final StringProperty details;

    public ActivityLog(Timestamp timestamp, String username, String action, String details) {
        this.timestamp = new SimpleObjectProperty<>(timestamp);
        this.username = new SimpleStringProperty(username);
        this.action = new SimpleStringProperty(action);
        this.details = new SimpleStringProperty(details);
    }

    public Timestamp getTimestamp() { return timestamp.get(); }
    public ObjectProperty<Timestamp> timestampProperty() { return timestamp; }

    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    public String getAction() { return action.get(); }
    public StringProperty actionProperty() { return action; }

    public String getDetails() { return details.get(); }
    public StringProperty detailsProperty() { return details; }
}

