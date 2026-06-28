package controller;

import dao.KeretaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.Kereta;
import org.kordamp.ikonli.javafx.FontIcon;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaKeretaController implements Initializable {

    @FXML private Label lblAdminName;
    @FXML private TextField tfSearch;

    @FXML private TableView<Kereta> tblKereta;
    @FXML private TableColumn<Kereta, Integer> colNo;
    @FXML private TableColumn<Kereta, String> colNama;
    @FXML private TableColumn<Kereta, String> colNomorKereta;
    @FXML private TableColumn<Kereta, String> colJenis;
    @FXML private TableColumn<Kereta, Integer> colKapasitas;
    @FXML private TableColumn<Kereta, Void> colAksi;

    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitle;
    @FXML private TextField tfNama;
    @FXML private TextField tfNomorKereta;
    @FXML private ComboBox<String> cbJenis;
    @FXML private TextField tfKapasitas;
    @FXML private Label lblModalError;
    @FXML private Button btnSimpan;

    private final KeretaDAO keretaDAO = new KeretaDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Kereta> daftarKereta;
    private FilteredList<Kereta> daftarKeretaTersaring;

    private Kereta keretaDiedit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());
        cbJenis.setItems(FXCollections.observableArrayList("Eksekutif", "Bisnis", "Ekonomi", "Campuran"));

        setupTabel();
        muatDataKereta();
        setupPencarian();
    }

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(tblKereta.getItems().indexOf(data.getValue()) + 1));

        colNama.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNama()));

        colNomorKereta.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNomorKereta()));

        colJenis.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getJenis()));
        colJenis.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String jenis, boolean kosong) {
                super.updateItem(jenis, kosong);
                if (kosong || jenis == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(jenis);
                    badge.getStyleClass().add("badge-jenis");
                    setGraphic(badge);
                }
            }
        });

        colKapasitas.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getKapasitasTotal()));

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final FontIcon iconEdit = new FontIcon("fas-edit");
            private final FontIcon iconHapus = new FontIcon("fas-trash-alt");
            private final HBox container = new HBox(14, iconEdit, iconHapus);

            {
                iconEdit.getStyleClass().add("action-icon-edit");
                iconHapus.getStyleClass().add("action-icon-delete");
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                iconEdit.setOnMouseClicked(e -> handleEditKereta(getTableRow().getItem()));
                iconHapus.setOnMouseClicked(e -> handleHapusKereta(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : container);
            }
        });
    }

    private void muatDataKereta() {
        daftarKereta = FXCollections.observableArrayList(keretaDAO.findAll());
        daftarKeretaTersaring = new FilteredList<>(daftarKereta, k -> true);
        tblKereta.setItems(daftarKeretaTersaring);
    }

    private void setupPencarian() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> {
            String kueri = baru == null ? "" : baru.trim().toLowerCase();
            daftarKeretaTersaring.setPredicate(kereta ->
                    kueri.isEmpty()
                            || kereta.getNama().toLowerCase().contains(kueri)
                            || kereta.getNomorKereta().toLowerCase().contains(kueri)
            );
        });
    }

    @FXML
    private void handleTambahKereta() {
        keretaDiedit = null;
        lblModalTitle.setText("Tambah Kereta");
        kosongkanForm();
        tampilkanModal();
    }

    private void handleEditKereta(Kereta kereta) {
        if (kereta == null) {
            return;
        }
        keretaDiedit = kereta;
        lblModalTitle.setText("Edit Kereta");
        tfNama.setText(kereta.getNama());
        tfNomorKereta.setText(kereta.getNomorKereta());
        cbJenis.setValue(kereta.getJenis());
        tfKapasitas.setText(String.valueOf(kereta.getKapasitasTotal()));
        sembunyikanError();
        tampilkanModal();
    }

    private void handleHapusKereta(Kereta kereta) {
        if (kereta == null) {
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Kereta");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus \"" + kereta.getNama() + "\"?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            boolean berhasil = adminController.hapusKereta(kereta.getId());
            if (berhasil) {
                daftarKereta.remove(kereta);
            } else {
                tampilkanAlertError("Gagal menghapus kereta. Pastikan kereta tidak terpakai di jadwal aktif.");
            }
        }
    }

    @FXML
    private void handleSimpanKereta() {
        String nama = tfNama.getText().trim();
        String nomorKereta = tfNomorKereta.getText().trim();
        String jenis = cbJenis.getValue();
        String teksKapasitas = tfKapasitas.getText().trim();

        if (nama.isEmpty() || nomorKereta.isEmpty() || jenis == null || teksKapasitas.isEmpty()) {
            tampilkanError("Semua field wajib diisi.");
            return;
        }

        int kapasitas;
        try {
            kapasitas = Integer.parseInt(teksKapasitas);
            if (kapasitas <= 0) {
                tampilkanError("Kapasitas harus berupa angka positif.");
                return;
            }
        } catch (NumberFormatException e) {
            tampilkanError("Kapasitas harus berupa angka.");
            return;
        }

        boolean berhasil;
        if (keretaDiedit == null) {
            Kereta keretaBaru = new Kereta(nama, nomorKereta, jenis, kapasitas);
            berhasil = adminController.tambahKereta(keretaBaru);
            if (berhasil) {
                daftarKereta.add(keretaBaru);
            }
        } else {
            keretaDiedit.setNama(nama);
            keretaDiedit.setJenis(jenis);
            berhasil = adminController.editKereta(keretaDiedit);
            if (berhasil) {
                tblKereta.refresh();
            }
        }

        if (berhasil) {
            tutupModal();
        } else {
            tampilkanError("Gagal menyimpan data kereta. Silakan coba lagi.");
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
        keretaDiedit = null;
    }

    private void kosongkanForm() {
        tfNama.clear();
        tfNomorKereta.clear();
        cbJenis.setValue(null);
        tfKapasitas.clear();
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
        // sudah berada di halaman Kelola Kereta
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