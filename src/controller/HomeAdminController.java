package controller;

import dao.JadwalDAO;
import dao.PemesananDAO;
import dao.PenumpangDAO;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Admin;
import model.Pemesanan;

/**
 * Controller untuk halaman utama Admin (Dashboard).
 *
 * Cara pakai dari halaman sebelumnya (contoh Login):
 * <pre>
 *   FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeAdmin.fxml"));
 *   Parent root = loader.load();
 *   HomeAdminController ctrl = loader.getController();
 *   ctrl.setAdminSession(adminYangLogin);
 *   stage.setScene(new Scene(root));
 * </pre>
 */
public class HomeAdminController implements Initializable {

    // ══════════════════════════════════════════════
    //  FXML BINDINGS — SIDEBAR
    // ══════════════════════════════════════════════
    @FXML private Label   labelAvatarInitial;
    @FXML private Label   labelAdminName;
    @FXML private Button  btnDashboard;
    @FXML private Button  btnKereta;
    @FXML private Button  btnRute;
    @FXML private Button  btnJadwal;
    @FXML private Button  btnPenumpang;
    @FXML private Button  btnPemesanan;
    @FXML private Button  btnLogout;

    // ══════════════════════════════════════════════
    //  FXML BINDINGS — HEADER
    // ══════════════════════════════════════════════
    @FXML private Label labelDateTime;

    // ══════════════════════════════════════════════
    //  FXML BINDINGS — STAT LABELS
    // ══════════════════════════════════════════════
    @FXML private Label labelTotalPemesanan;
    @FXML private Label labelTotalPendapatan;
    @FXML private Label labelTotalPenumpang;
    @FXML private Label labelJadwalAktif;
    @FXML private Label labelTrendPemesanan;
    @FXML private Label labelTrendPendapatan;
    @FXML private Label labelTrendPenumpang;

    // ══════════════════════════════════════════════
    //  FXML BINDINGS — TABLE
    // ══════════════════════════════════════════════
    @FXML private TableView<Pemesanan>         tablePemesanan;
    @FXML private TableColumn<Pemesanan, String> colKode;
    @FXML private TableColumn<Pemesanan, String> colNama;
    @FXML private TableColumn<Pemesanan, String> colRute;
    @FXML private TableColumn<Pemesanan, String> colTanggal;
    @FXML private TableColumn<Pemesanan, String> colStatus;

    // ══════════════════════════════════════════════
    //  STATE
    // ══════════════════════════════════════════════
    private Admin adminSession;

    private static final String[] NAV_FXML_PATHS = {
        "/view/KeretaAdmin.fxml",
        "/view/RuteAdmin.fxml",
        "/view/JadwalAdmin.fxml",
        "/view/PenumpangAdmin.fxml",
        "/view/PemesananAdmin.fxml",
        "/view/Login.fxml"
    };

