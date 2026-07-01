package controller;

import dao.StasiunDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Stasiun;
import org.kordamp.ikonli.javafx.FontIcon;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaStasiunController implements Initializable {

    @FXML private Label lblAdminName;
    @FXML private TextField tfSearch;

    @FXML private TableView<Stasiun> tblStasiun;
    @FXML private TableColumn<Stasiun, Integer> colNo;
    @FXML private TableColumn<Stasiun, String> colKode;
    @FXML private TableColumn<Stasiun, String> colNama;
    @FXML private TableColumn<Stasiun, String> colKota;
    @FXML private TableColumn<Stasiun, Void> colAksi;

    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitle;
    @FXML private TextField tfKode;
    @FXML private TextField tfNama;
    @FXML private TextField tfKota;
    @FXML private Label lblModalError;
    @FXML private Button btnSimpan;

    private final StasiunDAO stasiunDAO = new StasiunDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Stasiun> daftarStasiun;
    private FilteredList<Stasiun> daftarStasiunTersaring;

    private Stasiun stasiunDiedit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getUsername());

        setupTabel();
        muatDataStasiun();
        setupPencarian();
    }

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(tblStasiun.getItems().indexOf(data.getValue()) + 1));

        colKode.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getKodeStasiun()));

        colNama.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaStasiun()));

        colKota.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getKota()));

        colAksi.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final FontIcon iconEdit = new FontIcon("fas-edit");
            private final FontIcon iconHapus = new FontIcon("fas-trash-alt");
            private final HBox container = new HBox(14, iconEdit, iconHapus);

            {
                iconEdit.getStyleClass().add("action-icon-edit");
                iconHapus.getStyleClass().add("action-icon-delete");
                container.setAlignment(Pos.CENTER_LEFT);

                iconEdit.setOnMouseClicked(e -> handleEditStasiun(getTableRow().getItem()));
                iconHapus.setOnMouseClicked(e -> handleHapusStasiun(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : container);
            }
        });
    }

    private void muatDataStasiun() {
        daftarStasiun = FXCollections.observableArrayList(stasiunDAO.findAll());
        daftarStasiunTersaring = new FilteredList<>(daftarStasiun, s -> true);
        tblStasiun.setItems(daftarStasiunTersaring);
    }

    private void setupPencarian() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> {
            String kueri = baru == null ? "" : baru.trim().toLowerCase();
            daftarStasiunTersaring.setPredicate(stasiun ->
                    kueri.isEmpty()
                            || stasiun.getKodeStasiun().toLowerCase().contains(kueri)
                            || stasiun.getNamaStasiun().toLowerCase().contains(kueri)
                            || stasiun.getKota().toLowerCase().contains(kueri)
            );
        });
    }

    @FXML
    private void handleTambahStasiun() {
        stasiunDiedit = null;
        lblModalTitle.setText("Tambah Stasiun Baru");
        kosongkanForm();
        enableKodeField();
        tampilkanModal();
    }

    private void handleEditStasiun(Stasiun stasiun) {
        if (stasiun == null) {
            return;
        }
        stasiunDiedit = stasiun;
        lblModalTitle.setText("Edit Data Stasiun");
        tfKode.setText(stasiun.getKodeStasiun());
        tfNama.setText(stasiun.getNamaStasiun());
        tfKota.setText(stasiun.getKota());
        sembunyikanError();
        disableKodeField();
        tampilkanModal();
    }

    private void handleHapusStasiun(Stasiun stasiun) {
        if (stasiun == null) {
            return;
        }

        boolean usedInRute = stasiunDAO.isUsedInRute(stasiun.getId());
        boolean usedInRuteStasiun = stasiunDAO.isUsedInRuteStasiun(stasiun.getId());

        if (usedInRute || usedInRuteStasiun) {
            String usageInfo = stasiunDAO.getUsageInfo(stasiun.getId());
            String message = "Stasiun '" + stasiun.getNamaStasiun() + "' masih digunakan dalam rute:\n\n" +
                    usageInfo + "\n\nSilakan hapus/ubah rute tersebut terlebih dahulu.";
            tampilkanDetailError("Tidak Dapat Menghapus Stasiun", message);
            return;
        }

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Stasiun");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus stasiun \"" + stasiun.getNamaStasiun() + "\"?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            boolean berhasil = stasiunDAO.delete(stasiun.getId());
            if (berhasil) {
                daftarStasiun.remove(stasiun);
                tampilkanAlertInfo("Berhasil", "Stasiun berhasil dihapus dari sistem.");
            } else {
                tampilkanAlertError("Gagal", "Gagal menghapus stasiun. Silakan coba lagi.");
            }
        }
    }

    @FXML
    private void handleSimpanStasiun() {
        String kode = tfKode.getText().trim().toUpperCase();
        String nama = tfNama.getText().trim();
        String kota = tfKota.getText().trim();

        if (kode.isEmpty()) {
            tampilkanError("Kode stasiun tidak boleh kosong.");
            return;
        }
        if (kode.length() < 2 || kode.length() > 4) {
            tampilkanError("Kode stasiun harus 2-4 karakter (misal: YK, GMR).");
            return;
        }
        if (!kode.matches("^[A-Z0-9]+$")) {
            tampilkanError("Kode stasiun hanya boleh huruf besar dan angka.");
            return;
        }
        if (nama.isEmpty()) {
            tampilkanError("Nama stasiun tidak boleh kosong.");
            return;
        }
        if (kota.isEmpty()) {
            tampilkanError("Kota tidak boleh kosong.");
            return;
        }

        boolean berhasil;
        if (stasiunDiedit == null) {
            if (stasiunDAO.kodeStasiunExists(kode)) {
                tampilkanError("Kode stasiun '" + kode + "' sudah terdaftar.");
                return;
            }

            Stasiun stasiunBaru = new Stasiun(kode, nama, kota);
            berhasil = stasiunDAO.save(stasiunBaru);
            if (berhasil) {
                daftarStasiun.add(stasiunBaru);
                tutupModal();
                tampilkanAlertInfo("Berhasil", "Stasiun '" + nama + "' berhasil ditambahkan.");
            } else {
                tampilkanError("Gagal menyimpan stasiun. Silakan coba lagi.");
            }
        } else {
            if (stasiunDAO.kodeStasiunExistsExcept(kode, stasiunDiedit.getId())) {
                tampilkanError("Kode stasiun '" + kode + "' sudah digunakan stasiun lain.");
                return;
            }

            stasiunDiedit.setNamaStasiun(nama);
            stasiunDiedit.setKota(kota);
            berhasil = stasiunDAO.update(stasiunDiedit);
            if (berhasil) {
                tblStasiun.refresh();
                tutupModal();
                tampilkanAlertInfo("Berhasil", "Stasiun berhasil diperbarui.");
            } else {
                tampilkanError("Gagal memperbarui stasiun. Silakan coba lagi.");
            }
        }
    }

    @FXML
    private void handleTutupModal() {
        tutupModal();
    }

    private void tampilkanModal() {
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void tutupModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        stasiunDiedit = null;
    }

    private void kosongkanForm() {
        tfKode.clear();
        tfNama.clear();
        tfKota.clear();
        sembunyikanError();
    }

    private void enableKodeField() {
        tfKode.setDisable(false);
    }

    private void disableKodeField() {
        tfKode.setDisable(true);
    }

    private void tampilkanError(String pesan) {
        lblModalError.setText(pesan);
        lblModalError.setVisible(true);
        lblModalError.setManaged(true);
    }

    private void sembunyikanError() {
        lblModalError.setText("");
        lblModalError.setVisible(false);
        lblModalError.setManaged(false);
    }

    private void tampilkanDetailError(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    private void tampilkanAlertError(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    private void tampilkanAlertInfo(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    @FXML
    private void handleNavDashboard() {
        SceneManager.switchScene("HomeAdmin.fxml");
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