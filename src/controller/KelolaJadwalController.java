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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Jadwal;
import model.JenisKelas;
import model.Kereta;
import model.Rute;
import org.kordamp.ikonli.javafx.FontIcon;
import util.DateTimeUtil;
import util.PricingEngine;
import util.PricingEngine.AturanHarga;
import util.PricingEngine.HasilPerhitungan;
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

    private static final DateTimeFormatter FORMAT_JAM  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMAT_TABEL = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    // ===== Table =====
    @FXML private Label               lblAdminName;
    @FXML private TextField           tfSearch;
    @FXML private TableView<Jadwal>   tblJadwal;
    @FXML private TableColumn<Jadwal, Integer> colNo;
    @FXML private TableColumn<Jadwal, String>  colKereta;
    @FXML private TableColumn<Jadwal, String>  colRute;
    @FXML private TableColumn<Jadwal, String>  colJenisKelas;
    @FXML private TableColumn<Jadwal, String>  colBerangkat;
    @FXML private TableColumn<Jadwal, String>  colTiba;
    @FXML private TableColumn<Jadwal, String>  colHarga;
    @FXML private TableColumn<Jadwal, String>  colStatus;
    @FXML private TableColumn<Jadwal, Void>    colAksi;

    // ===== Modal =====
    @FXML private StackPane          modalOverlay;
    @FXML private Label              lblModalTitle;
    @FXML private ComboBox<Kereta>   cbKereta;
    @FXML private ComboBox<Rute>     cbRute;
    @FXML private ComboBox<JenisKelas> cbJenisKelas;
    @FXML private DatePicker         dpTanggal;
    @FXML private TextField          tfJamBerangkat;
    @FXML private Label              lblEstimasiTiba;
    @FXML private ComboBox<String>   cbStatus;
    @FXML private Label              lblModalError;
    @FXML private Button             btnSimpan;

    // ===== Panel Harga Live =====
    @FXML private VBox   vboxHarga;
    @FXML private Label  lblHargaDasar;
    @FXML private VBox   vboxAturan;
    @FXML private Label  lblHargaFinal;

    // ===== DAO & State =====
    private final JadwalDAO     jadwalDAO     = new JadwalDAO();
    private final KeretaDAO     keretaDAO     = new KeretaDAO();
    private final RuteDAO       ruteDAO       = new RuteDAO();
    private final JenisKelasDAO jenisKelasDAO = new JenisKelasDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Jadwal> daftarJadwal;
    private FilteredList<Jadwal>   daftarJadwalTersaring;

    private Jadwal jadwalDiedit;

    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getUsername());
        setupComboSumberData();
        setupListenerEstimasiDanHarga();
        setupTabel();
        muatDataJadwal();
        setupPencarian();
    }

    private void setupComboSumberData() {
        cbKereta.setItems(FXCollections.observableArrayList(keretaDAO.findAktif()));
        cbRute.setItems(FXCollections.observableArrayList(ruteDAO.findAll()));
        cbJenisKelas.setItems(FXCollections.observableArrayList(jenisKelasDAO.findAll()));
        cbStatus.setItems(FXCollections.observableArrayList("TERSEDIA", "PENUH", "DIBATALKAN"));

        setupComboDisplay(cbKereta,
            k -> k.getNama() + " — " + k.getNomorKereta() + " (" + k.getKapasitasTotal() + " kursi)");
        setupComboDisplay(cbRute,
            r -> r.getStasiunAsal() + " \u2192 " + r.getStasiunTujuan()
                + " (" + String.format("%.0f", r.getJarakKm()) + " km)");
        setupComboDisplay(cbJenisKelas,
            jk -> jk.getNamaKelas() + " (" + TiketHelper.formatHarga(jk.getHargaPerKm()) + "/km)");
    }

    private <T> void setupComboDisplay(ComboBox<T> cb, Function<T, String> fmt) {
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.apply(item));
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.apply(item));
            }
        });
    }

    // =========================================================
    // LISTENERS: Estimasi Tiba + Live Pricing Preview
    // =========================================================

    private void setupListenerEstimasiDanHarga() {
        // Setiap kali salah satu input berubah, update estimasi + harga
        cbRute.valueProperty().addListener((obs, o, n)         -> perbaruiPreview());
        cbJenisKelas.valueProperty().addListener((obs, o, n)   -> perbaruiPreview());
        dpTanggal.valueProperty().addListener((obs, o, n)      -> perbaruiPreview());
        tfJamBerangkat.textProperty().addListener((obs, o, n)  -> perbaruiPreview());
    }

    /**
     * Dipanggil setiap kali input form berubah.
     * Update: (1) label estimasi tiba, (2) panel preview harga live.
     */
    private void perbaruiPreview() {
        LocalDateTime waktuBerangkat = hitungWaktuBerangkat();
        Rute rute = cbRute.getValue();
        JenisKelas kelas = cbJenisKelas.getValue();

        // --- Update estimasi tiba ---
        if (waktuBerangkat != null && rute != null) {
            LocalDateTime tiba = waktuBerangkat.plusMinutes(rute.getEstimasiMenit());
            lblEstimasiTiba.setText("Estimasi tiba: " + DateTimeUtil.formatWaktu(tiba));
        } else {
            lblEstimasiTiba.setText("Estimasi tiba: \u2014");
        }

        // --- Update panel harga ---
        if (waktuBerangkat != null && rute != null && kelas != null) {
            HasilPerhitungan hasil = PricingEngine.hitung(
                kelas.getHargaPerKm(), rute.getJarakKm(), waktuBerangkat
            );
            tampilkanPanelHarga(hasil);
        } else {
            sembunyikanPanelHarga();
        }
    }

    private void tampilkanPanelHarga(HasilPerhitungan hasil) {
        // Harga dasar
        lblHargaDasar.setText(PricingEngine.formatRupiah(hasil.hargaDasar));

        // Baris aturan aktif
        vboxAturan.getChildren().clear();
        if (hasil.aturanAktif.isEmpty()) {
            Label lblStandar = new Label("Tidak ada penyesuaian harga");
            lblStandar.getStyleClass().add("harga-standar-label");
            vboxAturan.getChildren().add(lblStandar);
        } else {
            for (AturanHarga aturan : hasil.aturanAktif) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("aturan-row");

                Label lblNama = new Label(aturan.emoji + " " + aturan.nama);
                lblNama.getStyleClass().add("aturan-label");

                String sign = aturan.persentase >= 0 ? "+" : "";
                String pctStr = sign + (int)(aturan.persentase * 100) + "%";
                Label lblPct = new Label(pctStr);
                lblPct.getStyleClass().add(aturan.persentase >= 0 ? "aturan-surcharge" : "aturan-diskon");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                row.getChildren().addAll(lblNama, spacer, lblPct);
                vboxAturan.getChildren().add(row);
            }
        }

        // Harga final
        lblHargaFinal.setText(PricingEngine.formatRupiah(hasil.hargaFinal));
        // Warna biru jika ada adjustment, default jika tidak
        lblHargaFinal.getStyleClass().removeAll("harga-value-final-adjusted");
        if (hasil.adaAdjustment()) {
            lblHargaFinal.setStyle("-fx-text-fill: #2979FF;");
        } else {
            lblHargaFinal.setStyle("-fx-text-fill: #0A192F;");
        }

        vboxHarga.setVisible(true);
        vboxHarga.setManaged(true);
    }

    private void sembunyikanPanelHarga() {
        vboxHarga.setVisible(false);
        vboxHarga.setManaged(false);
    }

    // =========================================================
    // TABEL SETUP
    // =========================================================

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
            new SimpleObjectProperty<>(tblJadwal.getItems().indexOf(data.getValue()) + 1));

        colKereta.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getKereta().getNama()
                + "\n" + data.getValue().getKereta().getNomorKereta()));

        colRute.setCellValueFactory(data -> {
            Rute r = data.getValue().getRute();
            return new SimpleStringProperty(r.getStasiunAsal() + " \u2192 " + r.getStasiunTujuan());
        });

        colJenisKelas.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getJenisKelas().getNamaKelas()));

        colBerangkat.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getWaktuBerangkat().format(FORMAT_TABEL)));

        colTiba.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getWaktuTiba().format(FORMAT_TABEL)));

        // Kolom Harga — biru jika hargaFinal ada adjustment dari base
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean kosong) {
                super.updateItem(item, kosong);
                if (kosong || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle(null); return;
                }
                Jadwal j = (Jadwal) getTableRow().getItem();
                double efektif = j.getHargaEfektif();
                double dasar   = j.getJenisKelas().hitungHarga(j.getRute().getJarakKm());
                setText(PricingEngine.formatRupiah(efektif));
                setStyle(efektif != dasar
                    ? "-fx-font-weight: bold; -fx-text-fill: #2979FF;"
                    : "-fx-font-weight: bold; -fx-text-fill: #0A192F;");
            }
        });

        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean kosong) {
                super.updateItem(status, kosong);
                if (kosong || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().addAll("badge-status", kelasBadgeStatus(status));
                setGraphic(badge);
                setText(null);
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final FontIcon iconEdit  = new FontIcon("fas-edit");
            private final FontIcon iconHapus = new FontIcon("fas-trash-alt");
            private final HBox container     = new HBox(14, iconEdit, iconHapus);
            {
                iconEdit.getStyleClass().add("action-icon-edit");
                iconHapus.getStyleClass().add("action-icon-delete");
                container.setAlignment(Pos.CENTER_LEFT);
                iconEdit.setOnMouseClicked(e  -> handleEditJadwal(getTableRow().getItem()));
                iconHapus.setOnMouseClicked(e -> handleHapusJadwal(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : container);
            }
        });
    }

    private String kelasBadgeStatus(String status) {
        return switch (status.toUpperCase()) {
            case "TERSEDIA"   -> "badge-tersedia";
            case "PENUH"      -> "badge-penuh";
            case "DIBATALKAN" -> "badge-dibatalkan";
            default           -> "badge-default";
        };
    }

    private void muatDataJadwal() {
        daftarJadwal = FXCollections.observableArrayList(jadwalDAO.findAll());
        daftarJadwalTersaring = new FilteredList<>(daftarJadwal, j -> true);
        tblJadwal.setItems(daftarJadwalTersaring);
    }

    private void setupPencarian() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> {
            String q = baru == null ? "" : baru.trim().toLowerCase();
            daftarJadwalTersaring.setPredicate(j ->
                q.isEmpty()
                || j.getKereta().getNama().toLowerCase().contains(q)
                || j.getKereta().getNomorKereta().toLowerCase().contains(q)
                || j.getRute().getStasiunAsal().toLowerCase().contains(q)
                || j.getRute().getStasiunTujuan().toLowerCase().contains(q)
                || j.getJenisKelas().getNamaKelas().toLowerCase().contains(q)
            );
        });
    }

    // =========================================================
    // CRUD HANDLERS
    // =========================================================

    @FXML
    private void handleTambahJadwal() {
        jadwalDiedit = null;
        lblModalTitle.setText("Tambah Jadwal Baru");
        btnSimpan.setText("Simpan");
        kosongkanForm();
        tampilkanModal();
    }

    private void handleEditJadwal(Jadwal jadwal) {
        if (jadwal == null) return;
        jadwalDiedit = jadwal;
        lblModalTitle.setText("Edit Jadwal");
        btnSimpan.setText("Perbarui");

        cbKereta.setValue(cariById(cbKereta.getItems(), jadwal.getKereta().getId(), Kereta::getId));
        cbRute.setValue(cariById(cbRute.getItems(), jadwal.getRute().getId(), Rute::getId));
        cbJenisKelas.setValue(cariById(cbJenisKelas.getItems(), jadwal.getJenisKelas().getId(), JenisKelas::getId));
        dpTanggal.setValue(jadwal.getWaktuBerangkat().toLocalDate());
        tfJamBerangkat.setText(jadwal.getWaktuBerangkat().toLocalTime().format(FORMAT_JAM));
        cbStatus.setValue(jadwal.getStatus());

        sembunyikanError();
        tampilkanModal();
        // Trigger preview setelah autofill
        perbaruiPreview();
    }

    private void handleHapusJadwal(Jadwal jadwal) {
        if (jadwal == null) return;

        ButtonType btnYa    = new ButtonType("Ya, Hapus", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTidak = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> konfirmasi = new Dialog<>();
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus jadwal " + jadwal.getKereta().getNama() + "?");
        konfirmasi.setContentText(
            "Rute: " + jadwal.getRute().getStasiunAsal() + " \u2192 " + jadwal.getRute().getStasiunTujuan()
            + "\nBerangkat: " + DateTimeUtil.formatWaktu(jadwal.getWaktuBerangkat())
            + "\n\nSemua data kursi terkait akan ikut terhapus."
        );
        konfirmasi.getDialogPane().getButtonTypes().addAll(btnYa, btnTidak);

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isEmpty() || hasil.get() != btnYa) return;

        boolean berhasil = adminController.hapusJadwal(jadwal.getId());
        if (berhasil) {
            daftarJadwal.remove(jadwal);
        } else {
            tampilkanAlertError("Gagal menghapus jadwal. Pastikan tidak ada tiket aktif yang terkait jadwal ini.");
        }
    }

    @FXML
    private void handleSimpanJadwal() {
        Kereta     kereta    = cbKereta.getValue();
        Rute       rute      = cbRute.getValue();
        JenisKelas kelas     = cbJenisKelas.getValue();
        String     status    = cbStatus.getValue();
        LocalDateTime waktuBerangkat = hitungWaktuBerangkat();

        // ===== Validasi field wajib =====
        if (kereta == null) { tampilkanError("Pilih kereta terlebih dahulu."); return; }
        if (rute == null)   { tampilkanError("Pilih rute terlebih dahulu."); return; }
        if (kelas == null)  { tampilkanError("Pilih jenis kelas terlebih dahulu."); return; }
        if (status == null) { tampilkanError("Pilih status jadwal."); return; }
        if (waktuBerangkat == null) {
            tampilkanError("Tanggal dan jam berangkat wajib diisi (format jam: HH:mm, contoh: 08:00)."); return;
        }
        if (waktuBerangkat.isBefore(LocalDateTime.now().minusMinutes(5)) && jadwalDiedit == null) {
            tampilkanError("Waktu keberangkatan tidak boleh di masa lalu."); return;
        }

        LocalDateTime waktuTiba = waktuBerangkat.plusMinutes(rute.getEstimasiMenit());

        // ===== Validasi konflik kereta =====
        int kecualiId = jadwalDiedit != null ? jadwalDiedit.getId() : 0;
        String konflik = jadwalDAO.cekKonflikKereta(kereta.getId(), waktuBerangkat, waktuTiba, kecualiId);
        if (konflik != null) {
            tampilkanError(
                "Kereta \"" + kereta.getNama() + "\" sudah dijadwalkan pada waktu yang berdekatan:\n"
                + "\u2022 " + konflik
                + "\n\nPilih kereta lain atau ubah waktu keberangkatan."
            );
            return;
        }

        // ===== Kalkulasi harga dengan dynamic pricing =====
        HasilPerhitungan pricing = PricingEngine.hitung(
            kelas.getHargaPerKm(), rute.getJarakKm(), waktuBerangkat
        );

        // ===== Buat objek Jadwal =====
        Jadwal jadwalBaru = new Jadwal(kereta, rute, kelas, waktuBerangkat, waktuTiba);
        jadwalBaru.setStatus(status);
        jadwalBaru.setHargaFinal(pricing.hargaFinal);
        jadwalBaru.setInfoHarga(pricing.getRingkasanAturan());

        boolean berhasil;
        if (jadwalDiedit == null) {
            berhasil = adminController.tambahJadwal(jadwalBaru);
            if (berhasil) daftarJadwal.add(jadwalBaru);
        } else {
            jadwalBaru.setId(jadwalDiedit.getId());
            berhasil = adminController.editJadwal(jadwalBaru);
            if (berhasil) {
                int indeks = daftarJadwal.indexOf(jadwalDiedit);
                if (indeks >= 0) daftarJadwal.set(indeks, jadwalBaru);
            }
        }

        if (berhasil) {
            tutupModal();
        } else {
            tampilkanError("Gagal menyimpan jadwal. Silakan coba lagi.");
        }
    }

    // =========================================================
    // MODAL HELPERS
    // =========================================================

    @FXML private void handleTutupModal() { tutupModal(); }

    private void tampilkanModal() {
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void tutupModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        jadwalDiedit = null;
        btnSimpan.setText("Simpan");
    }

    private void kosongkanForm() {
        cbKereta.setValue(null);
        cbRute.setValue(null);
        cbJenisKelas.setValue(null);
        dpTanggal.setValue(null);
        tfJamBerangkat.clear();
        cbStatus.setValue("TERSEDIA");
        lblEstimasiTiba.setText("Estimasi tiba: \u2014");
        sembunyikanPanelHarga();
        sembunyikanError();
    }

    // =========================================================
    // UTILS
    // =========================================================

    private LocalDateTime hitungWaktuBerangkat() {
        LocalDate tanggal = dpTanggal.getValue();
        if (tanggal == null) return null;
        try {
            LocalTime jam = LocalTime.parse(tfJamBerangkat.getText().trim(), FORMAT_JAM);
            return LocalDateTime.of(tanggal, jam);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    private <T> T cariById(ObservableList<T> daftar, int id, Function<T, Integer> idGetter) {
        return daftar.stream().filter(item -> idGetter.apply(item) == id).findFirst().orElse(null);
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

    // =========================================================
    // NAVIGASI
    // =========================================================

    @FXML private void handleNavDashboard() { SceneManager.switchScene("HomeAdmin.fxml"); }
    @FXML private void handleNavKereta()    { SceneManager.switchScene("KelolaKereta.fxml"); }
    @FXML private void handleNavRute()      { SceneManager.switchScene("KelolaRute.fxml"); }
    @FXML private void handleNavStasiun()   { SceneManager.switchScene("KelolaStasiun.fxml"); }
    @FXML private void handleNavJadwal()    { /* sudah di halaman ini */ }
    @FXML private void handleNavPromo()     { SceneManager.switchScene("KelolaPromo.fxml"); }
    @FXML private void handleNavLaporan()   { SceneManager.switchScene("LaporanPenjualan.fxml"); }

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
