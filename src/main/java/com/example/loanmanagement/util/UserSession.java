package com.example.loanmanagement.util;

import com.example.loanmanagement.model.User;

public class UserSession {
    private static UserSession instance;
    private User user;

    private UserSession(User user) {
        this.user = user;
    }

    public static void setSession(User user) {
        instance = new UserSession(user);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public User getUser() {
        return user;
    }

    public static void cleanSession() {
        instance = null;
    }
}
