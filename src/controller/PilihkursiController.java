package Controller;

/*
 * =====================================================================
 *  ASUMSI STRUKTUR KELAS (sesuaikan dengan project Anda bila berbeda):
 *
 *  Model.Kursi
 *      - String getIdKursi()
 *      - String getNomorKursi()      contoh: "1A", "1B", "2C", dst
 *      - String getGerbong()         contoh: "Eksekutif 1"
 *      - boolean isTerisi()
 *
 *  Model.Kereta
 *      - String getNamaKereta()
 *      - String getKelas()
 *
 *  Model.Jadwal
 *      - String getIdJadwal()
 *      - Model.Kereta getKereta()
 *      - String getAsal()
 *      - String getTujuan()
 *      - String getTanggalKeberangkatan()
 *      - String getJamKeberangkatan()
 *
 *  DAO.KursiDAO
 *      - List<Kursi> getKursiByJadwal(String idJadwal) throws SQLException
 *      - boolean updateStatusKursi(String idKursi, boolean terisi) throws SQLException
 * =====================================================================
 */

import Model.Kereta;
import Model.Jadwal;
import Model.Kursi;
import DAO.KursiDAO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class PilihKursiController {

    @FXML private Label lblNamaKereta;
    @FXML private Label lblRute;
    @FXML private Label lblTanggalJam;
    @FXML private ComboBox<String> cbGerbong;
    @FXML private GridPane gridKursi;
    @FXML private Label lblTerpilih;
    @FXML private Label lblSisaKuota;
    @FXML private Button btnKonfirmasi;
    @FXML private Button btnKembali;

    private Jadwal jadwal;
    private int jumlahPenumpang;
    private final Map<String, List<Kursi>> kursiPerGerbong = new TreeMap<>();
    private final List<Kursi> kursiTerpilih = new ArrayList<>();
    private static final int KOLOM_PER_BARIS = 4; // format 2-2 dengan lorong tengah

    /**
     * Dipanggil dari controller sebelumnya (mis. PemesananController)
     * setelah scene dimuat, sebelum scene ditampilkan.
     */
    public void initData(Jadwal jadwal, int jumlahPenumpang) {
        this.jadwal = jadwal;
        this.jumlahPenumpang = jumlahPenumpang;

        Kereta kereta = jadwal.getKereta();
        lblNamaKereta.setText(kereta.getNamaKereta() + " - " + kereta.getKelas());
        lblRute.setText(jadwal.getAsal() + " \u2192 " + jadwal.getTujuan());
        lblTanggalJam.setText(jadwal.getTanggalKeberangkatan() + " | " + jadwal.getJamKeberangkatan());

        muatDataKursi();
        updateInfoSeleksi();
        btnKonfirmasi.setDisable(true);
    }

    private void muatDataKursi() {
        kursiPerGerbong.clear();
        try {
            KursiDAO dao = new KursiDAO();
            List<Kursi> semuaKursi = dao.getKursiByJadwal(jadwal.getIdJadwal());

            for (Kursi k : semuaKursi) {
                kursiPerGerbong
                        .computeIfAbsent(k.getGerbong(), key -> new ArrayList<>())
                        .add(k);
            }
        } catch (SQLException e) {
            tampilkanAlert(Alert.AlertType.ERROR, "Gagal memuat data kursi: " + e.getMessage());
        }

        cbGerbong.getItems().setAll(kursiPerGerbong.keySet());
        if (!cbGerbong.getItems().isEmpty()) {
            cbGerbong.getSelectionModel().selectFirst();
            tampilkanKursi(cbGerbong.getValue());
        }
    }

    @FXML
    private void handlePilihGerbong(ActionEvent event) {
        String gerbong = cbGerbong.getValue();
        if (gerbong != null) {
            tampilkanKursi(gerbong);
        }
    }

    private void tampilkanKursi(String gerbong) {
        gridKursi.getChildren().clear();

        List<Kursi> daftar = kursiPerGerbong.getOrDefault(gerbong, new ArrayList<>());
        daftar.sort(Comparator.comparing(Kursi::getNomorKursi));

        int kolom = 0;
        int baris = 0;

        for (Kursi kursi : daftar) {
            Button seatBtn = buatTombolKursi(kursi);

            // beri jarak lorong setiap 2 kolom (format 2-2)
            int posisiKolom = kolom < 2 ? kolom : kolom + 1;
            gridKursi.add(seatBtn, posisiKolom, baris);

            kolom++;
            if (kolom >= KOLOM_PER_BARIS) {
                kolom = 0;
                baris++;
            }
        }
    }

    private Button buatTombolKursi(Kursi kursi) {
        Button seatBtn = new Button(kursi.getNomorKursi());
        seatBtn.getStyleClass().add("seat-btn");
        seatBtn.setPrefSize(50, 50);

        if (kursi.isTerisi()) {
            seatBtn.getStyleClass().add("seat-terisi");
            seatBtn.setDisable(true);
        } else if (kursiTerpilih.contains(kursi)) {
            seatBtn.getStyleClass().add("seat-terpilih");
        } else {
            seatBtn.getStyleClass().add("seat-kosong");
        }

        seatBtn.setOnAction(e -> toggleKursi(kursi, seatBtn));
        return seatBtn;
    }

    private void toggleKursi(Kursi kursi, Button seatBtn) {
        if (kursiTerpilih.contains(kursi)) {
            kursiTerpilih.remove(kursi);
            seatBtn.getStyleClass().remove("seat-terpilih");
            seatBtn.getStyleClass().add("seat-kosong");
        } else {
            if (kursiTerpilih.size() >= jumlahPenumpang) {
                tampilkanAlert(Alert.AlertType.WARNING,
                        "Anda hanya dapat memilih maksimal " + jumlahPenumpang + " kursi.");
                return;
            }
            kursiTerpilih.add(kursi);
            seatBtn.getStyleClass().remove("seat-kosong");
            seatBtn.getStyleClass().add("seat-terpilih");
        }
        updateInfoSeleksi();
    }

    private void updateInfoSeleksi() {
        lblTerpilih.setText("Kursi terpilih: " +
                kursiTerpilih.stream().map(Kursi::getNomorKursi)
                        .reduce((a, b) -> a + ", " + b).orElse("-"));
        int sisa = jumlahPenumpang - kursiTerpilih.size();
        lblSisaKuota.setText("Sisa kuota: " + Math.max(sisa, 0) + " kursi");
        btnKonfirmasi.setDisable(kursiTerpilih.size() != jumlahPenumpang);
    }

    @FXML
    private void handleKonfirmasi(ActionEvent event) {
        if (kursiTerpilih.size() != jumlahPenumpang) {
            tampilkanAlert(Alert.AlertType.WARNING,
                    "Pilih tepat " + jumlahPenumpang + " kursi sebelum melanjutkan.");
            return;
        }

        try {
            // TODO: sesuaikan path FXML & nama controller halaman selanjutnya
            // (mis. halaman konfirmasi/pembayaran pemesanan)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Pembayaran.fxml"));
            Parent root = loader.load();

            // Contoh meneruskan data ke controller berikutnya:
            // PembayaranController next = loader.getController();
            // next.initData(jadwal, jumlahPenumpang, kursiTerpilih);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            tampilkanAlert(Alert.AlertType.ERROR, "Gagal membuka halaman selanjutnya: " + e.getMessage());
        }
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        try {
            // TODO: sesuaikan path FXML halaman sebelumnya (mis. pencarian jadwal)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Jadwal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            tampilkanAlert(Alert.AlertType.ERROR, "Gagal kembali: " + e.getMessage());
        }
    }

    private void tampilkanAlert(Alert.AlertType type, String pesan) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }
}