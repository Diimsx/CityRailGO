package controller;

import dao.JadwalDAO;
import dao.JenisKelasDAO;
import dao.KeretaDAO;
import dao.RuteDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.Jadwal;
import model.JenisKelas;
import model.Kereta;
import model.Rute;
import org.kordamp.ikonli.javafx.FontIcon;
import util.DateTimeUtil;
import util.SceneManager;
import util.SessionManager;
import util.TiketHelper;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

public class KelolaJadwalController implements Initializable {

    private static final DateTimeFormatter FORMAT_JAM = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label lblAdminName;
    @FXML private TextField tfSearch;

    @FXML private TableView<Jadwal> tblJadwal;
    @FXML private TableColumn<Jadwal, Integer> colNo;
    @FXML private TableColumn<Jadwal, String> colKereta;
    @FXML private TableColumn<Jadwal, String> colRute;
    @FXML private TableColumn<Jadwal, String> colJenisKelas;
    @FXML private TableColumn<Jadwal, String> colBerangkat;
    @FXML private TableColumn<Jadwal, String> colTiba;
    @FXML private TableColumn<Jadwal, String> colStatus;
    @FXML private TableColumn<Jadwal, Void> colAksi;

    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitle;
    @FXML private ComboBox<Kereta> cbKereta;
    @FXML private ComboBox<Rute> cbRute;
    @FXML private ComboBox<JenisKelas> cbJenisKelas;
    @FXML private DatePicker dpTanggal;
    @FXML private TextField tfJamBerangkat;
    @FXML private Label lblEstimasiTiba;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblModalError;
    @FXML private Button btnSimpan;

    private final JadwalDAO jadwalDAO = new JadwalDAO();
    private final KeretaDAO keretaDAO = new KeretaDAO();
    private final RuteDAO ruteDAO = new RuteDAO();
    private final JenisKelasDAO jenisKelasDAO = new JenisKelasDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Jadwal> daftarJadwal;
    private FilteredList<Jadwal> daftarJadwalTersaring;

