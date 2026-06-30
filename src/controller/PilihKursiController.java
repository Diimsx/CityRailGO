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
    @FXML private VBox   vboxSeatMap;
    @FXML private Label  lblKursiDipilih;
    @FXML private Label  lblInfoKursi;
    @FXML private Button btnLanjut;
    @FXML private Label  lblBtnLanjut;
    @FXML private FontIcon iconBtnLanjut;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private final KursiDAO kursiDAO = new KursiDAO();
    private Kursi kursiDipilih = null;
    private Button btnSeatDipilih = null;

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

        // Isi summary jadwal
        lblKereta.setText(j.getKereta().getNama() + " (" + j.getKereta().getNomorKereta() + ")");
        lblKelas.setText(j.getJenisKelas().getNamaKelas());
        lblJamBerangkat.setText(j.getWaktuBerangkat().format(FMT));
        lblAsalRute.setText(j.getRute().getStasiunAsal());
        lblJamTiba.setText(j.getWaktuTiba().format(FMT));
        lblTujuanRute.setText(j.getRute().getStasiunTujuan());
        String hargaStr = IDR.format((long) j.getHargaEfektif()).replace("Rp", "Rp ").replace(",00", "");
        lblHarga.setText(hargaStr);

        // Muat denah kursi
        List<Kursi> semuaKursi = kursiDAO.findByJadwal(j);
        long tersedia = semuaKursi.stream().filter(k -> "TERSEDIA".equalsIgnoreCase(k.getStatus())).count();
        lblSisaKursi.setText(tersedia + " kursi tersedia");
        renderSeatMap(semuaKursi);
    }

    /**
     * Render seat map layout 2+2 per baris (A,B | lorong | C,D).
     * Setiap baris ditandai dengan nomor baris di kiri.
     */
    private void renderSeatMap(List<Kursi> kursiList) {
        vboxSeatMap.getChildren().clear();

        // Header kolom: A  B  [lorong]  C  D
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(
            buatLabel("", 28), buatLabel("A", 48), buatLabel("B", 48),
            buatLabel("", 32),
            buatLabel("C", 48), buatLabel("D", 48)
        );
        vboxSeatMap.getChildren().add(header);

        // Baris per 4 kursi (A, B, C, D)
        int total = kursiList.size();
        int baris = (int) Math.ceil(total / 4.0);
        for (int r = 0; r < baris; r++) {
            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER);

            // Nomor baris
            Label lblBaris = buatLabel(String.valueOf(r + 1), 28);
            lblBaris.getStyleClass().add("row-label");
            row.getChildren().add(lblBaris);

            // Kursi A, B
            int base = r * 4;
            row.getChildren().add(base < total ? buatTombolKursi(kursiList.get(base)) : buatSlot());
            row.getChildren().add(base + 1 < total ? buatTombolKursi(kursiList.get(base + 1)) : buatSlot());

            // Lorong
            Region lorong = new Region();
            lorong.setMinWidth(32); lorong.setMaxWidth(32);
            row.getChildren().add(lorong);

            // Kursi C, D
            row.getChildren().add(base + 2 < total ? buatTombolKursi(kursiList.get(base + 2)) : buatSlot());
            row.getChildren().add(base + 3 < total ? buatTombolKursi(kursiList.get(base + 3)) : buatSlot());

            vboxSeatMap.getChildren().add(row);
        }
    }

    private Button buatTombolKursi(Kursi kursi) {
        Button btn = new Button(kursi.getNomorKursi());
        boolean tersedia = "TERSEDIA".equalsIgnoreCase(kursi.getStatus());
        btn.getStyleClass().add(tersedia ? "seat-tersedia" : "seat-terisi");
        btn.setDisable(!tersedia);

        if (tersedia) {
            btn.setOnAction(e -> pilihKursi(kursi, btn));
        }
        return btn;
    }

    private Region buatSlot() {
        Region r = new Region();
        r.setMinSize(48, 46); r.setMaxSize(48, 46);
        return r;
    }

    private Label buatLabel(String teks, double width) {
        Label l = new Label(teks);
        l.getStyleClass().add("aisle-label");
        l.setMinWidth(width); l.setMaxWidth(width);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private void pilihKursi(Kursi kursi, Button btn) {
        // Reset kursi sebelumnya
        if (btnSeatDipilih != null) {
            btnSeatDipilih.getStyleClass().removeAll("seat-dipilih");
            btnSeatDipilih.getStyleClass().add("seat-tersedia");
        }

        kursiDipilih = kursi;
        btnSeatDipilih = btn;

        btn.getStyleClass().removeAll("seat-tersedia");
        btn.getStyleClass().add("seat-dipilih");

        lblKursiDipilih.setText("Kursi dipilih: " + kursi.getNomorKursi());
        lblInfoKursi.setText("Klik 'Lanjut ke Pembayaran' untuk melanjutkan");

        // Aktifkan tombol lanjut
        btnLanjut.setDisable(false);
        btnLanjut.getStyleClass().removeAll("btn-lanjut-disabled");
        btnLanjut.getStyleClass().add("btn-lanjut");
        lblBtnLanjut.setText("Lanjut ke Pembayaran");
        lblBtnLanjut.setStyle("-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold;");
        iconBtnLanjut.setStyle("-fx-icon-size:14; -fx-icon-color:white;");
    }

    @FXML
    private void handleLanjutPembayaran() {
        if (kursiDipilih == null) return;
        PenumpangSession.setKursiDipilih(kursiDipilih);
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
