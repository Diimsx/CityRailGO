package controller;

import dao.KursiDAO;
import dao.PembayaranDAO;
import dao.TiketDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;
import model.Jadwal;
import model.Kursi;
import model.Pembayaran;
import model.Penumpang;
import model.Tiket;
import util.AvatarManager;
import util.PenumpangSession;
import util.SceneManager;
import util.SessionManager;
import util.TiketHelper;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PembayaranController implements Initializable {

    @FXML private Label lblNama;
    @FXML private FontIcon topbarIcon;
    @FXML private ImageView topbarAvatar;

    // Summary labels
    @FXML private Label lblSumKereta;
    @FXML private Label lblSumKelas;
    @FXML private Label lblSumRute;
    @FXML private Label lblSumTanggal;
    @FXML private Label lblSumWaktu;
    @FXML private Label lblSumKursi;
    @FXML private Label lblSumHargaBase;
    @FXML private Label lblSumTotal;

    // Form
    @FXML private TextField tfNama;
    @FXML private TextField tfNik;

    // Metode bayar
    @FXML private Button btnTransfer;
    @FXML private Button btnQris;
    @FXML private Button btnTunai;
    @FXML private Label  lblMetodeDipilih;

    @FXML private Label  lblError;

    private static final DateTimeFormatter FMT_WAKTU   = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_TANGGAL = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",
            new Locale("id", "ID"));
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private final TiketDAO     tiketDAO     = new TiketDAO();
    private final KursiDAO     kursiDAO     = new KursiDAO();
    private final PembayaranDAO pembayaranDAO = new PembayaranDAO();

    private String metodePembayaranDipilih = null;
    private List<Button> metodeBtns;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = SessionManager.getInstance().getCurrentUser();
        if (lblNama != null) lblNama.setText(user.getUsername());
        if (topbarAvatar != null && topbarIcon != null) {
            AvatarManager.loadAvatar(user.getUsername(), topbarAvatar, topbarIcon, 24);
        }

        metodeBtns = List.of(btnTransfer, btnQris, btnTunai);

        Jadwal j = PenumpangSession.getJadwalDipilih();
        Kursi  k = PenumpangSession.getKursiDipilih();

        if (j == null || k == null) {
            SceneManager.switchScene("HomePenumpang.fxml");
            return;
        }

        // Isi ringkasan
        lblSumKereta.setText(j.getKereta().getNama() + " · " + j.getKereta().getNomorKereta());
        lblSumKelas.setText(j.getJenisKelas().getNamaKelas());
        lblSumRute.setText(j.getRute().getStasiunAsal() + " → " + j.getRute().getStasiunTujuan());
        lblSumTanggal.setText(j.getWaktuBerangkat().format(FMT_TANGGAL));
        lblSumWaktu.setText(j.getWaktuBerangkat().format(FMT_WAKTU) + " → " + j.getWaktuTiba().format(FMT_WAKTU));
        lblSumKursi.setText(k.getNomorKursi());

        String hargaStr = formatIDR(j.getHargaEfektif());
        lblSumHargaBase.setText(hargaStr);
        lblSumTotal.setText(hargaStr);

        // Pre-fill data dari penumpang jika tersedia
        if (SessionManager.getInstance().getCurrentUser() instanceof Penumpang p) {
            if (p.getNamaLengkap() != null && !p.getNamaLengkap().isEmpty())
                tfNama.setText(p.getNamaLengkap());
            if (p.getNik() != null && !p.getNik().isEmpty())
                tfNik.setText(p.getNik());
        }
    }

    @FXML
    private void handlePilihMetode(javafx.event.ActionEvent e) {
        Button src = (Button) e.getSource();
        metodePembayaranDipilih = (String) src.getUserData();

        // Update visual active state
        for (Button b : metodeBtns) {
            b.getStyleClass().removeAll("btn-metode-active", "btn-metode");
            b.getStyleClass().add(b == src ? "btn-metode-active" : "btn-metode");
        }
        lblMetodeDipilih.setText("Metode dipilih: " + metodePembayaranDipilih);
        lblMetodeDipilih.setStyle("-fx-text-fill:#2BB673; -fx-font-weight:bold;");
    }

    @FXML
    private void handleKonfirmasiPembayaran() {
        // Validasi
        String nama = tfNama.getText().trim();
        String nik  = tfNik.getText().trim();

        if (nama.isEmpty()) { tampilkanError("Nama lengkap tidak boleh kosong."); return; }
        if (nik.isEmpty())  { tampilkanError("NIK / Nomor Paspor tidak boleh kosong."); return; }
        if (metodePembayaranDipilih == null) {
            tampilkanError("Pilih metode pembayaran terlebih dahulu.");
            return;
        }

        sembunyikanError();

        Jadwal j = PenumpangSession.getJadwalDipilih();
        Kursi  k = PenumpangSession.getKursiDipilih();
        var    user = SessionManager.getInstance().getCurrentUser();

        if (!(user instanceof Penumpang penumpang)) {
            tampilkanError("Sesi penumpang tidak valid. Silakan login ulang.");
            return;
        }

        // 1. Buat tiket
        double harga = j.getHargaEfektif();
        Tiket tiket = new Tiket(penumpang, j, k, harga);
        tiket.setKodeTiket(TiketHelper.generateKodeTiket());
        tiket.setStatus("AKTIF");

        // 2. Tandai kursi terpesan
        k.setStatus("TERPESAN");
        kursiDAO.update(k);

        // 3. Simpan tiket
        boolean tiketSimpan = tiketDAO.save(tiket);
        if (!tiketSimpan) {
            k.setStatus("TERSEDIA");
            kursiDAO.update(k);
            tampilkanError("Gagal menyimpan tiket. Coba lagi.");
            return;
        }

        // 4. Simpan pembayaran
        Pembayaran pembayaran = new Pembayaran(tiket, harga, metodePembayaranDipilih);
        pembayaran.setStatus("LUNAS");
        pembayaranDAO.save(pembayaran);

        // 5. Reset session booking
        PenumpangSession.setJadwalDipilih(null);
        PenumpangSession.setKursiDipilih(null);

        // 6. Tampilkan sukses
        Alert sukses = new Alert(Alert.AlertType.INFORMATION);
        sukses.setTitle("Pembayaran Berhasil");
        sukses.setHeaderText("Tiket Berhasil Dipesan!");
        sukses.setContentText(
                "Kode Tiket: " + tiket.getKodeTiket() + "\n" +
                "Rute: " + j.getRute().getStasiunAsal() + " → " + j.getRute().getStasiunTujuan() + "\n" +
                "Kursi: " + k.getNomorKursi() + "\n" +
                "Total: " + formatIDR(harga) + "\n\n" +
                "Tiket Anda dapat dilihat di menu 'Tiket Saya'."
        );
        sukses.showAndWait();

        SceneManager.switchScene("TiketSaya.fxml");
    }

    private String formatIDR(double nilai) {
        return IDR.format((long) nilai).replace("Rp", "Rp ").replace(",00", "");
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

    @FXML private void handleKembali() { SceneManager.switchScene("PilihKursi.fxml"); }
}