    private Jadwal jadwalDiedit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());

        setupComboSumberData();
        setupEstimasiTibaListener();
        setupTabel();
        muatDataJadwal();
        setupPencarian();
    }

    private void setupComboSumberData() {
        cbKereta.setItems(FXCollections.observableArrayList(keretaDAO.findAktif()));
        cbRute.setItems(FXCollections.observableArrayList(ruteDAO.findAll()));
        cbJenisKelas.setItems(FXCollections.observableArrayList(jenisKelasDAO.findAll()));
        cbStatus.setItems(FXCollections.observableArrayList("TERSEDIA", "PENUH", "DIBATALKAN"));

        setupComboDisplay(cbKereta, k -> k.getNama() + " - " + k.getNomorKereta()
                + " (" + k.getKapasitasTotal() + " kursi, " + k.getKelasTersedia() + ")");
        setupComboDisplay(cbRute, r -> r.getStasiunAsal() + " \u2192 " + r.getStasiunTujuan()
                + " (" + String.format("%.0f", r.getJarakKm()) + " km)");
        setupComboDisplay(cbJenisKelas, jk -> jk.getNamaKelas() + " (" + TiketHelper.formatHarga(jk.getHargaPerKm()) + "/km)");
    }

    private <T> void setupComboDisplay(ComboBox<T> comboBox, Function<T, String> formatter) {
        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean kosong) {
                super.updateItem(item, kosong);
                setText(kosong || item == null ? null : formatter.apply(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean kosong) {
                super.updateItem(item, kosong);
                setText(kosong || item == null ? null : formatter.apply(item));
            }
        });
    }

    private void setupEstimasiTibaListener() {
        cbRute.valueProperty().addListener((obs, lama, baru) -> perbaruiEstimasiTiba());
        dpTanggal.valueProperty().addListener((obs, lama, baru) -> perbaruiEstimasiTiba());
        tfJamBerangkat.textProperty().addListener((obs, lama, baru) -> perbaruiEstimasiTiba());
    }

    private void perbaruiEstimasiTiba() {
        LocalDateTime waktuTiba = hitungWaktuTiba();
        lblEstimasiTiba.setText(
                waktuTiba != null
                        ? "Estimasi tiba: " + DateTimeUtil.formatWaktu(waktuTiba)
                        : "Estimasi tiba: \u2014"
        );
    }

    private LocalDateTime hitungWaktuTiba() {
        LocalDateTime waktuBerangkat = hitungWaktuBerangkat();
        Rute rute = cbRute.getValue();
        if (waktuBerangkat == null || rute == null) {
            return null;
        }
        return waktuBerangkat.plusMinutes(rute.getEstimasiMenit());
    }

    private LocalDateTime hitungWaktuBerangkat() {
        LocalDate tanggal = dpTanggal.getValue();
        if (tanggal == null) {
            return null;
        }
        try {
            LocalTime jam = LocalTime.parse(tfJamBerangkat.getText().trim(), FORMAT_JAM);
            return LocalDateTime.of(tanggal, jam);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
                new SimpleObjectProperty<>(tblJadwal.getItems().indexOf(data.getValue()) + 1));

        colKereta.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getKereta().getNama()));

        colRute.setCellValueFactory(data -> {
            Rute rute = data.getValue().getRute();
            return new SimpleStringProperty(rute.getStasiunAsal() + " \u2192 " + rute.getStasiunTujuan());
        });

        colJenisKelas.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getJenisKelas().getNamaKelas()));

        colBerangkat.setCellValueFactory(data ->
                new SimpleStringProperty(DateTimeUtil.formatWaktu(data.getValue().getWaktuBerangkat())));

        colTiba.setCellValueFactory(data ->
                new SimpleStringProperty(DateTimeUtil.formatWaktu(data.getValue().getWaktuTiba())));

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));
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
                badge.getStyleClass().add(kelasBadgeStatus(status));
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
                container.setAlignment(Pos.CENTER_LEFT);

                iconEdit.setOnMouseClicked(e -> handleEditJadwal(getTableRow().getItem()));
                iconHapus.setOnMouseClicked(e -> handleHapusJadwal(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : container);
            }
        });
    }

    private String kelasBadgeStatus(String status) {
        return switch (status.toUpperCase()) {
            case "TERSEDIA" -> "badge-tersedia";
            case "PENUH" -> "badge-penuh";
            case "DIBATALKAN" -> "badge-dibatalkan";
            default -> "badge-default";
        };
    }

    private void muatDataJadwal() {
        daftarJadwal = FXCollections.observableArrayList(jadwalDAO.findAll());
        daftarJadwalTersaring = new FilteredList<>(daftarJadwal, j -> true);
        tblJadwal.setItems(daftarJadwalTersaring);
    }

    private void setupPencarian() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> {
            String kueri = baru == null ? "" : baru.trim().toLowerCase();
            daftarJadwalTersaring.setPredicate(jadwal ->
                    kueri.isEmpty()
                            || jadwal.getKereta().getNama().toLowerCase().contains(kueri)
                            || jadwal.getRute().getStasiunAsal().toLowerCase().contains(kueri)
                            || jadwal.getRute().getStasiunTujuan().toLowerCase().contains(kueri)
            );
        });
    }

    @FXML
    private void handleTambahJadwal() {
        jadwalDiedit = null;
        lblModalTitle.setText("Tambah Jadwal");
        kosongkanForm();
        tampilkanModal();
    }

    private void handleEditJadwal(Jadwal jadwal) {
        if (jadwal == null) {
            return;
        }
        jadwalDiedit = jadwal;
        lblModalTitle.setText("Edit Jadwal");

        cbKereta.setValue(cariById(cbKereta.getItems(), jadwal.getKereta().getId(), Kereta::getId));
        cbRute.setValue(cariById(cbRute.getItems(), jadwal.getRute().getId(), Rute::getId));
        cbJenisKelas.setValue(cariById(cbJenisKelas.getItems(), jadwal.getJenisKelas().getId(), JenisKelas::getId));
        dpTanggal.setValue(jadwal.getWaktuBerangkat().toLocalDate());
        tfJamBerangkat.setText(jadwal.getWaktuBerangkat().toLocalTime().format(FORMAT_JAM));
        cbStatus.setValue(jadwal.getStatus());

        sembunyikanError();
        tampilkanModal();
    }

    private <T> T cariById(ObservableList<T> daftar, int id, Function<T, Integer> idGetter) {
        return daftar.stream().filter(item -> idGetter.apply(item) == id).findFirst().orElse(null);
    }

    private void handleHapusJadwal(Jadwal jadwal) {
        if (jadwal == null) {
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Jadwal");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus jadwal "
                + jadwal.getKereta().getNama() + " (" + DateTimeUtil.formatWaktu(jadwal.getWaktuBerangkat()) + ")?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            boolean berhasil = adminController.hapusJadwal(jadwal.getId());
            if (berhasil) {
                daftarJadwal.remove(jadwal);
            } else {
                tampilkanAlertError("Gagal menghapus jadwal. Pastikan tidak ada kursi atau tiket yang terkait jadwal ini.");
            }
        }
    }

    @FXML
    private void handleSimpanJadwal() {
        Kereta kereta = cbKereta.getValue();
        Rute rute = cbRute.getValue();
        JenisKelas jenisKelas = cbJenisKelas.getValue();
        String status = cbStatus.getValue();
        LocalDateTime waktuBerangkat = hitungWaktuBerangkat();

        if (kereta == null || rute == null || jenisKelas == null || status == null) {
            tampilkanError("Kereta, rute, jenis kelas, dan status wajib dipilih.");
            return;
        }
        if (waktuBerangkat == null) {
            tampilkanError("Tanggal dan jam berangkat wajib diisi dengan format yang benar (HH:mm).");
            return;
        }

        LocalDateTime waktuTiba = waktuBerangkat.plusMinutes(rute.getEstimasiMenit());

        Jadwal jadwalBaru = new Jadwal(kereta, rute, jenisKelas, waktuBerangkat, waktuTiba);
        jadwalBaru.setStatus(status);

        boolean berhasil;
        if (jadwalDiedit == null) {
            berhasil = adminController.tambahJadwal(jadwalBaru);
            if (berhasil) {
                daftarJadwal.add(jadwalBaru);
            }
        } else {
            jadwalBaru.setId(jadwalDiedit.getId());
            berhasil = adminController.editJadwal(jadwalBaru);
            if (berhasil) {
                int indeks = daftarJadwal.indexOf(jadwalDiedit);
                daftarJadwal.set(indeks, jadwalBaru);
            }
        }

        if (berhasil) {
            tutupModal();
        } else {
            tampilkanError("Gagal menyimpan jadwal. Silakan coba lagi.");
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
        jadwalDiedit = null;
    }

    private void kosongkanForm() {
        cbKereta.setValue(null);
        cbRute.setValue(null);
        cbJenisKelas.setValue(null);
        dpTanggal.setValue(null);
        tfJamBerangkat.clear();
        cbStatus.setValue("TERSEDIA");
        lblEstimasiTiba.setText("Estimasi tiba: \u2014");
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
        SceneManager.switchScene("KelolaRute.fxml");
    }

    @FXML
    private void handleNavJadwal() {
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
