package org.example.util;

import org.example.model.User;

public class UserSession {
    private static UserSession instance;
    private User loggedInUser;
    private UserSession() {
    }
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
    public User getUser() {
        return loggedInUser;
    }

    public void cleanUserSession() {
        loggedInUser = null;
    }
}
