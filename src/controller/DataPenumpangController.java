package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Jadwal;
import org.kordamp.ikonli.javafx.FontIcon;
import util.AvatarManager;
import util.PenumpangSession;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DataPenumpangController implements Initializable {

    @FXML private Label     lblNama;
    @FXML private FontIcon  topbarIcon;
    @FXML private ImageView topbarAvatar;
    @FXML private HBox   timerBox;
    @FXML private Label  lblTimer;
    @FXML private Label lblKereta;
    @FXML private Label lblKelas;
    @FXML private Label lblJamBerangkat;
    @FXML private Label lblAsal;
    @FXML private Label lblJamTiba;
    @FXML private Label lblTujuan;
    @FXML private Label lblDurasi;
    @FXML private Label lblHarga;
    @FXML private VBox  vboxForms;
    @FXML private Label lblError;

    private static final DateTimeFormatter FMT_WAKTU = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final List<Object[]> fieldList = new ArrayList<>();
    private Timeline bookingTimer;
    private int remainingSeconds = 15 * 60;

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

        isiSummaryJadwal(j);
        buatFormPenumpang(PenumpangSession.getJumlahDewasa(), PenumpangSession.getJumlahBayi(), j);
        mulaiTimer();
    }

    private void isiSummaryJadwal(Jadwal j) {
        lblKereta.setText(j.getKereta().getNama() + " · " + j.getKereta().getNomorKereta());
        lblKelas.setText(j.getJenisKelas().getNamaKelas());
        lblJamBerangkat.setText(j.getWaktuBerangkat().format(FMT_WAKTU));
        lblAsal.setText(j.getRute().getStasiunAsal());
        lblJamTiba.setText(j.getWaktuTiba().format(FMT_WAKTU));
        lblTujuan.setText(j.getRute().getStasiunTujuan());

        int menit = j.getRute().getEstimasiMenit();
        String durasi = menit >= 60
                ? (menit / 60) + " jam" + (menit % 60 != 0 ? " " + (menit % 60) + " mnt" : "")
                : menit + " mnt";
        lblDurasi.setText(durasi);

        String hargaStr = IDR.format((long) j.getHargaEfektif())
                .replace("Rp", "Rp ").replace(",00", "");
        lblHarga.setText(hargaStr);
    }

    private void buatFormPenumpang(int jumlahDewasa, int jumlahBayi, Jadwal j) {
        fieldList.clear();
        vboxForms.getChildren().clear();

        int index = 1;
        for (int i = 0; i < jumlahDewasa; i++) {
            VBox card = buatKartuForm(index++, "DEWASA", j);
            vboxForms.getChildren().add(card);
        }
        for (int i = 0; i < jumlahBayi; i++) {
            VBox card = buatKartuForm(index++, "BAYI", j);
            vboxForms.getChildren().add(card);
        }
    }

    private VBox buatKartuForm(int nomorPenumpang, String tipe, Jadwal j) {
        VBox card = new VBox(16);
        card.getStyleClass().add("passenger-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("passenger-card-header");

        Label badge = new Label(String.valueOf(nomorPenumpang));
        badge.getStyleClass().add("passenger-number-badge");

        VBox headerText = new VBox(2);
        Label title = new Label("Penumpang #" + nomorPenumpang + " (" + ("DEWASA".equals(tipe) ? "Dewasa" : "Bayi") + ")");
        title.getStyleClass().add("passenger-title");
        Label subtitle = new Label("DEWASA".equals(tipe)
                ? "Usia 3 tahun ke atas (Mendapatkan Kursi)"
                : "Usia di bawah 3 tahun (Dipangku, Tanpa Kursi)");
        subtitle.getStyleClass().add("passenger-subtitle");
        headerText.getChildren().addAll(title, subtitle);

        header.getChildren().addAll(badge, headerText);

        Region divider = new Region();
        divider.getStyleClass().add("divider-h");

        VBox fieldNama = new VBox(6);
        Label lblFieldNama = new Label("Nama Lengkap");
        lblFieldNama.getStyleClass().add("form-label");
        TextField tfNama = new TextField();
        tfNama.setPromptText("Masukkan nama lengkap sesuai KTP / Paspor");
        tfNama.getStyleClass().add("form-field");

        if (nomorPenumpang == 1 && "DEWASA".equals(tipe)) {
            var user = SessionManager.getInstance().getCurrentUser();
            if (user instanceof model.Penumpang p) {
                if (p.getNamaLengkap() != null && !p.getNamaLengkap().isEmpty())
                    tfNama.setText(p.getNamaLengkap());
            }
        }

        fieldNama.getChildren().addAll(lblFieldNama, tfNama);

        VBox fieldNik = new VBox(6);
        Label lblFieldNik = new Label("NIK / Nomor Identitas");
        lblFieldNik.getStyleClass().add("form-label");
        TextField tfNik = new TextField();
        tfNik.setPromptText("16 digit NIK atau nomor identitas");
        tfNik.getStyleClass().add("form-field");

        if (nomorPenumpang == 1 && "DEWASA".equals(tipe)) {
            var user = SessionManager.getInstance().getCurrentUser();
            if (user instanceof model.Penumpang p) {
                if (p.getNik() != null && !p.getNik().isEmpty())
                    tfNik.setText(p.getNik());
            }
        }

        Label hintNik = new Label("Gunakan NIK KTP (16 digit) atau nomor identitas resmi");
        hintNik.getStyleClass().add("form-hint");
        fieldNik.getChildren().addAll(lblFieldNik, tfNik, hintNik);

        VBox fieldTgl = new VBox(6);
        Label lblFieldTgl = new Label("Tanggal Lahir");
        lblFieldTgl.getStyleClass().add("form-label");
        DatePicker dpTgl = new DatePicker();
        dpTgl.setPromptText("Pilih tanggal lahir...");
        dpTgl.getStyleClass().add("form-field");
        dpTgl.setMaxWidth(Double.MAX_VALUE);
        fieldTgl.getChildren().addAll(lblFieldTgl, dpTgl);

        card.getChildren().addAll(header, divider, fieldNama, fieldNik, fieldTgl);

        fieldList.add(new Object[]{tfNama, tfNik, dpTgl, tipe});

        return card;
    }

    private void mulaiTimer() {
        bookingTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTampilTimer();

            if (remainingSeconds <= 0) {
                bookingTimer.stop();
                timerHabis();
            }
        }));
        bookingTimer.setCycleCount(Timeline.INDEFINITE);
        bookingTimer.play();
        updateTampilTimer();
    }

    private void updateTampilTimer() {
        int menit  = remainingSeconds / 60;
        int detik  = remainingSeconds % 60;
        lblTimer.setText(String.format("%02d:%02d", menit, detik));

        boolean isWarn = remainingSeconds <= 5 * 60;

        timerBox.getStyleClass().removeAll("timer-box", "timer-box-warn");
        timerBox.getStyleClass().add(isWarn ? "timer-box-warn" : "timer-box");
        lblTimer.getStyleClass().removeAll("timer-value", "timer-value-warn");
        lblTimer.getStyleClass().add(isWarn ? "timer-value-warn" : "timer-value");

        timerBox.getChildren().forEach(node -> {
            if (node instanceof FontIcon fi) {
                fi.getStyleClass().removeAll("timer-icon", "timer-icon-warn");
                fi.getStyleClass().add(isWarn ? "timer-icon-warn" : "timer-icon");
            }
            if (node instanceof VBox vb) {
                vb.getChildren().forEach(child -> {
                    if (child instanceof Label lbl && lbl != lblTimer) {
                        lbl.getStyleClass().removeAll("timer-label", "timer-label-warn");
                        lbl.getStyleClass().add(isWarn ? "timer-label-warn" : "timer-label");
                    }
                });
            }
        });
    }

    private void timerHabis() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Waktu Pemesanan Habis");
        alert.setHeaderText("Sesi Pemesanan Kedaluwarsa");
        alert.setContentText(
            "Waktu pemesanan Anda telah habis (15 menit).\n" +
            "Silakan cari jadwal kembali dari halaman Beranda."
        );
        alert.showAndWait();
        PenumpangSession.setJadwalDipilih(null);
        SceneManager.switchScene("HomePenumpang.fxml");
    }

    private void hentikanTimer() {
        if (bookingTimer != null) bookingTimer.stop();
    }

    @FXML
    private void handleLanjutPilihKursi() {
        List<String[]> manifes = new ArrayList<>();
        for (int i = 0; i < fieldList.size(); i++) {
            TextField tfNama = (TextField) fieldList.get(i)[0];
            TextField tfNik  = (TextField) fieldList.get(i)[1];
            DatePicker dpTgl = (DatePicker) fieldList.get(i)[2];
            String tipe      = (String) fieldList.get(i)[3];
            String nama = tfNama.getText().trim();
            String nik  = tfNik.getText().trim();
            LocalDate tglLahir = dpTgl.getValue();

            if (nama.isEmpty()) {
                tampilkanError("Nama lengkap penumpang " + (i + 1) + " belum diisi.");
                return;
            }
            if (nik.isEmpty()) {
                tampilkanError("NIK / No. Identitas penumpang " + (i + 1) + " belum diisi.");
                return;
            }
            if (tglLahir == null) {
                tampilkanError("Tanggal lahir penumpang " + (i + 1) + " belum diisi.");
                return;
            }
            if (tglLahir.isAfter(LocalDate.now())) {
                tampilkanError("Tanggal lahir penumpang " + (i + 1) + " tidak boleh di masa depan.");
                return;
            }

            int usia = java.time.Period.between(tglLahir, LocalDate.now()).getYears();

            if ("DEWASA".equals(tipe) && usia < 3) {
                tampilkanError("Penumpang " + (i + 1) + " divalidasi sebagai Dewasa namun berusia " + usia + " tahun (harus ≥ 3 tahun).");
                return;
            }
            if ("BAYI".equals(tipe) && usia >= 3) {
                tampilkanError("Penumpang " + (i + 1) + " divalidasi sebagai Bayi namun berusia " + usia + " tahun (harus < 3 tahun).");
                return;
            }

            manifes.add(new String[]{nama, nik, String.valueOf(usia), tipe});
        }

        sembunyikanError();
        PenumpangSession.setManifesPenumpang(manifes);
        PenumpangSession.setSisaDetikTimer(remainingSeconds);
        hentikanTimer();

        SceneManager.switchScene("PilihKursi.fxml");
    }

    @FXML
    private void handleKembali() {
        hentikanTimer();
        SceneManager.switchScene("PilihJadwal.fxml");
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void sembunyikanError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}