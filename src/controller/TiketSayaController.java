package controller;

import dao.TiketDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import model.Penumpang;
import model.Tiket;
import util.AvatarManager;
import util.PenumpangSession;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class TiketSayaController implements Initializable {

    @FXML private Label lblNama;
    @FXML private FontIcon topbarIcon;
    @FXML private ImageView topbarAvatar;
    @FXML private Label lblKpiTotal;
    @FXML private Label lblKpiAktif;
    @FXML private Label lblKpiBatal;
    @FXML private TextField tfCari;

    @FXML private TableView<Tiket>          tblTiket;
    @FXML private TableColumn<Tiket,String> colKode;
    @FXML private TableColumn<Tiket,String> colRute;
    @FXML private TableColumn<Tiket,String> colKereta;
    @FXML private TableColumn<Tiket,String> colTanggal;
    @FXML private TableColumn<Tiket,String> colKursi;
    @FXML private TableColumn<Tiket,String> colHarga;
    @FXML private TableColumn<Tiket,String> colStatus;
    @FXML private TableColumn<Tiket,Void>   colAksi;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm",
            new Locale("id", "ID"));
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private final TiketDAO tiketDAO = new TiketDAO();
    private ObservableList<Tiket> masterList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = SessionManager.getInstance().getCurrentUser();
        lblNama.setText(user.getUsername());
        AvatarManager.loadAvatar(user.getUsername(), topbarAvatar, topbarIcon, 24);

        setupKolom();

        if (SessionManager.getInstance().getCurrentUser() instanceof Penumpang p) {
            muatData(p);
        }

        setupPencarian();
    }

    private void setupKolom() {
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeTiket()));
        colRute.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getJadwal().getRute().getStasiunAsal() +
                " → " + c.getValue().getJadwal().getRute().getStasiunTujuan()));
        colKereta.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getJadwal().getKereta().getNama()));
        colTanggal.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getJadwal().getWaktuBerangkat().format(FMT)));
        colKursi.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getKursi().getNomorKursi()));
        colHarga.setCellValueFactory(c -> new SimpleStringProperty(
                IDR.format((long) c.getValue().getHargaTotal()).replace("Rp", "Rp ").replace(",00", "")));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                String status = getTableRow().getItem().getStatus();
                Label badge = new Label(status);
                badge.getStyleClass().add(switch (status.toUpperCase()) {
                    case "AKTIF"       -> "badge-aktif";
                    case "DIBATALKAN"  -> "badge-dibatalkan";
                    default            -> "badge-pending";
                });
                setGraphic(badge);
                setText(null);
            }
        });
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnCetak = new Button("E-Tiket");
            private final Button btnBatal = new Button("Batalkan");
            private final HBox   box      = new HBox(6, btnCetak, btnBatal);

            {
                btnCetak.getStyleClass().add("btn-cetak");
                btnBatal.getStyleClass().add("btn-batal");
                box.setAlignment(Pos.CENTER);
                btnCetak.setOnAction(e -> cetakETiket(getTableView().getItems().get(getIndex())));
                btnBatal.setOnAction(e -> batalkanTiket(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); return;
                }
                Tiket t = getTableView().getItems().get(getIndex());
                btnBatal.setVisible("AKTIF".equalsIgnoreCase(t.getStatus()));
                btnBatal.setManaged("AKTIF".equalsIgnoreCase(t.getStatus()));
                setGraphic(box);
            }
        });
    }

    private void muatData(Penumpang p) {
        List<Tiket> tikets = tiketDAO.findByPenumpang(p);
        masterList = FXCollections.observableArrayList(tikets);

        long aktif = tikets.stream().filter(t -> "AKTIF".equalsIgnoreCase(t.getStatus())).count();
        long batal = tikets.stream().filter(t -> "DIBATALKAN".equalsIgnoreCase(t.getStatus())).count();

        lblKpiTotal.setText(String.valueOf(tikets.size()));
        lblKpiAktif.setText(String.valueOf(aktif));
        lblKpiBatal.setText(String.valueOf(batal));

        tblTiket.setItems(masterList);
    }

    private void setupPencarian() {
        tfCari.textProperty().addListener((obs, lama, baru) -> {
            if (masterList == null) return;
            String kueri = baru.toLowerCase().trim();
            FilteredList<Tiket> filtered = new FilteredList<>(masterList, t -> {
                if (kueri.isEmpty()) return true;
                return t.getKodeTiket().toLowerCase().contains(kueri) ||
                       t.getJadwal().getRute().getStasiunAsal().toLowerCase().contains(kueri) ||
                       t.getJadwal().getRute().getStasiunTujuan().toLowerCase().contains(kueri) ||
                       t.getJadwal().getKereta().getNama().toLowerCase().contains(kueri);
            });
            tblTiket.setItems(filtered);
        });
    }

    /**
     * Cetak E-Tiket menggunakan JavaFX PrinterJob.
     * Membangun node VBox yang berisi informasi tiket lengkap lalu mengirimnya ke printer.
     */
    private void cetakETiket(Tiket tiket) {
        // Buat konten e-tiket
        VBox konten = new VBox(16);
        konten.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-font-family: 'Inter', 'Segoe UI';");
        konten.setPrefWidth(600);

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label ikon = new Label("🚆");
        ikon.setStyle("-fx-font-size: 32;");
        VBox brandBox = new VBox(2);
        Label brandName = new Label("CityRailGO");
        brandName.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0A192F;");
        Label brandSub = new Label("E-Tiket Penumpang");
        brandSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6E84A8;");
        brandBox.getChildren().addAll(brandName, brandSub);
        header.getChildren().addAll(ikon, brandBox);

        // Garis
        javafx.scene.control.Separator sep1 = new javafx.scene.control.Separator();

        // Kode tiket besar
        Label kodeTiket = new Label(tiket.getKodeTiket());
        kodeTiket.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4D8AFF; -fx-alignment: CENTER;");
        kodeTiket.setMaxWidth(Double.MAX_VALUE);
        kodeTiket.setAlignment(Pos.CENTER);

        // Grid detail
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);
        grid.setStyle("-fx-background-color: #F7F9FC; -fx-background-radius: 10; -fx-padding: 16;");

        String[][] data = {
            {"Penumpang", tiket.getPenumpang().getUsername()},
            {"Kereta", tiket.getJadwal().getKereta().getNama() + " (" + tiket.getJadwal().getKereta().getNomorKereta() + ")"},
            {"Kelas", tiket.getJadwal().getJenisKelas().getNamaKelas()},
            {"Rute", tiket.getJadwal().getRute().getStasiunAsal() + " → " + tiket.getJadwal().getRute().getStasiunTujuan()},
            {"Berangkat", tiket.getJadwal().getWaktuBerangkat().format(FMT)},
            {"Tiba", tiket.getJadwal().getWaktuTiba().format(FMT)},
            {"Kursi", tiket.getKursi().getNomorKursi()},
            {"Harga", IDR.format((long) tiket.getHargaTotal()).replace("Rp", "Rp ").replace(",00", "")},
            {"Status", tiket.getStatus()},
        };

        for (int i = 0; i < data.length; i++) {
            Label lbl = new Label(data[i][0]);
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6E84A8; -fx-font-weight: bold;");
            Label val = new Label(data[i][1]);
            val.setStyle("-fx-font-size: 13px; -fx-text-fill: #0A192F; -fx-font-weight: bold;");
            grid.add(lbl, 0, i);
            grid.add(val, 1, i);
        }

        javafx.scene.control.Separator sep2 = new javafx.scene.control.Separator();

        Label footer = new Label("Tiket ini adalah bukti pemesanan yang sah. Tunjukkan kepada petugas saat boarding.");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CACC4; -fx-wrap-text: true;");
        footer.setWrapText(true);

        konten.getChildren().addAll(header, sep1, kodeTiket, grid, sep2, footer);

        // Tampilkan dialog preview + print
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean ok = job.showPrintDialog(SceneManager.getPrimaryStage());
            if (ok) {
                // Buat scene sementara untuk render
                Scene tempScene = new Scene(konten, 600, 800);
                konten.applyCss();
                konten.layout();
                boolean printed = job.printPage(konten);
                if (printed) job.endJob();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Tidak ada printer yang terdeteksi.").showAndWait();
        }
    }

    private void batalkanTiket(Tiket tiket) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Batalkan Tiket");
        konfirmasi.setHeaderText("Batalkan tiket " + tiket.getKodeTiket() + "?");
        konfirmasi.setContentText("Tindakan ini tidak dapat dibatalkan. Tiket akan berstatus DIBATALKAN.");
        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            tiket.setStatus("DIBATALKAN");
            tiketDAO.update(tiket);
            tblTiket.refresh();
            // Update KPI
            if (SessionManager.getInstance().getCurrentUser() instanceof Penumpang p) {
                muatData(p);
            }
        }
    }

    // ===== Nav =====
    @FXML private void handleNavBeranda()  { SceneManager.switchScene("HomePenumpang.fxml"); }
    @FXML private void handleNavProfil()   { SceneManager.switchScene("Profil.fxml"); }

    @FXML
    private void handleLogout() {
        Alert k = new Alert(Alert.AlertType.CONFIRMATION);
        k.setHeaderText(null);
        k.setContentText("Yakin ingin logout?");
        Optional<ButtonType> h = k.showAndWait();
        if (h.isPresent() && h.get() == ButtonType.OK) {
            PenumpangSession.reset();
            SessionManager.getInstance().logout();
            SceneManager.switchScene("login.fxml");
        }
    }
}
