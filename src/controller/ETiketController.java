package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Tiket;
import model.Pembayaran;
import util.PDFUtil;
import util.QRUtil;
import util.DateUtil;
import util.CurrencyUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * EtiketController — Menampilkan detail e-tiket setelah pembayaran berhasil.
 *
 * Alur:
 *   PembayaranController → setData(tiket, pembayaran) → EtiketController
 *
 * Gunakan:
 *   EtiketController.buka(stage, tiket, pembayaran);
 */
public class EtiketController implements Initializable {

    // ── FXML Injections ──────────────────────────────────────────────────────

    @FXML private Label  lblStatus;
    @FXML private Label  lblNamaKereta;
    @FXML private Label  lblKelas;
    @FXML private Label  lblJamBerangkat;
    @FXML private Label  lblStasiunAsal;
    @FXML private Label  lblKotaAsal;
    @FXML private Label  lblJamTiba;
    @FXML private Label  lblStasiunTujuan;
    @FXML private Label  lblKotaTujuan;
    @FXML private Label  lblDurasi;
    @FXML private Label  lblTanggal;
    @FXML private Label  lblKursi;
    @FXML private Label  lblGerbong;
    @FXML private Label  lblNamaPenumpang;
    @FXML private Label  lblNoId;
    @FXML private Label  lblMetodeBayar;
    @FXML private Label  lblHarga;
    @FXML private Label  lblKodeTiket;
    @FXML private Label  lblQrPlaceholder;
    @FXML private ImageView imgQr;
    @FXML private Button btnKembali;
    @FXML private Button btnUnduh;
    @FXML private Button btnBagikan;
    @FXML private Button btnBerandaUtama;

    // ── State ─────────────────────────────────────────────────────────────────

