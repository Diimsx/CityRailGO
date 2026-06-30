package controller;

import dao.JadwalDAO;
import dao.KursiDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Jadwal;
import org.kordamp.ikonli.javafx.FontIcon;
import util.AvatarManager;
import util.PenumpangSession;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PilihJadwalController implements Initializable {

    @FXML private Label lblNama;
    @FXML private FontIcon topbarIcon;
    @FXML private ImageView topbarAvatar;
    @FXML private Label lblRute;
    @FXML private Label lblTanggal;
    @FXML private Label lblJumlahPenumpang;
    @FXML private Label lblJumlahHasil;
    @FXML private Label lblCountHasil;
    @FXML private VBox  vboxHasil;
    @FXML private VBox  paneKosong;

    private static final DateTimeFormatter FMT_WAKTU   = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_TANGGAL = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy",
            new java.util.Locale("id", "ID"));
    private static final java.text.NumberFormat IDR = java.text.NumberFormat.getCurrencyInstance(
            new java.util.Locale("id", "ID"));

    private final JadwalDAO jadwalDAO = new JadwalDAO();
    private final KursiDAO  kursiDAO  = new KursiDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = SessionManager.getInstance().getCurrentUser();
        lblNama.setText(user.getUsername());
        AvatarManager.loadAvatar(user.getUsername(), topbarAvatar, topbarIcon, 24);

        String asal   = PenumpangSession.getStasiunAsal();
        String tujuan = PenumpangSession.getStasiunTujuan();
        var tanggal   = PenumpangSession.getTanggalBerangkat();
        int jumlah    = PenumpangSession.getJumlahPenumpang();

        lblRute.setText(asal + "  →  " + tujuan);
        lblTanggal.setText(tanggal != null ? tanggal.format(FMT_TANGGAL) : "—");
        lblJumlahPenumpang.setText(jumlah + " Penumpang");

        muatHasilPencarian(asal, tujuan, tanggal);
    }

    private void muatHasilPencarian(String asal, String tujuan, java.time.LocalDate tanggal) {
        if (asal == null || tujuan == null || tanggal == null) {
            tampilkanKosong();
            return;
        }

        List<Jadwal> daftar = jadwalDAO.findByRuteDanTanggal(asal, tujuan, tanggal);

        if (daftar.isEmpty()) {
            tampilkanKosong();
            return;
        }

        paneKosong.setVisible(false);
        paneKosong.setManaged(false);
        vboxHasil.setVisible(true);
        vboxHasil.setManaged(true);

        lblJumlahHasil.setText("Ditemukan " + daftar.size() + " jadwal tersedia");
        lblCountHasil.setText("untuk " + asal + " → " + tujuan);

        vboxHasil.getChildren().clear();
        for (Jadwal j : daftar) {
            long sisaKursi = kursiDAO.findTersediaByJadwal(j).size();
            vboxHasil.getChildren().add(buatKartuJadwal(j, sisaKursi));
        }
    }

    private HBox buatKartuJadwal(Jadwal jadwal, long sisaKursi) {
        HBox card = new HBox();
        card.getStyleClass().add("jadwal-card");
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);

        // ---- Nama Kereta & Kelas ----
        VBox infoKereta = new VBox(4);
        Label namaKereta = new Label(jadwal.getKereta().getNama());
        namaKereta.getStyleClass().add("kereta-nama");
        Label nomorKereta = new Label(jadwal.getKereta().getNomorKereta());
        nomorKereta.getStyleClass().add("kereta-nomor");

        String kelas = jadwal.getJenisKelas().getNamaKelas();
        Label labelKelas = new Label(kelas.toUpperCase());
        String styleKelas = switch (kelas.toLowerCase()) {
            case "eksekutif" -> "kelas-eksekutif";
            case "bisnis"    -> "kelas-bisnis";
            default          -> "kelas-ekonomi";
        };
        labelKelas.getStyleClass().addAll("kelas-badge", styleKelas);

        infoKereta.getChildren().addAll(namaKereta, nomorKereta, labelKelas);
        infoKereta.setPrefWidth(180);

        // ---- Waktu Berangkat ----
        VBox waktuBerangkat = new VBox(2);
        waktuBerangkat.setAlignment(Pos.CENTER);
        Label jamBerangkat = new Label(jadwal.getWaktuBerangkat().format(FMT_WAKTU));
        jamBerangkat.getStyleClass().add("waktu-text");
        Label stasiAsal = new Label(jadwal.getRute().getStasiunAsal());
        stasiAsal.getStyleClass().add("rute-text");
        waktuBerangkat.getChildren().addAll(jamBerangkat, stasiAsal);

        // ---- Panah & Durasi ----
        VBox tengah = new VBox(4);
        tengah.setAlignment(Pos.CENTER);
        FontIcon panah = new FontIcon("fas-arrow-right");
        panah.getStyleClass().add("panah-icon");

        int menit = jadwal.getRute().getEstimasiMenit();
        String durasi = menit >= 60
                ? (menit / 60) + " jam " + (menit % 60 == 0 ? "" : (menit % 60) + " mnt")
                : menit + " menit";
        Label lblDurasi = new Label(durasi.trim());
        lblDurasi.getStyleClass().add("durasi-text");
        tengah.getChildren().addAll(panah, lblDurasi);

        // ---- Waktu Tiba ----
        VBox waktuTiba = new VBox(2);
        waktuTiba.setAlignment(Pos.CENTER);
        Label jamTiba = new Label(jadwal.getWaktuTiba().format(FMT_WAKTU));
        jamTiba.getStyleClass().add("waktu-text");
        Label stasiTujuan = new Label(jadwal.getRute().getStasiunTujuan());
        stasiTujuan.getStyleClass().add("rute-text");
        waktuTiba.getChildren().addAll(jamTiba, stasiTujuan);

        // ---- Divider ----
        Region div1 = new Region();
        div1.getStyleClass().add("divider-v");

        // ---- Harga & Kursi ----
        VBox infoHarga = new VBox(4);
        infoHarga.setAlignment(Pos.CENTER_RIGHT);
        String hargaStr = IDR.format((long) jadwal.getHargaEfektif())
                .replace("Rp", "Rp ").replace(",00", "");
        Label lblHarga = new Label(hargaStr);
        lblHarga.getStyleClass().add("harga-text");
        Label lblPerOrang = new Label("/ penumpang");
        lblPerOrang.getStyleClass().add("harga-label");

        Label lblSisaKursi = new Label(
                sisaKursi > 0 ? sisaKursi + " kursi tersedia" : "Kursi habis");
        lblSisaKursi.getStyleClass().add(sisaKursi > 0 ? "kursi-text" : "kursi-habis");
        infoHarga.getChildren().addAll(lblHarga, lblPerOrang, lblSisaKursi);

        // ---- Tombol Pilih ----
        Button btnPilih = new Button(sisaKursi > 0 ? "Pilih" : "Habis");
        btnPilih.getStyleClass().add(sisaKursi > 0 ? "btn-pilih" : "btn-pilih-disabled");
        btnPilih.setDisable(sisaKursi == 0);
        btnPilih.setOnAction(e -> handlePilihJadwal(jadwal));

        // ---- Spacer ----
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().addAll(infoKereta, spacer, waktuBerangkat, tengah, waktuTiba, div1, infoHarga, btnPilih);
        return card;
    }

    private void handlePilihJadwal(Jadwal jadwal) {
        PenumpangSession.setJadwalDipilih(jadwal);
        SceneManager.switchScene("PilihKursi.fxml");
    }

    private void tampilkanKosong() {
        vboxHasil.setVisible(false);
        vboxHasil.setManaged(false);
        paneKosong.setVisible(true);
        paneKosong.setManaged(true);
        lblJumlahHasil.setText("Tidak ada jadwal ditemukan");
        lblCountHasil.setText("");
    }

    // ===== Nav =====
    @FXML private void handleNavBeranda()   { SceneManager.switchScene("HomePenumpang.fxml"); }
    @FXML private void handleNavTiketSaya() { SceneManager.switchScene("TiketSaya.fxml"); }

    @FXML
    private void handleLogout() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Logout");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Yakin ingin logout?");
        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            PenumpangSession.reset();
            SessionManager.getInstance().logout();
            SceneManager.switchScene("login.fxml");
        }
    }
}
