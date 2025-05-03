package com.mycompany.karaoke_rental_system;

import javafx.beans.property.*;
import java.sql.Timestamp;

public class UserModel {
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty role;
    private final ObjectProperty<Timestamp> lastLogin;
    private final StringProperty createdBy;
    private final ObjectProperty<Timestamp> createdAt;

    public UserModel(int userId, String username, String role, Timestamp lastLogin, String createdBy, Timestamp createdAt) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
        this.lastLogin = new SimpleObjectProperty<>(lastLogin);
        this.createdBy = new SimpleStringProperty(createdBy);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }

    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    public String getRole() { return role.get(); }
    public StringProperty roleProperty() { return role; }

    public Timestamp getLastLogin() { return lastLogin.get(); }
    public ObjectProperty<Timestamp> lastLoginProperty() { return lastLogin; }

    public String getCreatedBy() { return createdBy.get(); }
    public StringProperty createdByProperty() { return createdBy; }

    public Timestamp getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<Timestamp> createdAtProperty() { return createdAt; }
}

