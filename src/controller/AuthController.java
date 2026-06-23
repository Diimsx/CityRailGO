package controller;

import dao.UserDAO;
import model.Penumpang;
import model.User;

import java.time.LocalDate;

public class AuthController {
    private UserDAO userDAO;

    public AuthController() {
        this.userDAO = new UserDAO();
    }

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.login(username, password)) {
            return user;
        }
        return null;
    }

    public boolean register(String username, String password, String email,
                            String namaLengkap, String noTelepon,
                            String nik, LocalDate tglLahir, String jenisKelamin) {
        if (isUsernameTaken(username)) {
            return false;
        }
        Penumpang penumpang = new Penumpang(
                username, password, email, namaLengkap, noTelepon,
                nik, tglLahir, jenisKelamin
        );
        return userDAO.save(penumpang);
    }

    public boolean isUsernameTaken(String username) {
        return userDAO.findByUsername(username) != null;
    }

    public void logout(User user) {
        user.logout();
    }
}