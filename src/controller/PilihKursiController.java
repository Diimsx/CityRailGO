package controller;

import dao.KursiDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Jadwal;
import model.Kursi;
import org.kordamp.ikonli.javafx.FontIcon;
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

public class PilihKursiController implements Initializable {

    @FXML private Label  lblNama;
    @FXML private FontIcon topbarIcon;
    @FXML private ImageView topbarAvatar;
    @FXML private Label  lblKereta;
    @FXML private Label  lblKelas;
    @FXML private Label  lblJamBerangkat;
    @FXML private Label  lblAsalRute;
    @FXML private Label  lblJamTiba;
    @FXML private Label  lblTujuanRute;
    @FXML private Label  lblHarga;
    @FXML private Label  lblSisaKursi;
    @FXML private HBox   hboxCarriageSelector;
    @FXML private VBox   vboxSeatMap;
    @FXML private Label  lblKursiDipilih;
    @FXML private Label  lblInfoKursi;
    @FXML private Button btnLanjut;
    @FXML private Label  lblBtnLanjut;
    @FXML private FontIcon iconBtnLanjut;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private final KursiDAO kursiDAO = new KursiDAO();
    private List<Kursi> semuaKursi;
    private int gerbongAktif = 1;
    private int seatsPerCarriage = 80;
    private int jumlahGerbong = 1;
    private int N = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = SessionManager.getInstance().getCurrentUser();
        lblNama.setText(user.getUsername());
        AvatarManager.loadAvatar(user.getUsername(), topbarAvatar, topbarIcon, 24);

        Jadwal j = PenumpangSession.getJadwalDipilih();
        if (j == null) {
            SceneManager.switchScene("HomePenumpang.fxml");
            return;
        }

        N = PenumpangSession.getJumlahDewasa();
        PenumpangSession.getListKursiDipilih().clear();

        lblKereta.setText(j.getKereta().getNama() + " (" + j.getKereta().getNomorKereta() + ")");
        lblKelas.setText(j.getJenisKelas().getNamaKelas());
        lblJamBerangkat.setText(j.getWaktuBerangkat().format(FMT));
        lblAsalRute.setText(j.getRute().getStasiunAsal());
        lblJamTiba.setText(j.getWaktuTiba().format(FMT));
        lblTujuanRute.setText(j.getRute().getStasiunTujuan());
        String hargaStr = IDR.format((long) j.getHargaEfektif()).replace("Rp", "Rp ").replace(",00", "");
        lblHarga.setText(hargaStr);

        String kelas = j.getJenisKelas().getNamaKelas().toLowerCase();
        if (kelas.contains("eksekutif")) {
            seatsPerCarriage = 50;
            jumlahGerbong = j.getKereta().getGerbongEksekutif();
        } else if (kelas.contains("bisnis")) {
            seatsPerCarriage = 60;
            jumlahGerbong = j.getKereta().getGerbongBisnis();
        } else {
            seatsPerCarriage = 80;
            jumlahGerbong = j.getKereta().getGerbongEkonomi();
        }

        semuaKursi = kursiDAO.findByJadwal(j);
        
        if (jumlahGerbong == 0) {
            jumlahGerbong = (int) Math.ceil((double) semuaKursi.size() / seatsPerCarriage);
        }
        if (jumlahGerbong <= 0) {
            jumlahGerbong = 1;
        }

        long tersedia = semuaKursi.stream().filter(k -> "TERSEDIA".equalsIgnoreCase(k.getStatus())).count();
        lblSisaKursi.setText(tersedia + " kursi tersedia");

        setupCarriageTabs();
        
