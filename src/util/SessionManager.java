package util;

import model.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole().equals("ADMIN");
    }

    public boolean isPenumpang() {
        return isLoggedIn() && currentUser.getRole().equals("PENUMPANG");
    }
}