    private Tiket      tiket;
    private Pembayaran pembayaran;
    private Stage      stage;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Membuka layar E-Tiket di stage yang sama.
     *
     * @param stage      stage aktif
     * @param tiket      objek tiket yang sudah dibayar
     * @param pembayaran objek hasil pembayaran
     */
    public static void buka(Stage stage, Tiket tiket, Pembayaran pembayaran) {
        try {
            FXMLLoader loader = new FXMLLoader(
                EtiketController.class.getResource("/view/EtiketView.fxml")
            );
            Parent root = loader.load();

            EtiketController ctrl = loader.getController();
            ctrl.setStage(stage);
            ctrl.setData(tiket, pembayaran);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CityRailGO — E-Tiket");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Data akan diisi via setData() setelah FXML dimuat
    }

    // ── Setter ────────────────────────────────────────────────────────────────

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Mengisi seluruh UI dari objek Tiket dan Pembayaran.
     * Dipanggil oleh controller pemanggil sebelum menampilkan scene ini.
     */
    public void setData(Tiket tiket, Pembayaran pembayaran) {
        this.tiket      = tiket;
        this.pembayaran = pembayaran;
        populateUI();
    }

    // ── UI Population ─────────────────────────────────────────────────────────

    private void populateUI() {
        if (tiket == null) return;

        // Nama & kelas kereta
        lblNamaKereta.setText(tiket.getKereta().getNamaKereta());
        lblKelas.setText(tiket.getKelas().toUpperCase());

        // Jadwal berangkat & tiba
        lblJamBerangkat.setText(tiket.getKereta().getJamBerangkat());
        lblJamTiba.setText(tiket.getKereta().getJamTiba());
        lblDurasi.setText(hitungDurasi(
            tiket.getKereta().getJamBerangkat(),
            tiket.getKereta().getJamTiba()
        ));

        // Stasiun
        lblStasiunAsal.setText(tiket.getKereta().getStasiunAsal());
        lblKotaAsal.setText(tiket.getKereta().getKotaAsal());
        lblStasiunTujuan.setText(tiket.getKereta().getStasiunTujuan());
        lblKotaTujuan.setText(tiket.getKereta().getKotaTujuan());

        // Detail penumpang
        lblTanggal.setText(DateUtil.formatTanggalLengkap(tiket.getTanggalPerjalanan()));
        lblKursi.setText(tiket.getNomorKursi());
        lblGerbong.setText(String.valueOf(tiket.getNomorGerbong()));
        lblNamaPenumpang.setText(tiket.getPenumpang().getNama());
        lblNoId.setText(tiket.getPenumpang().getNomorId());

        // Pembayaran
        if (pembayaran != null) {
            lblMetodeBayar.setText(pembayaran.getMetodePembayaran());
            lblHarga.setText(CurrencyUtil.formatRupiah(pembayaran.getTotalBayar()));
        }

        // Kode tiket
        lblKodeTiket.setText(tiket.getKodeTiket());

        // QR Code
        muatQrCode(tiket.getKodeTiket());
    }

    /** Generate & tampilkan QR code dari kode tiket. */
    private void muatQrCode(String kodeTiket) {
        try {
            Image qrImage = QRUtil.generateQRImage(kodeTiket, 140, 140);
            if (qrImage != null) {
                imgQr.setImage(qrImage);
                lblQrPlaceholder.setVisible(false);
            }
        } catch (Exception e) {
            // Jika QR gagal, placeholder tetap tampil
            lblQrPlaceholder.setVisible(true);
            System.err.println("[EtiketController] Gagal load QR: " + e.getMessage());
        }
    }

    /** Hitung durasi perjalanan dari string jam "HH:mm". */
    private String hitungDurasi(String jamBerangkat, String jamTiba) {
        try {
            String[] awal  = jamBerangkat.split(":");
            String[] akhir = jamTiba.split(":");
            int menitAwal  = Integer.parseInt(awal[0])  * 60 + Integer.parseInt(awal[1]);
            int menitAkhir = Integer.parseInt(akhir[0]) * 60 + Integer.parseInt(akhir[1]);
            int selisih    = menitAkhir - menitAwal;
            if (selisih < 0) selisih += 24 * 60; // lewat tengah malam
            return (selisih / 60) + "j " + (selisih % 60) + "m";
        } catch (Exception e) {
            return "-";
        }
    }

    // ── Event Handlers ────────────────────────────────────────────────────────

    /** Tombol ← Kembali — kembali ke halaman sebelumnya (Pembayaran). */
    @FXML
    private void handleKembali() {
        // Biasanya e-tiket bersifat final; navigasi kembali cukup ke Home
        handleKembaliBerandaUtama();
    }

    /** Tombol Unduh — ekspor tiket ke PDF melalui dialog simpan file. */
    @FXML
    private void handleUnduh() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Simpan E-Tiket");
        chooser.setInitialFileName("Tiket_" + tiket.getKodeTiket() + ".pdf");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            boolean berhasil = PDFUtil.cetakTiket(tiket, pembayaran, file.getAbsolutePath());
            if (berhasil) {
                tampilkanInfo("Tiket berhasil disimpan:\n" + file.getAbsolutePath());
            } else {
                tampilkanError("Gagal menyimpan tiket. Coba lagi.");
            }
        }
    }

    /** Tombol Bagikan — salin kode tiket ke clipboard / share. */
    @FXML
    private void handleBagikan() {
        String pesanBagikan = buildPesanBagikan();
        // Copy ke clipboard
        javafx.scene.input.Clipboard clipboard =
            javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content =
            new javafx.scene.input.ClipboardContent();
        content.putString(pesanBagikan);
        clipboard.setContent(content);
        tampilkanInfo("Detail tiket disalin ke clipboard!");
    }

    /** Tombol Kembali ke Beranda — navigasi ke HomeController. */
    @FXML
    private void handleKembaliBerandaUtama() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/HomeView.fxml")
            );
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("CityRailGO — Beranda");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Susun teks ringkas tiket untuk dibagikan. */
    private String buildPesanBagikan() {
        return "🚆 CityRailGO E-Tiket\n"
            + "Kereta : " + tiket.getKereta().getNamaKereta() + " ("
                          + tiket.getKelas() + ")\n"
            + "Rute   : " + tiket.getKereta().getStasiunAsal()
                          + " → " + tiket.getKereta().getStasiunTujuan() + "\n"
            + "Tanggal: " + DateUtil.formatTanggalLengkap(tiket.getTanggalPerjalanan()) + "\n"
            + "Kursi  : " + tiket.getNomorKursi()
                          + " / Gerbong " + tiket.getNomorGerbong() + "\n"
            + "Kode   : " + tiket.getKodeTiket() + "\n"
            + "Penumpang: " + tiket.getPenumpang().getNama();
    }

    /** Tampilkan dialog informasi sederhana. */
    private void tampilkanInfo(String pesan) {
        javafx.scene.control.Alert alert =
            new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    /** Tampilkan dialog error sederhana. */
    private void tampilkanError(String pesan) {
        javafx.scene.control.Alert alert =
            new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setHeaderText("Terjadi Kesalahan");
        alert.setContentText(pesan);
        alert.showAndWait();
    }
}