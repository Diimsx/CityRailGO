package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import util.SceneManager;
import util.SessionManager;

public class LoginController {

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Label lblError;

    private final AuthController authController = new AuthController();

    @FXML
    private void handleLogin() {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password tidak boleh kosong.");
            return;
        }

        User user = authController.login(username, password);

        if (user == null) {
            showError("Username atau password salah. Silakan coba lagi.");
            return;
        }

        SessionManager.getInstance().login(user);
        hideError();

        if (SessionManager.getInstance().isAdmin()) {
            SceneManager.switchScene("HomeAdmin.fxml");
        } else {
            SceneManager.switchScene("HomePenumpang.fxml");
        }
    }

    @FXML
    private void handleGoToRegister() {
        SceneManager.switchScene("register.fxml");
    }

    private void showError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
    }

    private void hideError() {
        lblError.setText("");
        lblError.setVisible(false);
    }
}