package controller;

import dao.KeretaDAO;
import dao.TiketDAO;
import dao.UserDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Rute;
import model.Tiket;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class HomeAdminController implements Initializable {

    @FXML private Label lblAdminName;
    @FXML private Label lblTotalTiket;
    @FXML private Label lblTotalPendapatan;
    @FXML private Label lblTotalKereta;
    @FXML private Label lblTotalPenumpang;

    @FXML private TableView<Tiket> tblTiketTerbaru;
    @FXML private TableColumn<Tiket, String> colKodeTiket;
    @FXML private TableColumn<Tiket, String> colPenumpang;
    @FXML private TableColumn<Tiket, String> colKereta;
    @FXML private TableColumn<Tiket, String> colRute;
    @FXML private TableColumn<Tiket, String> colStatus;

    private final TiketDAO tiketDAO = new TiketDAO();
    private final KeretaDAO keretaDAO = new KeretaDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());
        setupTabelTiket();
        muatStatistik();
    }

    private void setupTabelTiket() {
        colKodeTiket.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getKodeTiket()));

        colPenumpang.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPenumpang().getNamaLengkap()));

        colKereta.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getJadwal().getKereta().getNama()));

        colRute.setCellValueFactory(data -> {
            Rute rute = data.getValue().getJadwal().getRute();
            return new SimpleStringProperty(rute.getStasiunAsal() + " - " + rute.getStasiunTujuan());
        });

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));
    }

    private void muatStatistik() {
        List<Tiket> semuaTiket = tiketDAO.findAll();

        lblTotalTiket.setText(String.valueOf(semuaTiket.size()));

        double totalPendapatan = semuaTiket.stream()
                .mapToDouble(Tiket::getHargaTotal)
                .sum();
        lblTotalPendapatan.setText(formatRupiah(totalPendapatan));

        lblTotalKereta.setText(String.valueOf(keretaDAO.findAll().size()));
        lblTotalPenumpang.setText(String.valueOf(userDAO.countPenumpang()));

        int batas = Math.min(semuaTiket.size(), 10);
        ObservableList<Tiket> tiketTerbaru = FXCollections.observableArrayList(
                semuaTiket.subList(0, batas)
        );
        tblTiketTerbaru.setItems(tiketTerbaru);
    }

    private String formatRupiah(double nominal) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return format.format(nominal).replace(",00", "");
    }

    @FXML
    private void handleNavDashboard() {
    }

    @FXML
    private void handleNavKereta() {
        SceneManager.switchScene("KelolaKereta.fxml");
    }

    @FXML
    private void handleNavRute() {
        SceneManager.switchScene("KelolaRute.fxml");
    }

    @FXML
    private void handleNavStasiun() {
        SceneManager.switchScene("KelolaStasiun.fxml");
    }
    
    @FXML
    private void handleNavJadwal() {
        SceneManager.switchScene("KelolaJadwal.fxml");
    }

    @FXML
    private void handleNavPromo() {
        SceneManager.switchScene("KelolaPromo.fxml");
    }

    @FXML
    private void handleNavValidasi() {
        SceneManager.switchScene("ValidasiPembayaran.fxml");
    }

    @FXML
    private void handleNavLaporan() {
        SceneManager.switchScene("LaporanPenjualan.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Logout");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Yakin logout?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            SceneManager.switchScene("login.fxml");
        }
    }
}