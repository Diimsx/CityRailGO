package controller;

import dao.KursiDAO;
import dao.PembayaranDAO;
import dao.TiketDAO;
import dao.PromoDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;
import model.Jadwal;
import model.Kursi;
import model.Pembayaran;
import model.Penumpang;
import model.Promo;
import model.Tiket;
import util.AvatarManager;
import util.PenumpangSession;
import util.SceneManager;
import util.SessionManager;
import util.TiketHelper;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PembayaranController implements Initializable {

    @FXML private Label lblNama;
    @FXML private FontIcon topbarIcon;
    @FXML private ImageView topbarAvatar;

    @FXML private Label lblSumKereta;
    @FXML private Label lblSumKelas;
    @FXML private Label lblSumRute;
    @FXML private Label lblSumTanggal;
    @FXML private Label lblSumWaktu;
    @FXML private Label lblSumKursi;
    @FXML private Label lblSumHargaBase;
    @FXML private Label lblSumTotal;
    @FXML private VBox vboxBreakdownHarga;

    @FXML private TextField tfPromo;
    @FXML private Label lblPromoStatus;

    @FXML private VBox vboxManifes;

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
    private final PromoDAO     promoDAO     = new PromoDAO();

    private String metodePembayaranDipilih = null;
    private List<Button> metodeBtns;

    private int N = 1;
    private double totalHargaOriginal = 0.0;
    private double totalHargaAkhir = 0.0;
    private Promo promoDipilih = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = SessionManager.getInstance().getCurrentUser();
        if (lblNama != null) lblNama.setText(user.getUsername());
        if (topbarAvatar != null && topbarIcon != null) {
            AvatarManager.loadAvatar(user.getUsername(), topbarAvatar, topbarIcon, 24);
        }

        metodeBtns = List.of(btnTransfer, btnQris, btnTunai);

        Jadwal j = PenumpangSession.getJadwalDipilih();
        List<Kursi> listKursi = PenumpangSession.getListKursiDipilih();

        if (j == null) {
            SceneManager.switchScene("HomePenumpang.fxml");
            return;
        }

        N = PenumpangSession.getJumlahPenumpang();

        lblSumKereta.setText(j.getKereta().getNama() + " · " + j.getKereta().getNomorKereta());
        lblSumKelas.setText(j.getJenisKelas().getNamaKelas());
        lblSumRute.setText(j.getRute().getStasiunAsal() + " → " + j.getRute().getStasiunTujuan());
        lblSumTanggal.setText(j.getWaktuBerangkat().format(FMT_TANGGAL));
        lblSumWaktu.setText(j.getWaktuBerangkat().format(FMT_WAKTU) + " → " + j.getWaktuTiba().format(FMT_WAKTU));
        
        List<String> labels = new ArrayList<>();
        for (Kursi k : listKursi) {
            if (k != null) {
                labels.add(k.getNomorKursi());
            }
        }
        lblSumKursi.setText(labels.isEmpty() ? "Tidak ada kursi" : String.join(", ", labels));

        double hargaBase = j.getHargaEfektif();
        int dewasa = PenumpangSession.getJumlahDewasa();
        int bayi = PenumpangSession.getJumlahBayi();

        totalHargaOriginal = (hargaBase * dewasa) + (hargaBase * 0.10 * bayi);
        totalHargaAkhir = totalHargaOriginal;
        
        perbaruiBreakdownHarga();

        generateManifesForm();
    }

    private void generateManifesForm() {
        vboxManifes.getChildren().clear();

        Label title = new Label("Data Penumpang");
        title.getStyleClass().add("card-title");
        vboxManifes.getChildren().add(title);
        
        VBox div = new VBox();
        div.getStyleClass().add("divider-h");
        vboxManifes.getChildren().add(div);

        List<String[]> manifes = PenumpangSession.getManifesPenumpang();
        List<Kursi> listKursi = PenumpangSession.getListKursiDipilih();

        if (manifes == null) return;

        int idxKursi = 0;
        for (int i = 0; i < manifes.size(); i++) {
            String[] p = manifes.get(i);
            String nama = p[0];
            String nik = p[1];
            String usia = p[2];
            String tipe = p[3];

            VBox block = new VBox(8);
            block.setStyle("-fx-background-color: #FAFBFD; -fx-padding: 14; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-border-width: 1;");
            
            String labelKursi = "";
            if ("DEWASA".equals(tipe)) {
                if (idxKursi < listKursi.size()) {
                    labelKursi = " (Kursi: " + listKursi.get(idxKursi++).getNomorKursi() + ")";
                }
            } else {
                labelKursi = " (Dipangku / Tanpa Kursi)";
            }

            Label lblHeader = new Label("Penumpang #" + (i + 1) + " - " + ("DEWASA".equals(tipe) ? "Dewasa" : "Bayi") + labelKursi);
            lblHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1B3A6B;");
            
            VBox detailBox = new VBox(4);
            Label lblNamaVal = new Label("Nama: " + nama);
            lblNamaVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #0A192F; -fx-font-weight: bold;");
            Label lblNikVal = new Label("Identitas/NIK: " + nik);
            lblNikVal.setStyle("-fx-font-size: 12px; -fx-text-fill: #6E84A8;");
            Label lblUsiaVal = new Label("Usia: " + usia + " Tahun");
            lblUsiaVal.setStyle("-fx-font-size: 12px; -fx-text-fill: #6E84A8;");
            
            detailBox.getChildren().addAll(lblNamaVal, lblNikVal, lblUsiaVal);
            block.getChildren().addAll(lblHeader, detailBox);
            vboxManifes.getChildren().add(block);
        }
    }

    @FXML
    private void handleApplyPromo() {
        String code = tfPromo.getText().trim();
        if (code.isEmpty()) {
            promoDipilih = null;
            totalHargaAkhir = totalHargaOriginal;
            perbaruiBreakdownHarga();
            lblPromoStatus.setText("");
            lblPromoStatus.setVisible(false);
            lblPromoStatus.setManaged(false);
            return;
        }

        Promo promo = promoDAO.findByKode(code);
        if (promo != null && promo.isAktif() && !LocalDate.now().isBefore(promo.getTanggalMulai()) && !LocalDate.now().isAfter(promo.getTanggalBerakhir())) {
            promoDipilih = promo;
            double diskon = totalHargaOriginal * (promo.getDiskonPersen() / 100.0);
            totalHargaAkhir = totalHargaOriginal - diskon;
            
            perbaruiBreakdownHarga();
            lblPromoStatus.setText("✓ Promo digunakan: Potongan " + (int) promo.getDiskonPersen() + "% (Diskon: " + formatIDR(diskon) + ")");
            lblPromoStatus.setStyle("-fx-text-fill: #2BB673; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblPromoStatus.setVisible(true);
            lblPromoStatus.setManaged(true);
        } else {
            promoDipilih = null;
            totalHargaAkhir = totalHargaOriginal;
            perbaruiBreakdownHarga();
            lblPromoStatus.setText("❌ Kode promo tidak valid atau kedaluwarsa");
            lblPromoStatus.setStyle("-fx-text-fill: #D64545; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblPromoStatus.setVisible(true);
            lblPromoStatus.setManaged(true);
        }
    }

    @FXML
    private void handlePilihMetode(javafx.event.ActionEvent event) {
        Button src = (Button) event.getSource();
        metodePembayaranDipilih = (String) src.getUserData();

        for (Button b : metodeBtns) {
            b.getStyleClass().removeAll("btn-metode-active", "btn-metode");
            b.getStyleClass().add(b == src ? "btn-metode-active" : "btn-metode");
        }

        lblMetodeDipilih.setText("Metode dipilih: " + metodePembayaranDipilih);
        lblMetodeDipilih.setStyle("-fx-text-fill:#2BB673; -fx-font-weight:bold;");
    }

    @FXML
    private void handleKonfirmasiPembayaran() {
        if (metodePembayaranDipilih == null) {
            tampilkanError("Pilih metode pembayaran terlebih dahulu.");
            return;
        }

        sembunyikanError();

        Jadwal j = PenumpangSession.getJadwalDipilih();
        List<Kursi> listKursi = PenumpangSession.getListKursiDipilih();
        List<String[]> manifes = PenumpangSession.getManifesPenumpang();
        var user = SessionManager.getInstance().getCurrentUser();

        if (!(user instanceof Penumpang penumpang)) {
            tampilkanError("Sesi penumpang tidak valid. Silakan login ulang.");
            return;
        }
        if (manifes == null || manifes.isEmpty()) {
            tampilkanError("Data manifest penumpang tidak ditemukan.");
            return;
        }

        double diskonPersen = (promoDipilih != null) ? promoDipilih.getDiskonPersen() : 0.0;
        double hargaBase = j.getHargaEfektif();

        List<String> listKodeTiket = new ArrayList<>();
        int idxKursi = 0;

        for (int i = 0; i < manifes.size(); i++) {
            String[] p = manifes.get(i);
            String nama = p[0];
            String nik = p[1];
            int usia = Integer.parseInt(p[2]);
            String tipe = p[3];

            double hargaOriginal = "DEWASA".equals(tipe) ? hargaBase : (hargaBase * 0.10);
            double hargaAkhir = hargaOriginal * (1.0 - diskonPersen / 100.0);

            Kursi k = null;
            if ("DEWASA".equals(tipe) && idxKursi < listKursi.size()) {
                k = listKursi.get(idxKursi++);
            }

            Tiket tiket = new Tiket(penumpang, j, k, hargaAkhir);
            tiket.setKodeTiket(TiketHelper.generateKodeTiket());
            tiket.setNamaPenumpang(nama);
            tiket.setNikPenumpang(nik);
            tiket.setUsiaPenumpang(usia);
            tiket.setStatus("AKTIF");

            if (k != null) {
                k.setStatus("TERPESAN");
                kursiDAO.update(k);
            }

            boolean tiketSimpan = tiketDAO.save(tiket);
            if (!tiketSimpan) {
                if (k != null) {
                    k.setStatus("TERSEDIA");
                    kursiDAO.update(k);
                }
                tampilkanError("Gagal menyimpan tiket Penumpang #" + (i + 1) + ". Coba lagi.");
                return;
            }

            listKodeTiket.add(tiket.getKodeTiket());

            Pembayaran pembayaran = new Pembayaran(tiket, hargaAkhir, metodePembayaranDipilih);
            pembayaran.setStatus("LUNAS");
            if (promoDipilih != null) {
                pembayaran.setPromo(promoDipilih);
            }
            pembayaranDAO.save(pembayaran);
        }

        PenumpangSession.reset();

        Alert sukses = new Alert(Alert.AlertType.INFORMATION);
        sukses.setTitle("Pembayaran Berhasil");
        sukses.setHeaderText("Tiket Berhasil Dipesan!");
        
        String listKursiStr = listKursi.stream()
                .filter(k -> k != null)
                .map(Kursi::getNomorKursi)
                .collect(Collectors.joining(", "));
        
        sukses.setContentText(
                "Jumlah Penumpang: " + N + "\n" +
                "Kode Tiket: " + String.join(", ", listKodeTiket) + "\n" +
                "Rute: " + j.getRute().getStasiunAsal() + " → " + j.getRute().getStasiunTujuan() + "\n" +
                "Kursi: " + (listKursiStr.isEmpty() ? "(Dipangku)" : listKursiStr) + "\n" +
                "Total Bayar: " + formatIDR(totalHargaAkhir) + "\n\n" +
                "Tiket Anda dapat dilihat di menu 'Tiket Saya'."
        );
        sukses.showAndWait();

        SceneManager.switchScene("TiketSaya.fxml");
    }

    private void perbaruiBreakdownHarga() {
        vboxBreakdownHarga.getChildren().clear();

        Jadwal j = PenumpangSession.getJadwalDipilih();
        if (j == null) return;

        double hargaBase = j.getHargaEfektif();
        int dewasa = PenumpangSession.getJumlahDewasa();
        int bayi = PenumpangSession.getJumlahBayi();

        for (int i = 0; i < dewasa; i++) {
            HBox row = new HBox();
            Label lbl = new Label("Dewasa #" + (i + 1));
            lbl.getStyleClass().add("detail-label");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label val = new Label(formatIDR(hargaBase));
            val.getStyleClass().add("detail-value");
            row.getChildren().addAll(lbl, spacer, val);
            vboxBreakdownHarga.getChildren().add(row);
        }

        for (int i = 0; i < bayi; i++) {
            HBox row = new HBox();
            Label lbl = new Label("Bayi #" + (i + 1) + " (Tarif 10%)");
            lbl.getStyleClass().add("detail-label");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label val = new Label(formatIDR(hargaBase * 0.10));
            val.getStyleClass().add("detail-value");
            row.getChildren().addAll(lbl, spacer, val);
            vboxBreakdownHarga.getChildren().add(row);
        }

        Region div1 = new Region();
        div1.setStyle("-fx-background-color:#E2E8F0; -fx-min-height:1; -fx-max-height:1; -fx-margin-top:4; -fx-margin-bottom:4;");
        vboxBreakdownHarga.getChildren().add(div1);

        HBox subtotalRow = new HBox();
        Label lblSub = new Label("Subtotal");
        lblSub.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B3A6B; -fx-font-size: 12px;");
        Region spacerSub = new Region();
        HBox.setHgrow(spacerSub, Priority.ALWAYS);
        Label valSub = new Label(formatIDR(totalHargaOriginal));
        valSub.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B3A6B; -fx-font-size: 12px;");
        subtotalRow.getChildren().addAll(lblSub, spacerSub, valSub);
        vboxBreakdownHarga.getChildren().add(subtotalRow);

        if (promoDipilih != null) {
            double diskon = totalHargaOriginal * (promoDipilih.getDiskonPersen() / 100.0);
            HBox promoRow = new HBox();
            Label lblPromo = new Label("Potongan Promo (" + promoDipilih.getKodePromo() + " -" + (int)promoDipilih.getDiskonPersen() + "%)");
            lblPromo.setStyle("-fx-text-fill: #2BB673; -fx-font-size: 12px; -fx-font-weight: bold;");
            Region spacerPromo = new Region();
            HBox.setHgrow(spacerPromo, Priority.ALWAYS);
            Label valPromo = new Label("-" + formatIDR(diskon));
            valPromo.setStyle("-fx-text-fill: #2BB673; -fx-font-size: 12px; -fx-font-weight: bold;");
            promoRow.getChildren().addAll(lblPromo, spacerPromo, valPromo);
            vboxBreakdownHarga.getChildren().add(promoRow);
        }

        HBox adminRow = new HBox();
        Label lblAdmin = new Label("Biaya Layanan");
        lblAdmin.getStyleClass().add("detail-label");
        Region spacerAdmin = new Region();
        HBox.setHgrow(spacerAdmin, Priority.ALWAYS);
        Label valAdmin = new Label("Rp 0");
        valAdmin.getStyleClass().add("detail-value");
        adminRow.getChildren().addAll(lblAdmin, spacerAdmin, valAdmin);
        vboxBreakdownHarga.getChildren().add(adminRow);

        Region div2 = new Region();
        div2.setStyle("-fx-background-color:#E2E8F0; -fx-min-height:1; -fx-max-height:1; -fx-margin-top:4; -fx-margin-bottom:4;");
        vboxBreakdownHarga.getChildren().add(div2);

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTot = new Label("Total Pembayaran");
        lblTot.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1B3A6B;");
        Region spacerTot = new Region();
        HBox.setHgrow(spacerTot, Priority.ALWAYS);
        Label valTot = new Label(formatIDR(totalHargaAkhir));
        valTot.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2BB673;");
        totalRow.getChildren().addAll(lblTot, spacerTot, valTot);
        vboxBreakdownHarga.getChildren().add(totalRow);
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