package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class HomePenumpangController implements Initializable {

    @FXML private Label lblNama;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblNama.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());
    }

    @FXML
    private void handleCariTiket() {
        SceneManager.switchScene("CariJadwal.fxml");
    }

    @FXML
    private void handleRiwayatTiket() {
        SceneManager.switchScene("RiwayatTiket.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.switchScene("login.fxml");
    }
}