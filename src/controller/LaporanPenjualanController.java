package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.Rute;
import model.Tiket;
import util.DateTimeUtil;
import util.SceneManager;
import util.SessionManager;
import util.TiketHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.ButtonType;

public class LaporanPenjualanController implements Initializable {

    @FXML private Label lblAdminName;

    @FXML private Label lblTotalTransaksi;
    @FXML private Label lblTotalPendapatan;
    @FXML private Label lblTiketAktif;
    @FXML private Label lblTiketDibatalkan;

    @FXML private TextField tfSearch;
    @FXML private DatePicker dpDari;
    @FXML private DatePicker dpSampai;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private Button btnExport;

    @FXML private TableView<Tiket> tblLaporan;
    @FXML private TableColumn<Tiket, Integer> colNo;
    @FXML private TableColumn<Tiket, String> colKodeTiket;
    @FXML private TableColumn<Tiket, String> colPenumpang;
    @FXML private TableColumn<Tiket, String> colKereta;
    @FXML private TableColumn<Tiket, String> colRute;
    @FXML private TableColumn<Tiket, String> colBerangkat;
    @FXML private TableColumn<Tiket, String> colHarga;
    @FXML private TableColumn<Tiket, String> colStatus;

    private final AdminController adminController = new AdminController();

    private ObservableList<Tiket> daftarTiket;
    private FilteredList<Tiket> daftarTiketTersaring;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());

        cbStatusFilter.setItems(FXCollections.observableArrayList("Semua Status", "AKTIF", "DIBATALKAN"));
        cbStatusFilter.setValue("Semua Status");

        setupTabel();
        muatDataLaporan();
        setupFilter();
    }

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
                new SimpleObjectProperty<>(tblLaporan.getItems().indexOf(data.getValue()) + 1));

        colKodeTiket.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getKodeTiket()));

        colPenumpang.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPenumpang().getNamaLengkap()));

        colKereta.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getJadwal().getKereta().getNama()));

        colRute.setCellValueFactory(data -> {
            Rute rute = data.getValue().getJadwal().getRute();
            return new SimpleStringProperty(rute.getStasiunAsal() + " \u2192 " + rute.getStasiunTujuan());
        });

        colBerangkat.setCellValueFactory(data ->
                new SimpleStringProperty(DateTimeUtil.formatWaktu(data.getValue().getJadwal().getWaktuBerangkat())));

        colHarga.setCellValueFactory(data ->
                new SimpleStringProperty(TiketHelper.formatHarga(data.getValue().getHargaTotal())));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String harga, boolean kosong) {
                super.updateItem(harga, kosong);
                if (kosong || harga == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(harga);
                    label.getStyleClass().add("harga-value");
                    setGraphic(label);
                }
            }
        });

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
                badge.getStyleClass().add(status.equalsIgnoreCase("AKTIF") ? "badge-aktif" : "badge-dibatalkan");
                setGraphic(badge);
            }
        });
    }

    private void muatDataLaporan() {
        daftarTiket = FXCollections.observableArrayList(adminController.getLaporanPenjualan());
        daftarTiketTersaring = new FilteredList<>(daftarTiket, t -> true);
        tblLaporan.setItems(daftarTiketTersaring);

        daftarTiketTersaring.addListener((ListChangeListener<Tiket>) change -> perbaruiStatistik());
        perbaruiStatistik();
    }

    private void setupFilter() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> terapkanFilter());
        dpDari.valueProperty().addListener((obs, lama, baru) -> terapkanFilter());
        dpSampai.valueProperty().addListener((obs, lama, baru) -> terapkanFilter());
        cbStatusFilter.valueProperty().addListener((obs, lama, baru) -> terapkanFilter());
    }

    private void terapkanFilter() {
        String kueri = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        LocalDate dari = dpDari.getValue();
        LocalDate sampai = dpSampai.getValue();
        String status = cbStatusFilter.getValue();

        daftarTiketTersaring.setPredicate(tiket -> {
            boolean cocokTeks = kueri.isEmpty()
                    || tiket.getKodeTiket().toLowerCase().contains(kueri)
                    || tiket.getPenumpang().getNamaLengkap().toLowerCase().contains(kueri);

            LocalDate tanggalBerangkat = tiket.getJadwal().getWaktuBerangkat().toLocalDate();
            boolean cocokDari = dari == null || !tanggalBerangkat.isBefore(dari);
            boolean cocokSampai = sampai == null || !tanggalBerangkat.isAfter(sampai);

            boolean cocokStatus = status == null
                    || status.equals("Semua Status")
                    || tiket.getStatus().equalsIgnoreCase(status);

            return cocokTeks && cocokDari && cocokSampai && cocokStatus;
        });
    }

    private void perbaruiStatistik() {
        int totalTransaksi = daftarTiketTersaring.size();

        double totalPendapatan = daftarTiketTersaring.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("AKTIF"))
                .mapToDouble(Tiket::getHargaTotal)
                .sum();

        long tiketAktif = daftarTiketTersaring.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("AKTIF"))
                .count();

        long tiketDibatalkan = daftarTiketTersaring.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("DIBATALKAN"))
                .count();

        lblTotalTransaksi.setText(String.valueOf(totalTransaksi));
        lblTotalPendapatan.setText(TiketHelper.formatHarga(totalPendapatan));
        lblTiketAktif.setText(String.valueOf(tiketAktif));
        lblTiketDibatalkan.setText(String.valueOf(tiketDibatalkan));
    }

    @FXML
    private void handleResetFilter() {
        tfSearch.clear();
        dpDari.setValue(null);
        dpSampai.setValue(null);
        cbStatusFilter.setValue("Semua Status");
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan Penjualan");
        fileChooser.setInitialFileName("laporan_penjualan.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));

        File file = fileChooser.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Kode Tiket,Penumpang,Kereta,Rute,Berangkat,Harga,Status");
            for (Tiket tiket : daftarTiketTersaring) {
                writer.println(barisCsv(tiket));
            }
            tampilkanAlertInfo("Laporan berhasil diekspor ke " + file.getName());
        } catch (IOException e) {
            tampilkanAlertError("Gagal mengekspor laporan: " + e.getMessage());
        }
    }

    private String barisCsv(Tiket tiket) {
        Rute rute = tiket.getJadwal().getRute();
        return String.join(",",
                kutip(tiket.getKodeTiket()),
                kutip(tiket.getPenumpang().getNamaLengkap()),
                kutip(tiket.getJadwal().getKereta().getNama()),
                kutip(rute.getStasiunAsal() + " - " + rute.getStasiunTujuan()),
                kutip(DateTimeUtil.formatWaktu(tiket.getJadwal().getWaktuBerangkat())),
                String.valueOf(tiket.getHargaTotal()),
                kutip(tiket.getStatus())
        );
    }

    private String kutip(String teks) {
        return "\"" + teks.replace("\"", "\"\"") + "\"";
    }

    private void tampilkanAlertInfo(String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Berhasil");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
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
        SceneManager.switchScene("KelolaJadwal.fxml");
    }

    @FXML
    private void handleNavPromo() {
        SceneManager.switchScene("KelolaPromo.fxml");
    }

    @FXML
    private void handleNavLaporan() {
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