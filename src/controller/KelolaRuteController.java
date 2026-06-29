package controller;

import dao.RuteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.Rute;
import org.kordamp.ikonli.javafx.FontIcon;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaRuteController implements Initializable {

    @FXML private Label lblAdminName;
    @FXML private TextField tfSearch;

    @FXML private TableView<Rute> tblRute;
    @FXML private TableColumn<Rute, Integer> colNo;
    @FXML private TableColumn<Rute, String> colStasiunAsal;
    @FXML private TableColumn<Rute, String> colStasiunTujuan;
    @FXML private TableColumn<Rute, Double> colJarak;
    @FXML private TableColumn<Rute, Integer> colEstimasi;
    @FXML private TableColumn<Rute, Void> colAksi;

    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitle;
    @FXML private TextField tfStasiunAsal;
    @FXML private TextField tfStasiunTujuan;
    @FXML private TextField tfJarakKm;
    @FXML private TextField tfEstimasiMenit;
    @FXML private Label lblModalError;
    @FXML private Button btnSimpan;

    private final RuteDAO ruteDAO = new RuteDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Rute> daftarRute;
    private FilteredList<Rute> daftarRuteTersaring;

    private Rute ruteDiedit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());

        setupTabel();
        muatDataRute();
        setupPencarian();
    }

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(tblRute.getItems().indexOf(data.getValue()) + 1));

        colStasiunAsal.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStasiunAsal()));

        colStasiunTujuan.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStasiunTujuan()));

        colJarak.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getJarakKm()));
        colJarak.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double jarak, boolean kosong) {
                super.updateItem(jarak, kosong);
                setText(kosong || jarak == null ? null : String.format("%.0f km", jarak));
            }
        });

        colEstimasi.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEstimasiMenit()));
        colEstimasi.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer menit, boolean kosong) {
                super.updateItem(menit, kosong);
                setText(kosong || menit == null ? null : formatEstimasi(menit));
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final FontIcon iconEdit = new FontIcon("fas-edit");
            private final FontIcon iconHapus = new FontIcon("fas-trash-alt");
            private final HBox container = new HBox(14, iconEdit, iconHapus);

            {
                iconEdit.getStyleClass().add("action-icon-edit");
                iconHapus.getStyleClass().add("action-icon-delete");
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                iconEdit.setOnMouseClicked(e -> handleEditRute(getTableRow().getItem()));
                iconHapus.setOnMouseClicked(e -> handleHapusRute(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : container);
            }
        });
    }

    private String formatEstimasi(int menit) {
        int jam = menit / 60;
        int sisaMenit = menit % 60;
        if (jam <= 0) {
            return sisaMenit + " menit";
        }
        return jam + " jam " + sisaMenit + " menit";
    }

    private void muatDataRute() {
        daftarRute = FXCollections.observableArrayList(ruteDAO.findAll());
        daftarRuteTersaring = new FilteredList<>(daftarRute, r -> true);
        tblRute.setItems(daftarRuteTersaring);
    }

    private void setupPencarian() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> {
            String kueri = baru == null ? "" : baru.trim().toLowerCase();
            daftarRuteTersaring.setPredicate(rute ->
                    kueri.isEmpty()
                            || rute.getStasiunAsal().toLowerCase().contains(kueri)
                            || rute.getStasiunTujuan().toLowerCase().contains(kueri)
            );
        });
    }

    @FXML
    private void handleTambahRute() {
        ruteDiedit = null;
        lblModalTitle.setText("Tambah Rute");
        kosongkanForm();
        tampilkanModal();
    }

    private void handleEditRute(Rute rute) {
        if (rute == null) {
            return;
        }
        ruteDiedit = rute;
        lblModalTitle.setText("Edit Rute");
        tfStasiunAsal.setText(rute.getStasiunAsal());
        tfStasiunTujuan.setText(rute.getStasiunTujuan());
        tfJarakKm.setText(String.valueOf(rute.getJarakKm()));
        tfEstimasiMenit.setText(String.valueOf(rute.getEstimasiMenit()));
        sembunyikanError();
        tampilkanModal();
    }

    private void handleHapusRute(Rute rute) {
        if (rute == null) {
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Rute");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus rute \""
                + rute.getStasiunAsal() + " \u2192 " + rute.getStasiunTujuan() + "\"?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            boolean berhasil = adminController.hapusRute(rute.getId());
            if (berhasil) {
                daftarRute.remove(rute);
            } else {
                tampilkanAlertError("Gagal menghapus rute. Pastikan rute tidak terpakai di jadwal aktif.");
            }
        }
    }

    @FXML
    private void handleSimpanRute() {
        String stasiunAsal = tfStasiunAsal.getText().trim();
        String stasiunTujuan = tfStasiunTujuan.getText().trim();
        String teksJarak = tfJarakKm.getText().trim();
        String teksEstimasi = tfEstimasiMenit.getText().trim();

        if (stasiunAsal.isEmpty() || stasiunTujuan.isEmpty() || teksJarak.isEmpty() || teksEstimasi.isEmpty()) {
            tampilkanError("Semua field wajib diisi.");
            return;
        }

        if (stasiunAsal.equalsIgnoreCase(stasiunTujuan)) {
            tampilkanError("Stasiun asal dan tujuan tidak boleh sama.");
            return;
        }

        double jarakKm;
        try {
            jarakKm = Double.parseDouble(teksJarak);
            if (jarakKm <= 0) {
                tampilkanError("Jarak harus berupa angka positif.");
                return;
            }
        } catch (NumberFormatException e) {
            tampilkanError("Jarak harus berupa angka.");
            return;
        }

        int estimasiMenit;
        try {
            estimasiMenit = Integer.parseInt(teksEstimasi);
            if (estimasiMenit <= 0) {
                tampilkanError("Estimasi waktu harus berupa angka positif.");
                return;
            }
        } catch (NumberFormatException e) {
            tampilkanError("Estimasi waktu harus berupa angka (dalam menit).");
            return;
        }

        boolean berhasil;
        if (ruteDiedit == null) {
            Rute ruteBaru = new Rute(stasiunAsal, stasiunTujuan, jarakKm, estimasiMenit);
            berhasil = adminController.tambahRute(ruteBaru);
            if (berhasil) {
                daftarRute.add(ruteBaru);
            }
        } else {
            ruteDiedit.setStasiunAsal(stasiunAsal);
            ruteDiedit.setStasiunTujuan(stasiunTujuan);
            ruteDiedit.setJarakKm(jarakKm);
            ruteDiedit.setEstimasiMenit(estimasiMenit);
            berhasil = adminController.editRute(ruteDiedit);
            if (berhasil) {
                tblRute.refresh();
            }
        }

        if (berhasil) {
            tutupModal();
        } else {
            tampilkanError("Gagal menyimpan data rute. Silakan coba lagi.");
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
        ruteDiedit = null;
    }

    private void kosongkanForm() {
        tfStasiunAsal.clear();
        tfStasiunTujuan.clear();
        tfJarakKm.clear();
        tfEstimasiMenit.clear();
        sembunyikanError();
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

    private void tampilkanAlertError(String pesan) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Terjadi Kesalahan");
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
        // sudah berada di halaman Kelola Rute
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