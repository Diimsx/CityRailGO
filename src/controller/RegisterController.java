package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import util.SceneManager;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField tfNamaLengkap;
    @FXML private TextField tfNik;
    @FXML private TextField tfEmail;
    @FXML private TextField tfNoTelepon;
    @FXML private DatePicker dpTglLahir;
    @FXML private ComboBox<String> cbJenisKelamin;
    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private HBox errorBox;
    @FXML private Label lblError;
    @FXML private Region errorSpacer;

    private final AuthController authController = new AuthController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbJenisKelamin.setItems(
            FXCollections.observableArrayList("Laki-laki", "Perempuan")
        );
    }

    @FXML
    private void handleRegister() {
        String namaLengkap  = tfNamaLengkap.getText().trim();
        String nik          = tfNik.getText().trim();
        String email        = tfEmail.getText().trim();
        String noTelepon    = tfNoTelepon.getText().trim();
        LocalDate tglLahir  = dpTglLahir.getValue();
        String jenisKelamin = cbJenisKelamin.getValue();
        String username     = tfUsername.getText().trim();
        String password     = pfPassword.getText();

        if (namaLengkap.isEmpty() || nik.isEmpty() || email.isEmpty()
                || noTelepon.isEmpty() || username.isEmpty()
                || password.isEmpty() || tglLahir == null
                || jenisKelamin == null) {
            showError("Semua field wajib diisi.");
            return;
        }

        if (nik.length() != 16 || !nik.matches("\\d+")) {
            showError("NIK harus 16 digit angka.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Format email tidak valid.");
            return;
        }

        if (password.length() < 6) {
            showError("Password minimal 6 karakter.");
            return;
        }

        if (authController.isUsernameTaken(username)) {
            showError("Username sudah dipakai. Coba username lain.");
            return;
        }

        boolean berhasil = authController.register(
                username, password, email,
                namaLengkap, noTelepon,
                nik, tglLahir, jenisKelamin
        );

        if (berhasil) {
            hideError();
            SceneManager.switchScene("Login.fxml");
        } else {
            showError("Registrasi gagal. Silakan coba lagi.");
        }
    }

    @FXML
    private void handleGoToLogin() {
        SceneManager.switchScene("Login.fxml");
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