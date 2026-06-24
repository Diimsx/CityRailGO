package controller;

import dao.JadwalDAO;
import dao.StasiunDAO;
import model.Jadwal;
import model.Stasiun;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CariJadwalController implements Initializable {

    // ── Search Form ──────────────────────────────────────────
    @FXML private ComboBox<Stasiun> cmbAsalStasiun;
    @FXML private ComboBox<Stasiun> cmbTujuanStasiun;
    @FXML private DatePicker        dpTanggalPergi;
    @FXML private DatePicker        dpTanggalKembali;
    @FXML private ComboBox<String>  cmbKelasKereta;
    @FXML private Spinner<Integer>  spnJumlahPenumpang;
    @FXML private ToggleGroup       tgTiketType;
    @FXML private RadioButton       rbSekaliJalan;
    @FXML private RadioButton       rbPulangPergi;

    // ── Filter & Sort ─────────────────────────────────────────
    @FXML private ComboBox<String>  cmbSortBy;
    @FXML private ToggleButton      tbFilterEkonomi;
    @FXML private ToggleButton      tbFilterBisnis;
    @FXML private ToggleButton      tbFilterEksekutif;
    @FXML private Slider            sliderMaxHarga;
    @FXML private Label             lblMaxHarga;

    // ── Tabel Hasil ───────────────────────────────────────────
    @FXML private TableView<Jadwal>             tblJadwal;
    @FXML private TableColumn<Jadwal, String>   colKereta;
    @FXML private TableColumn<Jadwal, String>   colKelas;
    @FXML private TableColumn<Jadwal, String>   colBerangkat;
    @FXML private TableColumn<Jadwal, String>   colTiba;
    @FXML private TableColumn<Jadwal, String>   colDurasi;
    @FXML private TableColumn<Jadwal, Integer>  colKursiTersedia;
    @FXML private TableColumn<Jadwal, Double>   colHarga;
    @FXML private TableColumn<Jadwal, Void>     colAksi;

    // ── State & Info ──────────────────────────────────────────
    @FXML private Label     lblHasilCount;
    @FXML private Label     lblRuteSummary;
    @FXML private VBox      vboxEmptyState;
    @FXML private VBox      vboxLoadingState;
    @FXML private StackPane stackPaneContent;
    @FXML private HBox      hboxPulangPergiDate;
    @FXML private Button    btnCari;
    @FXML private Button    btnReset;
    @FXML private Button    btnTukarStasiun;

    // ── DAO & Data ────────────────────────────────────────────
    private final JadwalDAO  jadwalDAO  = new JadwalDAO();
    private final StasiunDAO stasiunDAO = new StasiunDAO();

    private ObservableList<Jadwal> hasilJadwal = FXCollections.observableArrayList();
    private ObservableList<Jadwal> semuaJadwal = FXCollections.observableArrayList();

    private static final DateTimeFormatter FMT_DISPLAY =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_TANGGAL =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ─────────────────────────────────────────────────────────
    //  INITIALIZE
    // ─────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupKelasComboBox();
        setupStasiunComboBox();
        setupSortComboBox();
        setupDatePickers();
        setupSpinner();
        setupTiketTypeToggle();
        setupSliderHarga();
        setupTableColumns();
        tblJadwal.setItems(hasilJadwal);
        showEmptyState(true);
    }

    // ─────────────────────────────────────────────────────────
    //  SETUP METHODS
    // ─────────────────────────────────────────────────────────

    private void setupKelasComboBox() {
        cmbKelasKereta.setItems(FXCollections.observableArrayList(
                "Semua Kelas", "Ekonomi", "Bisnis", "Eksekutif"
        ));
        cmbKelasKereta.setValue("Semua Kelas");
    }

    private void setupStasiunComboBox() {
        List<Stasiun> daftarStasiun = stasiunDAO.findAll();
        ObservableList<Stasiun> stasiunList = FXCollections.observableArrayList(daftarStasiun);

        StringConverter<Stasiun> converter = new StringConverter<>() {
            @Override public String toString(Stasiun s) {
                return s == null ? "" : s.getNamaStasiun() + " (" + s.getKodeStasiun() + ")";
            }
            @Override public Stasiun fromString(String s) { return null; }
        };

        cmbAsalStasiun.setItems(stasiunList);
        cmbAsalStasiun.setConverter(converter);
        cmbAsalStasiun.setEditable(true);
        setupStasiunFilter(cmbAsalStasiun, stasiunList);

        cmbTujuanStasiun.setItems(FXCollections.observableArrayList(stasiunList));
        cmbTujuanStasiun.setConverter(converter);
        cmbTujuanStasiun.setEditable(true);
        setupStasiunFilter(cmbTujuanStasiun, stasiunList);
    }

    /** Filter autocomplete untuk ComboBox stasiun */
    private void setupStasiunFilter(ComboBox<Stasiun> cmb,
                                    ObservableList<Stasiun> allItems) {
        cmb.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                cmb.setItems(allItems);
                return;
            }
            String keyword = newVal.toLowerCase();
            ObservableList<Stasiun> filtered = allItems.filtered(s ->
                    s.getNamaStasiun().toLowerCase().contains(keyword) ||
                    s.getKodeStasiun().toLowerCase().contains(keyword) ||
                    s.getKota().toLowerCase().contains(keyword)
            );
            cmb.setItems(filtered);
            if (!filtered.isEmpty()) cmb.show();
        });
    }

    private void setupSortComboBox() {
        cmbSortBy.setItems(FXCollections.observableArrayList(
                "Berangkat Paling Awal",
                "Berangkat Paling Akhir",
                "Harga Terendah",
                "Harga Tertinggi",
                "Durasi Terpendek"
        ));
        cmbSortBy.setValue("Berangkat Paling Awal");
        cmbSortBy.setOnAction(e -> applyFilterAndSort());
    }

    private void setupDatePickers() {
        dpTanggalPergi.setValue(LocalDate.now());
        dpTanggalPergi.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        dpTanggalKembali.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate pergi = dpTanggalPergi.getValue();
                setDisable(empty || (pergi != null && date.isBefore(pergi.plusDays(1))));
            }
        });
        hboxPulangPergiDate.setVisible(false);
        hboxPulangPergiDate.setManaged(false);
    }

    private void setupSpinner() {
        SpinnerValueFactory<Integer> factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1);
        spnJumlahPenumpang.setValueFactory(factory);
        spnJumlahPenumpang.setEditable(true);
    }

    private void setupTiketTypeToggle() {
        rbSekaliJalan.setSelected(true);
        tgTiketType.selectedToggleProperty().addListener((obs, old, newToggle) -> {
            boolean isPulangPergi = newToggle == rbPulangPergi;
            hboxPulangPergiDate.setVisible(isPulangPergi);
            hboxPulangPergiDate.setManaged(isPulangPergi);
            if (isPulangPergi && dpTanggalKembali.getValue() == null) {
                dpTanggalKembali.setValue(dpTanggalPergi.getValue().plusDays(1));
            }
        });
    }

    private void setupSliderHarga() {
        sliderMaxHarga.setMin(0);
        sliderMaxHarga.setMax(2000000);
        sliderMaxHarga.setValue(2000000);
        lblMaxHarga.setText("Rp 2.000.000");
        sliderMaxHarga.valueProperty().addListener((obs, old, newVal) -> {
            long val = newVal.longValue();
            lblMaxHarga.setText(formatRupiah(val));
            applyFilterAndSort();
        });
    }

    private void setupTableColumns() {
        colKereta.setCellValueFactory(new PropertyValueFactory<>("namaKereta"));

        colKelas.setCellValueFactory(new PropertyValueFactory<>("jenisKelas"));
        colKelas.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String kelas, boolean empty) {
                super.updateItem(kelas, empty);
                if (empty || kelas == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(kelas);
                badge.getStyleClass().addAll("kelas-badge", "kelas-" + kelas.toLowerCase());
                setGraphic(badge);
                setText(null);
            }
        });

        colBerangkat.setCellValueFactory(new PropertyValueFactory<>("waktuBerangkat"));
        colBerangkat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                LocalDateTime ldt = (LocalDateTime) item;
                setText(ldt.format(FMT_DISPLAY));
                getStyleClass().add("time-cell");
            }
        });

        colTiba.setCellValueFactory(new PropertyValueFactory<>("waktuTiba"));
        colTiba.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                LocalDateTime ldt = (LocalDateTime) item;
                setText(ldt.format(FMT_DISPLAY));
                getStyleClass().add("time-cell");
            }
        });

        colDurasi.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                Jadwal j = getTableView().getItems().get(getIndex());
                if (j == null) return;
                long minutes = java.time.Duration.between(
                        j.getWaktuBerangkat(), j.getWaktuTiba()).toMinutes();
                setText(String.format("%dj %02dm", minutes / 60, minutes % 60));
            }
        });

        colKursiTersedia.setCellValueFactory(new PropertyValueFactory<>("kursiTersedia"));
        colKursiTersedia.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer sisa, boolean empty) {
                super.updateItem(sisa, empty);
                if (empty || sisa == null) { setText(null); return; }
                setText(sisa + " kursi");
                if (sisa == 0) getStyleClass().add("kursi-habis");
                else if (sisa <= 5) getStyleClass().add("kursi-sedikit");
                else getStyleClass().add("kursi-tersedia");
            }
        });

        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double harga, boolean empty) {
                super.updateItem(harga, empty);
                if (empty || harga == null) { setText(null); return; }
                setText(formatRupiah(harga.longValue()));
                getStyleClass().add("harga-cell");
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnPilih = new Button("Pilih");
            {
                btnPilih.getStyleClass().add("btn-pilih");
                btnPilih.setOnAction(e -> {
                    Jadwal jadwal = getTableView().getItems().get(getIndex());
                    handlePilihJadwal(jadwal);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnPilih);
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    //  ACTION HANDLERS
    // ─────────────────────────────────────────────────────────

    @FXML
    private void handleCari(ActionEvent event) {
        if (!validateForm()) return;

        Stasiun asal   = cmbAsalStasiun.getValue();
        Stasiun tujuan = cmbTujuanStasiun.getValue();
        LocalDate tanggal = dpTanggalPergi.getValue();

        showLoadingState(true);

        // Jalankan query ke DAO
        List<Jadwal> hasil = jadwalDAO.searchJadwal(
                asal.getId(),
                tujuan.getId(),
                tanggal,
                cmbKelasKereta.getValue(),
                spnJumlahPenumpang.getValue()
        );

        semuaJadwal.setAll(hasil);
        applyFilterAndSort();

        // Update info rute
        lblRuteSummary.setText(
                asal.getNamaStasiun() + " → " + tujuan.getNamaStasiun() +
                " · " + tanggal.format(FMT_TANGGAL) +
                " · " + spnJumlahPenumpang.getValue() + " penumpang"
        );

        showLoadingState(false);
        showEmptyState(hasil.isEmpty());
    }

    @FXML
    private void handleTukarStasiun(ActionEvent event) {
        Stasiun tempAsal   = cmbAsalStasiun.getValue();
        Stasiun tempTujuan = cmbTujuanStasiun.getValue();
        cmbAsalStasiun.setValue(tempTujuan);
        cmbTujuanStasiun.setValue(tempAsal);

        // Animasi visual swap
        btnTukarStasiun.setRotate(btnTukarStasiun.getRotate() + 180);
    }

    @FXML
    private void handleReset(ActionEvent event) {
        cmbAsalStasiun.setValue(null);
        cmbTujuanStasiun.setValue(null);
        dpTanggalPergi.setValue(LocalDate.now());
        dpTanggalKembali.setValue(null);
        cmbKelasKereta.setValue("Semua Kelas");
        spnJumlahPenumpang.getValueFactory().setValue(1);
        rbSekaliJalan.setSelected(true);
        sliderMaxHarga.setValue(2000000);
        cmbSortBy.setValue("Berangkat Paling Awal");
        tbFilterEkonomi.setSelected(false);
        tbFilterBisnis.setSelected(false);
        tbFilterEksekutif.setSelected(false);
        hasilJadwal.clear();
        semuaJadwal.clear();
        lblHasilCount.setText("");
        lblRuteSummary.setText("");
        showEmptyState(true);
    }

    @FXML
    private void handleFilterToggle(ActionEvent event) {
        applyFilterAndSort();
    }

    /** Dipanggil saat user klik tombol "Pilih" di baris tabel */
    private void handlePilihJadwal(Jadwal jadwal) {
        if (jadwal == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/PemesananView.fxml"));
            Parent root = loader.load();

            // Kirim data jadwal ke controller berikutnya
            PemesananController pemesananCtrl = loader.getController();
            pemesananCtrl.setJadwal(jadwal);
            pemesananCtrl.setJumlahPenumpang(spnJumlahPenumpang.getValue());

            Stage stage = (Stage) tblJadwal.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Error", "Gagal membuka halaman pemesanan: " + e.getMessage());
        }
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/view/HomePenumpangView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal kembali: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  FILTER & SORT
    // ─────────────────────────────────────────────────────────

    private void applyFilterAndSort() {
        if (semuaJadwal.isEmpty()) return;

        double maxHarga = sliderMaxHarga.getValue();

        // Kumpulkan filter kelas aktif
        boolean filterEkonomi   = tbFilterEkonomi.isSelected();
        boolean filterBisnis    = tbFilterBisnis.isSelected();
        boolean filterEksekutif = tbFilterEksekutif.isSelected();
        boolean noFilter        = !filterEkonomi && !filterBisnis && !filterEksekutif;

        ObservableList<Jadwal> filtered = semuaJadwal.filtered(j -> {
            // Filter harga
            if (j.getHarga() > maxHarga) return false;
            // Filter kelas
            if (!noFilter) {
                String kelas = j.getJenisKelas().toLowerCase();
                if (filterEkonomi   && kelas.equals("ekonomi"))   return true;
                if (filterBisnis    && kelas.equals("bisnis"))     return true;
                if (filterEksekutif && kelas.equals("eksekutif")) return true;
                return false;
            }
            return true;
        });

        // Sorting
        ObservableList<Jadwal> sorted = FXCollections.observableArrayList(filtered);
        String sortBy = cmbSortBy.getValue();
        if (sortBy != null) {
            switch (sortBy) {
                case "Berangkat Paling Awal"  -> sorted.sort((a, b) -> a.getWaktuBerangkat().compareTo(b.getWaktuBerangkat()));
                case "Berangkat Paling Akhir" -> sorted.sort((a, b) -> b.getWaktuBerangkat().compareTo(a.getWaktuBerangkat()));
                case "Harga Terendah"         -> sorted.sort((a, b) -> Double.compare(a.getHarga(), b.getHarga()));
                case "Harga Tertinggi"        -> sorted.sort((a, b) -> Double.compare(b.getHarga(), a.getHarga()));
                case "Durasi Terpendek"       -> sorted.sort((a, b) -> {
                    long durA = java.time.Duration.between(a.getWaktuBerangkat(), a.getWaktuTiba()).toMinutes();
                    long durB = java.time.Duration.between(b.getWaktuBerangkat(), b.getWaktuTiba()).toMinutes();
                    return Long.compare(durA, durB);
                });
            }
        }

        hasilJadwal.setAll(sorted);
        lblHasilCount.setText(sorted.size() + " jadwal ditemukan");
        showEmptyState(sorted.isEmpty());
    }

    // ─────────────────────────────────────────────────────────
    //  VALIDASI
    // ─────────────────────────────────────────────────────────

    private boolean validateForm() {
        if (cmbAsalStasiun.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih stasiun asal terlebih dahulu.");
            cmbAsalStasiun.requestFocus();
            return false;
        }
        if (cmbTujuanStasiun.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih stasiun tujuan terlebih dahulu.");
            cmbTujuanStasiun.requestFocus();
            return false;
        }
        if (cmbAsalStasiun.getValue().getId() == cmbTujuanStasiun.getValue().getId()) {
            showAlert(Alert.AlertType.WARNING, "Perhatian", "Stasiun asal dan tujuan tidak boleh sama.");
            return false;
        }
        if (dpTanggalPergi.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih tanggal keberangkatan.");
            return false;
        }
        if (rbPulangPergi.isSelected() && dpTanggalKembali.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih tanggal kembali untuk tiket pulang-pergi.");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────

    private void showEmptyState(boolean show) {
        vboxEmptyState.setVisible(show);
        vboxEmptyState.setManaged(show);
        tblJadwal.setVisible(!show);
        tblJadwal.setManaged(!show);
    }

    private void showLoadingState(boolean show) {
        vboxLoadingState.setVisible(show);
        vboxLoadingState.setManaged(show);
        stackPaneContent.setDisable(show);
        btnCari.setDisable(show);
        btnCari.setText(show ? "Mencari..." : "Cari Jadwal");
    }

    private String formatRupiah(long amount) {
        return "Rp " + String.format("%,d", amount).replace(',', '.');
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}