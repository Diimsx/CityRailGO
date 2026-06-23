package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import model.User;
import util.SceneManager;
import util.SessionManager;

public class LoginController {

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private HBox errorBox;
    @FXML private Label lblError;
    @FXML private Region errorSpacer;

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
        SceneManager.switchScene("Register.fxml");
    }

    private void showError(String pesan) {
        lblError.setText(pesan);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
        errorSpacer.setPrefHeight(12);
        errorSpacer.setManaged(true);
        errorSpacer.setVisible(true);
    }

    private void hideError() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
        errorSpacer.setPrefHeight(0);
        errorSpacer.setManaged(false);
        errorSpacer.setVisible(false);
    }
}