        tampilkanGerbong(1);
        updateStatusLanjut();
    }

    private void setupCarriageTabs() {
        hboxCarriageSelector.getChildren().clear();
        for (int i = 1; i <= jumlahGerbong; i++) {
            final int carriageNum = i;
            Button tabBtn = new Button("Gerbong " + i);
            tabBtn.setCursor(javafx.scene.Cursor.HAND);
            tabBtn.setOnAction(e -> tampilkanGerbong(carriageNum));
            hboxCarriageSelector.getChildren().add(tabBtn);
        }
    }

    private void tampilkanGerbong(int carriageNum) {
        gerbongAktif = carriageNum;
        
        for (int i = 0; i < hboxCarriageSelector.getChildren().size(); i++) {
            Button btn = (Button) hboxCarriageSelector.getChildren().get(i);
            btn.setStyle(null);
            btn.getStyleClass().removeAll("btn-metode-active", "btn-metode");
            if (i == (carriageNum - 1)) {
                btn.setStyle("-fx-background-color: #1B3A6B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16;");
            } else {
                btn.setStyle("-fx-background-color: #EEF1F6; -fx-text-fill: #6E84A8; -fx-background-radius: 8; -fx-padding: 8 16 8 16;");
            }
        }

        int startIdx = (carriageNum - 1) * seatsPerCarriage;
        int endIdx = Math.min(carriageNum * seatsPerCarriage, semuaKursi.size());
        
        List<Kursi> kursiGerbong = new java.util.ArrayList<>();
        for (int i = startIdx; i < endIdx; i++) {
            kursiGerbong.add(semuaKursi.get(i));
        }

        renderSeatMap(kursiGerbong);
    }

    private void renderSeatMap(List<Kursi> kursiList) {
        vboxSeatMap.getChildren().clear();

        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(
            buatLabel("", 28), buatLabel("A", 46), buatLabel("B", 46),
            buatLabel("", 32),
            buatLabel("C", 46), buatLabel("D", 46)
        );
        vboxSeatMap.getChildren().add(header);

        int total = kursiList.size();
        int baris = (int) Math.ceil(total / 4.0);
        for (int r = 0; r < baris; r++) {
            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER);

            Label lblBaris = buatLabel(String.valueOf(r + 1), 28);
            lblBaris.getStyleClass().add("row-label");
            row.getChildren().add(lblBaris);

            int base = r * 4;
            row.getChildren().add(base < total ? buatTombolKursi(kursiList.get(base)) : buatSlot());
            row.getChildren().add(base + 1 < total ? buatTombolKursi(kursiList.get(base + 1)) : buatSlot());

            Region lorong = new Region();
            lorong.setMinWidth(32); lorong.setMaxWidth(32);
            row.getChildren().add(lorong);

            row.getChildren().add(base + 2 < total ? buatTombolKursi(kursiList.get(base + 2)) : buatSlot());
            row.getChildren().add(base + 3 < total ? buatTombolKursi(kursiList.get(base + 3)) : buatSlot());

            vboxSeatMap.getChildren().add(row);
        }
    }

    private Button buatTombolKursi(Kursi kursi) {
        String formatLabel = String.format("G%d-%s", gerbongAktif, kursi.getNomorKursi());
        Button btn = new Button(formatLabel);
        btn.setMinSize(46, 46);
        btn.setMaxSize(46, 46);
        btn.setStyle("-fx-font-size: 10px; -fx-padding: 0;");
        
        boolean tersedia = "TERSEDIA".equalsIgnoreCase(kursi.getStatus());
        boolean selected = PenumpangSession.getListKursiDipilih().contains(kursi);

        if (!tersedia) {
            btn.getStyleClass().add("seat-terisi");
            btn.setDisable(true);
        } else if (selected) {
            btn.getStyleClass().add("seat-dipilih");
            btn.setOnAction(e -> togglePilihKursi(kursi, btn));
        } else {
            btn.getStyleClass().add("seat-tersedia");
            btn.setOnAction(e -> togglePilihKursi(kursi, btn));
        }
        return btn;
    }

    private Region buatSlot() {
        Region r = new Region();
        r.setMinSize(46, 46); r.setMaxSize(46, 46);
        return r;
    }

    private Label buatLabel(String teks, double width) {
        Label l = new Label(teks);
        l.getStyleClass().add("aisle-label");
        l.setMinWidth(width); l.setMaxWidth(width);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private void togglePilihKursi(Kursi kursi, Button btn) {
        List<Kursi> terpilih = PenumpangSession.getListKursiDipilih();
        if (terpilih.contains(kursi)) {
            terpilih.remove(kursi);
            btn.getStyleClass().removeAll("seat-dipilih");
            btn.getStyleClass().add("seat-tersedia");
        } else {
            if (terpilih.size() >= N) {
                Alert w = new Alert(Alert.AlertType.WARNING);
                w.setTitle("Batas Pemilihan");
                w.setHeaderText(null);
                w.setContentText("Anda hanya dapat memilih " + N + " kursi sesuai jumlah penumpang.");
                w.showAndWait();
                return;
            }
            terpilih.add(kursi);
            btn.getStyleClass().removeAll("seat-tersedia");
            btn.getStyleClass().add("seat-dipilih");
        }

        updateStatusLanjut();
    }

    private void updateStatusLanjut() {
        List<Kursi> terpilih = PenumpangSession.getListKursiDipilih();
        
        if (terpilih.isEmpty()) {
            lblKursiDipilih.setText("Belum ada kursi dipilih");
            lblInfoKursi.setText("Pilih " + N + " kursi dari denah di atas");
        } else {
            String textKursi = terpilih.stream()
                .map(k -> "G" + (semuaKursi.indexOf(k) / seatsPerCarriage + 1) + "-" + k.getNomorKursi())
                .collect(java.util.stream.Collectors.joining(", "));
            lblKursiDipilih.setText("Kursi dipilih: " + textKursi);
            lblInfoKursi.setText(terpilih.size() + " dari " + N + " kursi dipilih");
        }

        if (terpilih.size() == N) {
            btnLanjut.setDisable(false);
            btnLanjut.getStyleClass().removeAll("btn-lanjut-disabled");
            btnLanjut.getStyleClass().add("btn-lanjut");
            lblBtnLanjut.setText("Lanjut ke Pembayaran →");
            lblBtnLanjut.setStyle("-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold;");
            iconBtnLanjut.setStyle("-fx-icon-size:14; -fx-icon-color:white;");
        } else {
            btnLanjut.setDisable(true);
            btnLanjut.getStyleClass().removeAll("btn-lanjut");
            btnLanjut.getStyleClass().add("btn-lanjut-disabled");
            lblBtnLanjut.setText("Pilih " + (N - terpilih.size()) + " kursi lagi");
            lblBtnLanjut.setStyle("-fx-text-fill:#9CACC4; -fx-font-size:14px; -fx-font-weight:bold;");
            iconBtnLanjut.setStyle("-fx-icon-size:14; -fx-icon-color:#9CACC4;");
        }
    }

    @FXML
    private void handleLanjutPembayaran() {
        if (PenumpangSession.getListKursiDipilih().size() != N) return;
        SceneManager.switchScene("Pembayaran.fxml");
    }

    @FXML private void handleKembali()       { SceneManager.switchScene("PilihJadwal.fxml"); }
    @FXML private void handleNavBeranda()    { SceneManager.switchScene("HomePenumpang.fxml"); }
    @FXML private void handleNavTiketSaya()  { SceneManager.switchScene("TiketSaya.fxml"); }

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
