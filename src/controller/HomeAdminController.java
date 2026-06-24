package controller;

import dao.JadwalDAO;
import dao.KeretaDAO;
import dao.PembayaranDAO;
import dao.PromoDAO;
import dao.TiketDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.*;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HomeAdminController implements Initializable {

    // ── Sidebar nav buttons ──────────────────────────────────────
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavKereta;
    @FXML private Button btnNavJadwal;
    @FXML private Button btnNavPromo;
    @FXML private Button btnNavPembayaran;
    @FXML private Button btnLogout;

    // ── Sidebar user info ────────────────────────────────────────
    @FXML private Label lblAdminName;
    @FXML private Label lblAdminRole;

    // ── Content panels ───────────────────────────────────────────
    @FXML private StackPane contentArea;
    @FXML private VBox panelDashboard;
    @FXML private VBox panelKereta;
    @FXML private VBox panelJadwal;
    @FXML private VBox panelPromo;
    @FXML private VBox panelPembayaran;

    // ── Dashboard stat labels ────────────────────────────────────
    @FXML private Label lblStatKereta;
    @FXML private Label lblStatJadwal;
    @FXML private Label lblStatTiket;
    @FXML private Label lblStatPromo;

    // ── Dashboard tabel pemesanan ─────────────────────────────────
    @FXML private TableView<Tiket> tblPemesananDash;
    @FXML private TableColumn<Tiket, String>  colDashKode;
    @FXML private TableColumn<Tiket, String>  colDashPenumpang;
    @FXML private TableColumn<Tiket, String>  colDashRute;
    @FXML private TableColumn<Tiket, String>  colDashTotal;
    @FXML private TableColumn<Tiket, String>  colDashStatus;

    // ── Dashboard jadwal hari ini ─────────────────────────────────
    @FXML private VBox boxJadwalDash;

    // ── Kereta panel ─────────────────────────────────────────────
    @FXML private TableView<Kereta> tblKereta;
    @FXML private TableColumn<Kereta, String> colKeretaNama;
    @FXML private TableColumn<Kereta, String> colKeretaNomor;
    @FXML private TableColumn<Kereta, String> colKeretaJenis;
    @FXML private TableColumn<Kereta, Integer> colKeretaKapasitas;
    @FXML private TextField tfKeretaNama;
    @FXML private TextField tfKeretaNomor;
    @FXML private TextField tfKeretaJenis;
    @FXML private TextField tfKeretaKapasitas;

    // ── Jadwal panel ─────────────────────────────────────────────
    @FXML private TableView<Jadwal> tblJadwal;
    @FXML private TableColumn<Jadwal, String> colJadwalKereta;
    @FXML private TableColumn<Jadwal, String> colJadwalRute;
    @FXML private TableColumn<Jadwal, String> colJadwalBerangkat;
    @FXML private TableColumn<Jadwal, String> colJadwalTiba;
    @FXML private TableColumn<Jadwal, String> colJadwalStatus;
    @FXML private TextField tfJadwalKeretaId;
    @FXML private TextField tfJadwalRuteId;
    @FXML private TextField tfJadwalKelasId;
    @FXML private TextField tfJadwalBerangkat;
    @FXML private TextField tfJadwalTiba;

    // ── Promo panel ───────────────────────────────────────────────
    @FXML private TableView<Promo> tblPromo;
    @FXML private TableColumn<Promo, String> colPromoKode;
    @FXML private TableColumn<Promo, String> colPromoDeskripsi;
    @FXML private TableColumn<Promo, Double> colPromoDiskon;
    @FXML private TableColumn<Promo, String> colPromoMulai;
    @FXML private TableColumn<Promo, String> colPromoAkhir;
    @FXML private TableColumn<Promo, String> colPromoStatus;
    @FXML private TextField tfPromoKode;
    @FXML private TextField tfPromoDeskripsi;
    @FXML private TextField tfPromoDiskon;
    @FXML private DatePicker dpPromoMulai;
    @FXML private DatePicker dpPromoAkhir;

    // ── Pembayaran panel ──────────────────────────────────────────
    @FXML private TableView<Pembayaran> tblPembayaran;
    @FXML private TableColumn<Pembayaran, Integer> colPembayaranId;
    @FXML private TableColumn<Pembayaran, Double> colPembayaranJumlah;
    @FXML private TableColumn<Pembayaran, String> colPembayaranMetode;
    @FXML private TableColumn<Pembayaran, String> colPembayaranStatus;
    @FXML private TableColumn<Pembayaran, String> colPembayaranTanggal;

    // ── Status bar ────────────────────────────────────────────────
    @FXML private Label lblStatus;

    // ── DAOs & controllers ────────────────────────────────────────
    private final AdminController adminController = new AdminController();
    private final KeretaDAO keretaDAO = new KeretaDAO();
    private final JadwalDAO jadwalDAO = new JadwalDAO();
    private final PromoDAO promoDAO = new PromoDAO();
    private final TiketDAO tiketDAO = new TiketDAO();
    private final PembayaranDAO pembayaranDAO = new PembayaranDAO();

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DF  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupAdminInfo();
        setupKeretaTable();
        setupJadwalTable();
        setupPromoTable();
        setupPembayaranTable();
        setupDashboardTable();
        loadDashboardStats();
        showPanel(panelDashboard, btnNavDashboard);
    }

    // ── Setup ─────────────────────────────────────────────────────

    private void setupAdminInfo() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblAdminName.setText(user.getNamaLengkap());
            lblAdminRole.setText("Administrator");
        }
    }

    private void setupKeretaTable() {
        colKeretaNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colKeretaNomor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomorKereta()));
        colKeretaJenis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJenis()));
        colKeretaKapasitas.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getKapasitasTotal()).asObject());
        loadKereta();
    }

    private void setupJadwalTable() {
        colJadwalKereta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKereta().getNama()));
        colJadwalRute.setCellValueFactory(c -> {
            Rute r = c.getValue().getRute();
            return new SimpleStringProperty(r.getStasiunAsal() + " → " + r.getStasiunTujuan());
        });
        colJadwalBerangkat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWaktuBerangkat().format(DTF)));
        colJadwalTiba.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWaktuTiba().format(DTF)));
        colJadwalStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        loadJadwal();
    }

    private void setupPromoTable() {
        colPromoKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodePromo()));
        colPromoDeskripsi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeskripsi()));
        colPromoDiskon.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getDiskonPersen()).asObject());
        colPromoMulai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggalMulai().format(DF)));
        colPromoAkhir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggalBerakhir().format(DF)));
        colPromoStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAktif() ? "Aktif" : "Nonaktif"));
        loadPromo();
    }

    private void setupPembayaranTable() {
        colPembayaranId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colPembayaranJumlah.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getJumlahBayar()).asObject());
        colPembayaranMetode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMetodePembayaran()));
        colPembayaranStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colPembayaranTanggal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggalBayar().format(DTF)));
        loadPembayaran();
    }

    // ── Load data ─────────────────────────────────────────────────

    private void setupDashboardTable() {
        colDashKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeTiket()));
        colDashPenumpang.setCellValueFactory(c -> {
            User u = c.getValue().getPenumpang();
            return new SimpleStringProperty(u != null ? u.getNamaLengkap() : "-");
        });
        colDashRute.setCellValueFactory(c -> {
            Jadwal j = c.getValue().getJadwal();
            if (j == null || j.getRute() == null) return new SimpleStringProperty("-");
            return new SimpleStringProperty(j.getRute().getStasiunAsal() + " → " + j.getRute().getStasiunTujuan());
        });
        colDashTotal.setCellValueFactory(c -> new SimpleStringProperty(
            String.format("Rp %,.0f", c.getValue().getHargaTotal())));
        colDashStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
    }

    private void loadDashboardStats() {
        lblStatKereta.setText(String.valueOf(keretaDAO.findAll().size()));
        lblStatJadwal.setText(String.valueOf(jadwalDAO.findAll().size()));
        List<Tiket> tikets = tiketDAO.findAll();
        lblStatTiket.setText(String.valueOf(tikets.size()));
        lblStatPromo.setText(String.valueOf(promoDAO.findAll().size()));

        // Load tabel pemesanan terbaru (max 10)
        List<Tiket> recent = tikets.size() > 10 ? tikets.subList(tikets.size() - 10, tikets.size()) : tikets;
        tblPemesananDash.setItems(FXCollections.observableArrayList(recent));
    }

    private void loadKereta() {
        tblKereta.setItems(FXCollections.observableArrayList(keretaDAO.findAll()));
    }

    private void loadJadwal() {
        tblJadwal.setItems(FXCollections.observableArrayList(jadwalDAO.findAll()));
    }

    private void loadPromo() {
        tblPromo.setItems(FXCollections.observableArrayList(promoDAO.findAll()));
    }

    private void loadPembayaran() {
        tblPembayaran.setItems(FXCollections.observableArrayList(pembayaranDAO.findAll()));
    }

    // ── Nav ───────────────────────────────────────────────────────

    private void showPanel(VBox panel, Button activeBtn) {
        panelDashboard.setVisible(false);   panelDashboard.setManaged(false);
        panelKereta.setVisible(false);      panelKereta.setManaged(false);
        panelJadwal.setVisible(false);      panelJadwal.setManaged(false);
        panelPromo.setVisible(false);       panelPromo.setManaged(false);
        panelPembayaran.setVisible(false);  panelPembayaran.setManaged(false);

        panel.setVisible(true);
        panel.setManaged(true);

        for (Button b : new Button[]{btnNavDashboard, btnNavKereta, btnNavJadwal, btnNavPromo, btnNavPembayaran}) {
            b.getStyleClass().remove("nav-active");
        }
        activeBtn.getStyleClass().add("nav-active");
    }

    @FXML private void handleNavDashboard() {
        loadDashboardStats();
        showPanel(panelDashboard, btnNavDashboard);
    }

    @FXML private void handleNavKereta() {
        loadKereta();
        showPanel(panelKereta, btnNavKereta);
    }

    @FXML private void handleNavJadwal() {
        loadJadwal();
        showPanel(panelJadwal, btnNavJadwal);
    }

    @FXML private void handleNavPromo() {
        loadPromo();
        showPanel(panelPromo, btnNavPromo);
    }

    @FXML private void handleNavPembayaran() {
        loadPembayaran();
        showPanel(panelPembayaran, btnNavPembayaran);
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.switchScene("login.fxml");
    }

    // ── KERETA CRUD ───────────────────────────────────────────────

    @FXML private void handleTambahKereta() {
        try {
            String nama     = tfKeretaNama.getText().trim();
            String nomor    = tfKeretaNomor.getText().trim();
            String jenis    = tfKeretaJenis.getText().trim();
            int kapasitas   = Integer.parseInt(tfKeretaKapasitas.getText().trim());
            if (nama.isEmpty() || nomor.isEmpty() || jenis.isEmpty()) {
                setStatus("Semua field wajib diisi.", true); return;
            }
            Kereta k = new Kereta(nama, nomor, jenis, kapasitas);
            if (adminController.tambahKereta(k)) {
                setStatus("Kereta berhasil ditambahkan.", false);
                clearKeretaForm(); loadKereta();
            } else {
                setStatus("Gagal menambahkan kereta.", true);
            }
        } catch (NumberFormatException e) {
            setStatus("Kapasitas harus berupa angka.", true);
        }
    }

    @FXML private void handleEditKereta() {
        Kereta selected = tblKereta.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Pilih kereta yang ingin diedit.", true); return; }
        try {
            selected.setNama(tfKeretaNama.getText().trim());
            selected.setJenis(tfKeretaJenis.getText().trim());
            if (adminController.editKereta(selected)) {
                setStatus("Kereta berhasil diperbarui.", false);
                clearKeretaForm(); loadKereta();
            } else {
                setStatus("Gagal memperbarui kereta.", true);
            }
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), true);
        }
    }

    @FXML private void handleHapusKereta() {
        Kereta selected = tblKereta.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Pilih kereta yang ingin dihapus.", true); return; }
        if (adminController.hapusKereta(selected.getId())) {
            setStatus("Kereta berhasil dihapus.", false);
            loadKereta();
        } else {
            setStatus("Gagal menghapus kereta.", true);
        }
    }

    @FXML private void handleSelectKereta() {
        Kereta k = tblKereta.getSelectionModel().getSelectedItem();
        if (k == null) return;
        tfKeretaNama.setText(k.getNama());
        tfKeretaNomor.setText(k.getNomorKereta());
        tfKeretaJenis.setText(k.getJenis());
        tfKeretaKapasitas.setText(String.valueOf(k.getKapasitasTotal()));
    }

    private void clearKeretaForm() {
        tfKeretaNama.clear(); tfKeretaNomor.clear();
        tfKeretaJenis.clear(); tfKeretaKapasitas.clear();
    }

    // ── JADWAL CRUD ───────────────────────────────────────────────

    @FXML private void handleTambahJadwal() {
        try {
            int keretaId = Integer.parseInt(tfJadwalKeretaId.getText().trim());
            int ruteId   = Integer.parseInt(tfJadwalRuteId.getText().trim());
            int kelasId  = Integer.parseInt(tfJadwalKelasId.getText().trim());
            LocalDateTime berangkat = LocalDateTime.parse(tfJadwalBerangkat.getText().trim(), DTF);
            LocalDateTime tiba      = LocalDateTime.parse(tfJadwalTiba.getText().trim(), DTF);

            Kereta kereta       = keretaDAO.findById(keretaId);
            if (kereta == null) { setStatus("ID Kereta tidak ditemukan.", true); return; }

            // Jadwal constructor requires Rute and JenisKelas — build minimal placeholders from IDs
            // Full impl needs RuteDAO and JenisKelasDAO; for now show intent
            setStatus("Fitur tambah jadwal membutuhkan RuteDAO & JenisKelasDAO (ID: rute=" + ruteId + ", kelas=" + kelasId + ").", true);
        } catch (Exception e) {
            setStatus("Format salah. Tanggal: dd/MM/yyyy HH:mm", true);
        }
    }

    @FXML private void handleHapusJadwal() {
        Jadwal selected = tblJadwal.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Pilih jadwal yang ingin dihapus.", true); return; }
        if (adminController.hapusJadwal(selected.getId())) {
            setStatus("Jadwal berhasil dihapus.", false);
            loadJadwal();
        } else {
            setStatus("Gagal menghapus jadwal.", true);
        }
    }

    // ── PROMO CRUD ────────────────────────────────────────────────

    @FXML private void handleTambahPromo() {
        try {
            String kode    = tfPromoKode.getText().trim();
            String desk    = tfPromoDeskripsi.getText().trim();
            double diskon  = Double.parseDouble(tfPromoDiskon.getText().trim());
            LocalDate mulai  = dpPromoMulai.getValue();
            LocalDate akhir  = dpPromoAkhir.getValue();
            if (kode.isEmpty() || mulai == null || akhir == null) {
                setStatus("Semua field promo wajib diisi.", true); return;
            }
            Promo p = new Promo(kode, desk, diskon, mulai, akhir);
            if (adminController.tambahPromo(p)) {
                setStatus("Promo berhasil ditambahkan.", false);
                clearPromoForm(); loadPromo();
            } else {
                setStatus("Gagal menambahkan promo.", true);
            }
        } catch (NumberFormatException e) {
            setStatus("Diskon harus berupa angka.", true);
        }
    }

    @FXML private void handleHapusPromo() {
        Promo selected = tblPromo.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Pilih promo yang ingin dihapus.", true); return; }
        if (adminController.hapusPromo(selected.getId())) {
            setStatus("Promo berhasil dihapus.", false);
            loadPromo();
        } else {
            setStatus("Gagal menghapus promo.", true);
        }
    }

    @FXML private void handleSelectPromo() {
        Promo p = tblPromo.getSelectionModel().getSelectedItem();
        if (p == null) return;
        tfPromoKode.setText(p.getKodePromo());
        tfPromoDeskripsi.setText(p.getDeskripsi());
        tfPromoDiskon.setText(String.valueOf(p.getDiskonPersen()));
        dpPromoMulai.setValue(p.getTanggalMulai());
        dpPromoAkhir.setValue(p.getTanggalBerakhir());
    }

    private void clearPromoForm() {
        tfPromoKode.clear(); tfPromoDeskripsi.clear();
        tfPromoDiskon.clear(); dpPromoMulai.setValue(null); dpPromoAkhir.setValue(null);
    }

    // ── PEMBAYARAN ────────────────────────────────────────────────

    @FXML private void handleValidasiPembayaran() {
        Pembayaran selected = tblPembayaran.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Pilih pembayaran yang ingin divalidasi.", true); return; }
        if ("LUNAS".equals(selected.getStatus())) { setStatus("Pembayaran ini sudah LUNAS.", true); return; }
        if (adminController.validasiPembayaran(selected)) {
            setStatus("Pembayaran #" + selected.getId() + " berhasil divalidasi.", false);
            loadPembayaran();
        } else {
            setStatus("Gagal memvalidasi pembayaran.", true);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.getStyleClass().removeAll("status-ok", "status-err");
        lblStatus.getStyleClass().add(isError ? "status-err" : "status-ok");
    }
}