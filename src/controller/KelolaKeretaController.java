package controller;

import dao.KeretaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaKeretaController implements Initializable {

    @FXML private Label lblAdminName;
    @FXML private TextField tfSearch;

    @FXML private TableView<Kereta> tblKereta;
    @FXML private TableColumn<Kereta, Integer> colNo;
    @FXML private TableColumn<Kereta, String> colNama;
    @FXML private TableColumn<Kereta, String> colNomorKereta;
    @FXML private TableColumn<Kereta, Integer> colJumlahGerbong;
    @FXML private TableColumn<Kereta, Integer> colKapasitas;
    @FXML private TableColumn<Kereta, String> colKelas;
    @FXML private TableColumn<Kereta, String> colStatus;
    @FXML private TableColumn<Kereta, Void> colAksi;

    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitle;
    @FXML private TextField tfNama;
    @FXML private TextField tfNomorKereta;
    @FXML private TextField tfGerbongEksekutif;
    @FXML private TextField tfGerbongBisnis;
    @FXML private TextField tfGerbongEkonomi;
    @FXML private TextField tfKalkulasi;
    @FXML private CheckBox chkEksekutif;
    @FXML private CheckBox chkBisnis;
    @FXML private CheckBox chkEkonomi;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblModalError;
    @FXML private Button btnSimpan;

    private final KeretaDAO keretaDAO = new KeretaDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Kereta> daftarKereta;
    private FilteredList<Kereta> daftarKeretaTersaring;

    private Kereta keretaDiedit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getUsername());

        cbStatus.setItems(FXCollections.observableArrayList(Kereta.STATUS_AKTIF, Kereta.STATUS_NON_AKTIF));
        setupTabel();
        muatDataKereta();
        setupPencarian();
        setupKalkulasiOtomatis();
    }

    private void setupKalkulasiOtomatis() {
        javafx.beans.value.ChangeListener<String> listener = (obs, oldVal, newVal) -> {
            int eks = parseInteger(tfGerbongEksekutif.getText());
            int bis = parseInteger(tfGerbongBisnis.getText());
            int eko = parseInteger(tfGerbongEkonomi.getText());
            int totalGerbong = eks + bis + eko;
            int totalKapasitas = eks * 50 + bis * 60 + eko * 80;
            tfKalkulasi.setText(totalKapasitas + " Kursi (Total Gerbong: " + totalGerbong + ")");
        };
        tfGerbongEksekutif.textProperty().addListener(listener);
        tfGerbongBisnis.textProperty().addListener(listener);
        tfGerbongEkonomi.textProperty().addListener(listener);
    }

    private int parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(tblKereta.getItems().indexOf(data.getValue()) + 1));

        colNama.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNama()));

        colNomorKereta.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNomorKereta()));

        colJumlahGerbong.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getJumlahGerbong()));

        colKapasitas.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getKapasitasTotal()));

        colKelas.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getKelasTersedia()));

        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean kosong) {
                super.updateItem(status, kosong);
                if (kosong || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("badge-status");
                badge.getStyleClass().add(Kereta.STATUS_AKTIF.equalsIgnoreCase(status)
                        ? "badge-aktif" : "badge-nonaktif");
                setGraphic(badge);
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
                            || teksAman(kereta.getKelasTersedia()).toLowerCase().contains(kueri)
                            || teksAman(kereta.getStatus()).toLowerCase().contains(kueri)
            );
        });
    }

    @FXML
    private void handleTambahKereta() {
        keretaDiedit = null;
        lblModalTitle.setText("Tambah Kereta");
        btnSimpan.setText("Simpan");
        kosongkanForm();
        tampilkanModal();
    }

    private void handleEditKereta(Kereta kereta) {
        if (kereta == null) {
            return;
        }
        keretaDiedit = kereta;
        lblModalTitle.setText("Edit Kereta");
        btnSimpan.setText("Perbarui");
        tfNama.setText(kereta.getNama());
        tfNomorKereta.setText(kereta.getNomorKereta());
        tfGerbongEksekutif.setText(String.valueOf(kereta.getGerbongEksekutif()));
        tfGerbongBisnis.setText(String.valueOf(kereta.getGerbongBisnis()));
        tfGerbongEkonomi.setText(String.valueOf(kereta.getGerbongEkonomi()));
        int totalGerbong = kereta.getGerbongEksekutif() + kereta.getGerbongBisnis() + kereta.getGerbongEkonomi();
        tfKalkulasi.setText(kereta.getKapasitasTotal() + " Kursi (Total Gerbong: " + totalGerbong + ")");
        
        setKelasDipilih(kereta.getKelasTersedia());
        cbStatus.setValue(kereta.getStatus());
        sembunyikanError();
        tampilkanModal();
    }

    private void handleHapusKereta(Kereta kereta) {
        if (kereta == null) {
            return;
        }

        ButtonType btnYa = new ButtonType("Ya, Hapus", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTidak = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> konfirmasi = new Dialog<>();
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus " + kereta.getNomorKereta() + " — " + kereta.getNama() + "?");
        konfirmasi.setContentText("Data kereta ini akan dihapus secara permanen dari sistem.");
        konfirmasi.getDialogPane().getButtonTypes().addAll(btnYa, btnTidak);

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isEmpty() || hasil.get() != btnYa) {
            return;
        }

        String jadwalAktif = keretaDAO.getJadwalAktifPertama(kereta.getId());
        if (jadwalAktif != null) {
            tampilkanDialogBlokir(
                kereta.getNomorKereta(),
                "Kereta " + kereta.getNomorKereta() + " — " + kereta.getNama()
                    + " masih terjadwal pada:\n" + jadwalAktif
                    + "\n\nSilakan hapus/ubah jadwal tersebut terlebih dahulu,"
                    + " atau ubah status kereta ke NON-AKTIF."
            );
            return;
        }

        boolean berhasil = adminController.hapusKereta(kereta.getId());
        if (berhasil) {
            daftarKereta.remove(kereta);
        } else {
            tampilkanAlertError("Gagal menghapus kereta. Data ini masih dirujuk oleh jadwal atau riwayat transaksi.");
        }
    }

    private void tampilkanDialogBlokir(String nomorKereta, String pesanDetail) {
        ButtonType btnLihatJadwal = new ButtonType("Lihat Jadwal Terkait →", ButtonBar.ButtonData.OTHER);
        ButtonType btnTutup = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Penghapusan Diblokir");
        dialog.setHeaderText("⚠  Kereta " + nomorKereta + " tidak dapat dihapus");
        dialog.setContentText(pesanDetail);
        dialog.getDialogPane().getButtonTypes().addAll(btnLihatJadwal, btnTutup);
        dialog.getDialogPane().setPrefWidth(480);

        Optional<ButtonType> pilihan = dialog.showAndWait();
        if (pilihan.isPresent() && pilihan.get() == btnLihatJadwal) {
            SceneManager.switchScene("KelolaJadwal.fxml");
        }
    }

    @FXML
    private void handleSimpanKereta() {
        String nama = tfNama.getText().trim();
        String nomorKereta = tfNomorKereta.getText().trim();
        String status = cbStatus.getValue();

        if (nama.isEmpty() || nomorKereta.isEmpty() || status == null) {
            tampilkanError("Semua field wajib diisi.");
            return;
        }

        int eks = parseInteger(tfGerbongEksekutif.getText());
        int bis = parseInteger(tfGerbongBisnis.getText());
        int eko = parseInteger(tfGerbongEkonomi.getText());
        int totalGerbong = eks + bis + eko;

        if (totalGerbong <= 0) {
            tampilkanError("Masukkan jumlah gerbong minimal 1.");
            return;
        }

        String kelasTersedia = kumpulkanKelasDipilih();
        if (kelasTersedia.isEmpty()) {
            tampilkanError("Pilih minimal satu kelas (dengan mengisi gerbong).");
            return;
        }

        Kereta keretaDenganNomorSama = keretaDAO.findByNomorKereta(nomorKereta);
        boolean nomorDipakaiKeretaLain = keretaDenganNomorSama != null
                && (keretaDiedit == null || keretaDenganNomorSama.getId() != keretaDiedit.getId());
        if (nomorDipakaiKeretaLain) {
            tampilkanError("Nomor KA \"" + nomorKereta + "\" sudah digunakan oleh "
                    + keretaDenganNomorSama.getNama() + ".");
            return;
        }

        if (keretaDiedit != null
                && Kereta.STATUS_NON_AKTIF.equals(status)
                && !Kereta.STATUS_NON_AKTIF.equalsIgnoreCase(keretaDiedit.getStatus())) {
            String jadwalDenganPenumpang = keretaDAO.getJadwalDenganTiketAktifPertama(keretaDiedit.getId());
            if (jadwalDenganPenumpang != null) {
                tampilkanError("Kereta ini memiliki jadwal aktif pada " + jadwalDenganPenumpang
                        + ". Batalkan/pindahkan tiket aktif terlebih dahulu sebelum mengubah status ke NON-AKTIF.");
                return;
            }
        }

        Kereta dataKereta = new Kereta(nama, nomorKereta, eks, bis, eko, kelasTersedia, status);
        boolean berhasil;
        if (keretaDiedit == null) {
            berhasil = adminController.tambahKereta(dataKereta);
            if (berhasil) {
                daftarKereta.add(dataKereta);
            }
        } else {
            dataKereta.setId(keretaDiedit.getId());
            berhasil = adminController.editKereta(dataKereta);
            if (berhasil) {
                int indeks = daftarKereta.indexOf(keretaDiedit);
                daftarKereta.set(indeks, dataKereta);
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
        btnSimpan.setText("Simpan");
    }

    private void kosongkanForm() {
        tfNama.clear();
        tfNomorKereta.clear();
        tfGerbongEksekutif.clear();
        tfGerbongBisnis.clear();
        tfGerbongEkonomi.clear();
        tfKalkulasi.setText("0 Kursi (Total Gerbong: 0)");
        chkEksekutif.setSelected(false);
        chkBisnis.setSelected(false);
        chkEkonomi.setSelected(false);
        cbStatus.setValue(Kereta.STATUS_AKTIF);
        sembunyikanError();
    }

    private String kumpulkanKelasDipilih() {
        List<String> kelas = new ArrayList<>();
        int eks = parseInteger(tfGerbongEksekutif.getText());
        int bis = parseInteger(tfGerbongBisnis.getText());
        int eko = parseInteger(tfGerbongEkonomi.getText());
        if (eks > 0) {
            kelas.add("Eksekutif");
        }
        if (bis > 0) {
            kelas.add("Bisnis");
        }
        if (eko > 0) {
            kelas.add("Ekonomi");
        }
        return String.join(", ", kelas);
    }

    private void setKelasDipilih(String kelasTersedia) {
        chkEksekutif.setSelected(false);
        chkBisnis.setSelected(false);
        chkEkonomi.setSelected(false);

        if (kelasTersedia == null || kelasTersedia.isBlank()) {
            return;
        }

        for (String item : kelasTersedia.split(",")) {
            String kelas = item.trim().toLowerCase();
            switch (kelas) {
                case "eksekutif" -> chkEksekutif.setSelected(true);
                case "bisnis" -> chkBisnis.setSelected(true);
                case "ekonomi" -> chkEkonomi.setSelected(true);
                default -> {
                }
            }
        }
    }

    private String teksAman(String teks) {
        return teks == null ? "" : teks;
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