    // ══════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadInterFont();
        setDateTimeLabel();
        setupTable();
        setActiveNav(btnDashboard);
        loadDashboardDataAsync();
    }

    // ══════════════════════════════════════════════
    //  SESSION — dipanggil dari controller sebelumnya
    // ══════════════════════════════════════════════

    /**
     * Inject sesi Admin setelah FXML di-load.
     * Panggil sebelum menampilkan Scene.
     */
    public void setAdminSession(Admin admin) {
        this.adminSession = admin;
        if (admin == null) return;

        String nama = (admin.getNamaLengkap() != null && !admin.getNamaLengkap().isBlank())
                ? admin.getNamaLengkap()
                : admin.getUsername();

        labelAdminName.setText(nama);
        labelAvatarInitial.setText(
            nama.isEmpty() ? "A" : String.valueOf(nama.charAt(0)).toUpperCase()
        );
    }

    // ══════════════════════════════════════════════
    //  SETUP HELPERS
    // ══════════════════════════════════════════════

    /** Muat font Inter dari resources/fonts agar CSS dapat mengenalinya. */
    private void loadInterFont() {
        String[] variants = {
            "Inter-Regular.ttf", "Inter-Medium.ttf",
            "Inter-SemiBold.ttf", "Inter-Bold.ttf"
        };
        for (String variant : variants) {
            try (var stream = getClass().getResourceAsStream("/fonts/" + variant)) {
                if (stream != null) Font.loadFont(stream, 14);
            } catch (Exception ignored) {
                // Font tidak tersedia — JavaFX akan fallback ke Segoe UI / system font
            }
        }
    }

    private void setDateTimeLabel() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                "EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        labelDateTime.setText(LocalDate.now().format(fmt));
    }

    private void setupTable() {
        // Mapping kolom → property getter di model Pemesanan
        // Sesuaikan nama properti dengan getter yang ada di class Pemesanan kamu
        colKode.setCellValueFactory(new PropertyValueFactory<>("kode"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaPenumpang"));
        colRute.setCellValueFactory(new PropertyValueFactory<>("ruteDisplay"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalFormatted"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status column dengan warna badge
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                setStyle(resolveStatusStyle(status));
            }
        });

        // Nonaktifkan sort bawaan JavaFX di kolom status (opsional)
        colStatus.setSortable(false);
    }

    private String resolveStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "dikonfirmasi", "confirmed", "aktif" ->
                "-fx-text-fill: #059669; -fx-font-weight: 600;";
            case "menunggu", "pending" ->
                "-fx-text-fill: #D97706; -fx-font-weight: 600;";
            case "dibatalkan", "cancelled", "batal" ->
                "-fx-text-fill: #DC2626; -fx-font-weight: 600;";
            default ->
                "-fx-text-fill: #64748B;";
        };
    }

    // ══════════════════════════════════════════════
    //  DATA LOADING (async agar UI tidak freeze)
    // ══════════════════════════════════════════════

    private void loadDashboardDataAsync() {
        Task<DashboardStats> task = new Task<>() {
            @Override
            protected DashboardStats call() {
                DashboardStats stats = new DashboardStats();
                try {
                    PemesananDAO pDao = new PemesananDAO();
                    PenumpangDAO uDao = new PenumpangDAO();
                    JadwalDAO    jDao = new JadwalDAO();

                    stats.totalPemesanan  = pDao.countAll();
                    stats.totalPendapatan = pDao.sumPendapatan();
                    stats.totalPenumpang  = uDao.countAll();
                    stats.jadwalAktif     = jDao.countAktif();
                    stats.recentPemesanan = pDao.findRecent(10);
                } catch (Exception e) {
                    System.err.println("[HomeAdmin] Gagal memuat data: " + e.getMessage());
                }
                return stats;
            }
        };

        task.setOnSucceeded(e -> {
            DashboardStats stats = task.getValue();
            Platform.runLater(() -> applyStats(stats));
        });

        task.setOnFailed(e ->
            System.err.println("[HomeAdmin] Task gagal: " + task.getException())
        );

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void applyStats(DashboardStats stats) {
        labelTotalPemesanan.setText(String.valueOf(stats.totalPemesanan));
        labelTotalPendapatan.setText(formatRupiah(stats.totalPendapatan));
        labelTotalPenumpang.setText(String.valueOf(stats.totalPenumpang));
        labelJadwalAktif.setText(String.valueOf(stats.jadwalAktif));

        if (stats.recentPemesanan != null) {
            ObservableList<Pemesanan> items =
                    FXCollections.observableArrayList(stats.recentPemesanan);
            tablePemesanan.setItems(items);
        }
    }

    // ══════════════════════════════════════════════
    //  NAVIGATION HANDLERS
    // ══════════════════════════════════════════════

    @FXML private void handleDashboard() {
        setActiveNav(btnDashboard);
        loadDashboardDataAsync(); // refresh saat kembali ke dashboard
    }

    @FXML private void handleKereta() {
        setActiveNav(btnKereta);
        navigateTo("/view/KeretaAdmin.fxml", "CityRailGO — Manajemen Kereta");
    }

    @FXML private void handleRute() {
        setActiveNav(btnRute);
        navigateTo("/view/RuteAdmin.fxml", "CityRailGO — Manajemen Rute");
    }

    @FXML private void handleJadwal() {
        setActiveNav(btnJadwal);
        navigateTo("/view/JadwalAdmin.fxml", "CityRailGO — Manajemen Jadwal");
    }

    @FXML private void handlePenumpang() {
        setActiveNav(btnPenumpang);
        navigateTo("/view/PenumpangAdmin.fxml", "CityRailGO — Data Penumpang");
    }

    @FXML private void handlePemesanan() {
        setActiveNav(btnPemesanan);
        navigateTo("/view/PemesananAdmin.fxml", "CityRailGO — Data Pemesanan");
    }

    @FXML private void handleRefresh() {
        // Animasi sederhana: reset teks → async reload
        labelTotalPemesanan.setText("...");
        labelTotalPendapatan.setText("...");
        labelTotalPenumpang.setText("...");
        labelJadwalAktif.setText("...");
        tablePemesanan.setItems(FXCollections.observableArrayList());
        loadDashboardDataAsync();
    }

    @FXML private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Keluar");
        confirm.setHeaderText(null);
        confirm.setContentText("Apakah Anda yakin ingin keluar dari sesi ini?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                navigateTo("/view/Login.fxml", "CityRailGO — Login");
            }
        });
    }

    // ══════════════════════════════════════════════
    //  NAVIGATION HELPER
    // ══════════════════════════════════════════════

    private void setActiveNav(Button active) {
        Button[] all = {btnDashboard, btnKereta, btnRute, btnJadwal, btnPenumpang, btnPemesanan};
        for (Button b : all) {
            b.getStyleClass().remove("nav-btn-active");
        }
        if (active != null && !active.getStyleClass().contains("nav-btn-active")) {
            active.getStyleClass().add("nav-btn-active");
        }
    }

    /**
     * Ganti Scene ke halaman lain.
     * Jika target tidak ditemukan, tampilkan error alert.
     */
    private void navigateTo(String fxmlPath, String windowTitle) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showError("Halaman tidak ditemukan", "File " + fxmlPath + " belum tersedia.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigasi Gagal",
                      "Tidak dapat membuka halaman.\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════

    private String formatRupiah(double amount) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String result = fmt.format(amount);
        // Hapus ",00" di belakang agar lebih bersih
        return result.endsWith(",00") ? result.substring(0, result.length() - 3) : result;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ══════════════════════════════════════════════
    //  INNER CLASS — Data Transfer Object
    // ══════════════════════════════════════════════

    /** Wadah data yang diambil di background thread. */
    private static class DashboardStats {
        int              totalPemesanan  = 0;
        double           totalPendapatan = 0.0;
        int              totalPenumpang  = 0;
        int              jadwalAktif     = 0;
        List<Pemesanan>  recentPemesanan = null;
    }
}