package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.Rute;
import model.Tiket;
import util.DateTimeUtil;
import util.PricingEngine;
import util.SceneManager;
import util.SessionManager;
import util.TiketHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LaporanPenjualanController implements Initializable {

    private static final DateTimeFormatter FMT_HARI   = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter FMT_BULAN  = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter FMT_EXPORT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int MAX_BREAKDOWN_ROWS = 30;

    // ===== Header =====
    @FXML private Label lblAdminName;

    // ===== KPI Cards =====
    @FXML private Label lblTotalTransaksi;
    @FXML private Label lblTotalPendapatan;
    @FXML private Label lblTiketAktif;
    @FXML private Label lblTiketDibatalkan;

    // ===== Filter toolbar =====
    @FXML private TextField        tfSearch;
    @FXML private DatePicker       dpDari;
    @FXML private DatePicker       dpSampai;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private Button           btnExport;
    @FXML private Button           btnToggleHarian;
    @FXML private Button           btnToggleBulanan;

    // ===== Tabel =====
    @FXML private TableView<Tiket>            tblLaporan;
    @FXML private TableColumn<Tiket, Integer> colNo;
    @FXML private TableColumn<Tiket, String>  colKodeTiket;
    @FXML private TableColumn<Tiket, String>  colPenumpang;
    @FXML private TableColumn<Tiket, String>  colKereta;
    @FXML private TableColumn<Tiket, String>  colRute;
    @FXML private TableColumn<Tiket, String>  colBerangkat;
    @FXML private TableColumn<Tiket, String>  colHarga;
    @FXML private TableColumn<Tiket, String>  colStatus;
    @FXML private Label                       lblJumlahTampil;

    // ===== Breakdown Panel =====
    @FXML private Label lblBreakdownTitle;
    @FXML private Label lblBreakdownSubtitle;
    @FXML private VBox  vboxBreakdown;
    @FXML private Label lblBreakdownTotal;

    // ===== State =====
    private final AdminController adminController = new AdminController();

    private ObservableList<Tiket> daftarTiket;
    private FilteredList<Tiket>   daftarTiketTersaring;

    /** true = grouping per hari, false = per bulan */
    private boolean modeHarian = true;

    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getUsername());

        cbStatusFilter.setItems(FXCollections.observableArrayList("Semua Status", "AKTIF", "DIBATALKAN"));
        cbStatusFilter.setValue("Semua Status");

        setupTabel();
        muatDataLaporan();
        setupFilter();
    }

    // =========================================================
    // TABEL SETUP
    // =========================================================

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
            new SimpleObjectProperty<>(tblLaporan.getItems().indexOf(data.getValue()) + 1));

        colKodeTiket.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getKodeTiket()));

        colPenumpang.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPenumpang().getNamaLengkap()));

        colKereta.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getJadwal().getKereta().getNama()
                + "\n" + data.getValue().getJadwal().getKereta().getNomorKereta()));

        colRute.setCellValueFactory(data -> {
            Rute r = data.getValue().getJadwal().getRute();
            return new SimpleStringProperty(r.getStasiunAsal() + " \u2192 " + r.getStasiunTujuan());
        });

        colBerangkat.setCellValueFactory(data ->
            new SimpleStringProperty(DateTimeUtil.formatWaktu(data.getValue().getJadwal().getWaktuBerangkat())));

        colHarga.setCellValueFactory(data ->
            new SimpleStringProperty(PricingEngine.formatRupiah(data.getValue().getHargaTotal())));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String harga, boolean kosong) {
                super.updateItem(harga, kosong);
                if (kosong || harga == null) { setGraphic(null); setText(null); return; }
                Label lbl = new Label(harga);
                lbl.getStyleClass().add("harga-value");
                setGraphic(lbl);
                setText(null);
            }
        });

        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean kosong) {
                super.updateItem(status, kosong);
                if (kosong || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().addAll("badge-status",
                    status.equalsIgnoreCase("AKTIF") ? "badge-aktif" : "badge-dibatalkan");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void muatDataLaporan() {
        daftarTiket = FXCollections.observableArrayList(adminController.getLaporanPenjualan());
        daftarTiketTersaring = new FilteredList<>(daftarTiket, t -> true);
        tblLaporan.setItems(daftarTiketTersaring);

        // Setiap perubahan filtered list → update KPI + Breakdown + label jumlah
        daftarTiketTersaring.addListener((ListChangeListener<Tiket>) change -> {
            perbaruiStatistik();
            perbaruiBreakdown();
            perbaruiLabelJumlah();
        });
        perbaruiStatistik();
        perbaruiBreakdown();
        perbaruiLabelJumlah();
    }

    // =========================================================
    // FILTER
    // =========================================================

    private void setupFilter() {
        tfSearch.textProperty().addListener((obs, o, n)        -> terapkanFilter());
        dpDari.valueProperty().addListener((obs, o, n)         -> terapkanFilter());
        dpSampai.valueProperty().addListener((obs, o, n)       -> terapkanFilter());
        cbStatusFilter.valueProperty().addListener((obs, o, n) -> terapkanFilter());
    }

    private void terapkanFilter() {
        String kueri   = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        LocalDate dari  = dpDari.getValue();
        LocalDate sampai = dpSampai.getValue();
        String status   = cbStatusFilter.getValue();

        daftarTiketTersaring.setPredicate(t -> {
            // --- Teks: kode tiket, nama penumpang, kereta, nomor kereta, rute ---
            boolean cocokTeks = kueri.isEmpty()
                || t.getKodeTiket().toLowerCase().contains(kueri)
                || t.getPenumpang().getNamaLengkap().toLowerCase().contains(kueri)
                || t.getJadwal().getKereta().getNama().toLowerCase().contains(kueri)
                || t.getJadwal().getKereta().getNomorKereta().toLowerCase().contains(kueri)
                || t.getJadwal().getRute().getStasiunAsal().toLowerCase().contains(kueri)
                || t.getJadwal().getRute().getStasiunTujuan().toLowerCase().contains(kueri);

            // --- Rentang tanggal keberangkatan ---
            LocalDate tglBerangkat = t.getJadwal().getWaktuBerangkat().toLocalDate();
            boolean cocokDari   = dari   == null || !tglBerangkat.isBefore(dari);
            boolean cocokSampai = sampai == null || !tglBerangkat.isAfter(sampai);

            // --- Status ---
            boolean cocokStatus = status == null || status.equals("Semua Status")
                || t.getStatus().equalsIgnoreCase(status);

            return cocokTeks && cocokDari && cocokSampai && cocokStatus;
        });
    }

    @FXML
    private void handleResetFilter() {
        tfSearch.clear();
        dpDari.setValue(null);
        dpSampai.setValue(null);
        cbStatusFilter.setValue("Semua Status");
    }

    // =========================================================
    // KPI STATISTIK
    // =========================================================

    private void perbaruiStatistik() {
        List<Tiket> visible = daftarTiketTersaring;

        int total = visible.size();
        double pendapatan = visible.stream()
            .filter(t -> t.getStatus().equalsIgnoreCase("AKTIF"))
            .mapToDouble(Tiket::getHargaTotal).sum();
        long aktif = visible.stream()
            .filter(t -> t.getStatus().equalsIgnoreCase("AKTIF")).count();
        long dibatalkan = visible.stream()
            .filter(t -> t.getStatus().equalsIgnoreCase("DIBATALKAN")).count();

        lblTotalTransaksi.setText(String.valueOf(total));
        lblTotalPendapatan.setText(PricingEngine.formatRupiah(pendapatan));
        lblTiketAktif.setText(String.valueOf(aktif));
        lblTiketDibatalkan.setText(String.valueOf(dibatalkan));
    }

    private void perbaruiLabelJumlah() {
        int n = daftarTiketTersaring.size();
        lblJumlahTampil.setText(n + " tiket ditampilkan");
    }

    // =========================================================
    // BREAKDOWN HARIAN / BULANAN
    // =========================================================

    @FXML
    private void handleToggleHarian() {
        if (modeHarian) return;
        modeHarian = true;
        btnToggleHarian.getStyleClass().setAll("toggle-btn-active");
        btnToggleBulanan.getStyleClass().setAll("toggle-btn");
        perbaruiBreakdown();
    }

    @FXML
    private void handleToggleBulanan() {
        if (!modeHarian) return;
        modeHarian = false;
        btnToggleBulanan.getStyleClass().setAll("toggle-btn-active");
        btnToggleHarian.getStyleClass().setAll("toggle-btn");
        perbaruiBreakdown();
    }

    /**
     * Membangun ulang panel breakdown sesuai mode (harian/bulanan).
     * Hanya tiket AKTIF yang dihitung sebagai pendapatan.
     */
    private void perbaruiBreakdown() {
        vboxBreakdown.getChildren().clear();

        // Group tiket AKTIF berdasarkan periode
        Map<String, Double> grouped = grupTiketByPeriode();

        if (grouped.isEmpty()) {
            Label empty = new Label("Belum ada data transaksi\nuntuk periode ini");
            empty.getStyleClass().add("breakdown-empty");
            empty.setWrapText(true);
            vboxBreakdown.getChildren().add(empty);
            lblBreakdownTotal.setText("Rp 0");
            return;
        }

        // Hitung total & max untuk proporsi bar
        double total = grouped.values().stream().mapToDouble(Double::doubleValue).sum();
        double max   = grouped.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);

        // Hitung jumlah tiket per periode (semua status)
        Map<String, Long> countMap = grupCountByPeriode();

        // Render baris — max MAX_BREAKDOWN_ROWS terakhir
        int skip = Math.max(0, grouped.size() - MAX_BREAKDOWN_ROWS);
        int[] idx = {0};
        grouped.entrySet().stream().skip(skip).forEach(entry -> {
            String periode  = entry.getKey();
            double pendapatan = entry.getValue();
            long   count    = countMap.getOrDefault(periode, 0L);
            boolean isTop   = (pendapatan == max);

            VBox rowWrap = buildBreakdownRow(periode, pendapatan, count, max, isTop);
            vboxBreakdown.getChildren().add(rowWrap);
            idx[0]++;
        });

        lblBreakdownTitle.setText(modeHarian ? "Breakdown Harian" : "Breakdown Bulanan");
        lblBreakdownSubtitle.setText(modeHarian ? "Pendapatan per hari" : "Pendapatan per bulan");
        lblBreakdownTotal.setText(PricingEngine.formatRupiah(total));
    }

    private VBox buildBreakdownRow(String periode, double pendapatan, long count,
                                   double maxPendapatan, boolean isTop) {
        // Label periode (tanggal/bulan)
        Label lblPeriode = new Label(periode);
        lblPeriode.getStyleClass().add("breakdown-period");

        // Label jumlah tiket
        Label lblCount = new Label(count + " tiket");
        lblCount.getStyleClass().add("breakdown-count");

        // Label nilai pendapatan
        Label lblAmt = new Label(PricingEngine.formatRupiah(pendapatan));
        lblAmt.getStyleClass().add("breakdown-amount");

        // Mini bar proportional
        double barRatio = maxPendapatan > 0 ? pendapatan / maxPendapatan : 0;
        double barMaxWidth = 140.0;

        // Track (background)
        Region track = new Region();
        track.getStyleClass().add("breakdown-bar-track");
        track.setMinHeight(8); track.setMaxHeight(8);
        track.setPrefWidth(barMaxWidth);

        // Fill (foreground) — di-stack di dalam StackPane sederhana
        Region fill = new Region();
        fill.getStyleClass().add(isTop ? "breakdown-bar-fill-top" : "breakdown-bar-fill");
        fill.setMinHeight(8); fill.setMaxHeight(8);
        fill.setPrefWidth(barMaxWidth * barRatio);

        StackPane barPane = new StackPane(track, fill);
        StackPane.setAlignment(fill, javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(barPane, Priority.ALWAYS);

        // Row atas: periode + jumlah tiket
        HBox rowTop = new HBox(6, lblPeriode, lblCount);
        rowTop.setAlignment(Pos.CENTER_LEFT);

        // Row bawah: bar + nilai
        HBox rowBar = new HBox(8, barPane, lblAmt);
        rowBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(barPane, Priority.ALWAYS);

        VBox container = new VBox(3, rowTop, rowBar);
        container.getStyleClass().add("breakdown-row");
        if (isTop) {
            container.setStyle("-fx-background-color: #F0FBF5;");
        }
        return container;
    }

    /** Group pendapatan tiket AKTIF berdasarkan periode (harian/bulanan), sorted by date. */
    private Map<String, Double> grupTiketByPeriode() {
        DateTimeFormatter fmt = modeHarian ? FMT_HARI : FMT_BULAN;
        return daftarTiketTersaring.stream()
            .filter(t -> t.getStatus().equalsIgnoreCase("AKTIF"))
            .collect(Collectors.groupingBy(
                t -> t.getJadwal().getWaktuBerangkat().toLocalDate().format(fmt),
                TreeMap::new,
                Collectors.summingDouble(Tiket::getHargaTotal)
            ));
    }

    /** Group count semua tiket per periode untuk label "N tiket". */
    private Map<String, Long> grupCountByPeriode() {
        DateTimeFormatter fmt = modeHarian ? FMT_HARI : FMT_BULAN;
        return daftarTiketTersaring.stream()
            .collect(Collectors.groupingBy(
                t -> t.getJadwal().getWaktuBerangkat().toLocalDate().format(fmt),
                TreeMap::new,
                Collectors.counting()
            ));
    }

    // =========================================================
    // EXPORT CSV
    // =========================================================

    @FXML
    private void handleExportCsv() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Simpan Laporan Penjualan");

        // Nama file default mengandung timestamp
        String namaFile = "laporan_penjualan_"
            + java.time.LocalDateTime.now().format(FMT_EXPORT) + ".csv";
        fc.setInitialFileName(namaFile);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File (*.csv)", "*.csv"));

        File file = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
            // BOM untuk Excel agar karakter Indonesia terbaca
            writer.print('\uFEFF');

            // Header
            writer.println("No,Kode Tiket,Penumpang,Kereta,Nomor Kereta,Rute,Berangkat,Harga,Status");

            int no = 1;
            for (Tiket t : daftarTiketTersaring) {
                writer.println(barisCsv(no++, t));
            }

            // Summary footer
            writer.println();
            writer.println("RINGKASAN");
            writer.println("Total Transaksi," + daftarTiketTersaring.size());
            double pendapatan = daftarTiketTersaring.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("AKTIF"))
                .mapToDouble(Tiket::getHargaTotal).sum();
            writer.println("Total Pendapatan," + (long) pendapatan);
            writer.println("Tiket Aktif," + daftarTiketTersaring.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("AKTIF")).count());
            writer.println("Tiket Dibatalkan," + daftarTiketTersaring.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("DIBATALKAN")).count());

            tampilkanAlertInfo("Laporan berhasil diekspor!\n" + file.getAbsolutePath());
        } catch (IOException e) {
            tampilkanAlertError("Gagal mengekspor laporan: " + e.getMessage());
        }
    }

    private String barisCsv(int no, Tiket t) {
        Rute rute = t.getJadwal().getRute();
        return String.join(",",
            String.valueOf(no),
            kutip(t.getKodeTiket()),
            kutip(t.getPenumpang().getNamaLengkap()),
            kutip(t.getJadwal().getKereta().getNama()),
            kutip(t.getJadwal().getKereta().getNomorKereta()),
            kutip(rute.getStasiunAsal() + " - " + rute.getStasiunTujuan()),
            kutip(DateTimeUtil.formatWaktu(t.getJadwal().getWaktuBerangkat())),
            String.valueOf((long) t.getHargaTotal()),
            kutip(t.getStatus())
        );
    }

    private String kutip(String s) {
        return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
    }

    // =========================================================
    // ALERT HELPERS
    // =========================================================

    private void tampilkanAlertInfo(String pesan) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Berhasil");
        a.setHeaderText(null);
        a.setContentText(pesan);
        a.showAndWait();
    }

    private void tampilkanAlertError(String pesan) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Terjadi Kesalahan");
        a.setHeaderText(null);
        a.setContentText(pesan);
        a.showAndWait();
    }

    // =========================================================
    // NAVIGASI
    // =========================================================

    @FXML private void handleNavDashboard() { SceneManager.switchScene("HomeAdmin.fxml"); }
    @FXML private void handleNavKereta()    { SceneManager.switchScene("KelolaKereta.fxml"); }
    @FXML private void handleNavRute()      { SceneManager.switchScene("KelolaRute.fxml"); }
    @FXML private void handleNavStasiun()   { SceneManager.switchScene("KelolaStasiun.fxml"); }
    @FXML private void handleNavJadwal()    { SceneManager.switchScene("KelolaJadwal.fxml"); }
    @FXML private void handleNavPromo()     { SceneManager.switchScene("KelolaPromo.fxml"); }
    @FXML private void handleNavLaporan()   { /* sudah di halaman ini */ }

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