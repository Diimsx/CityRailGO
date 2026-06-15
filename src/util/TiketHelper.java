package util;

import model.Jadwal;
import model.Kursi;
import model.Penumpang;
import model.Tiket;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

public class TiketHelper {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    public static String generateKodeTiket() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        int acak = RANDOM.nextInt(1000);
        return "TKT" + timestamp + String.format("%03d", acak);
    }

    public static String formatHarga(double harga) {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols(new Locale("in", "ID"));
        simbol.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", simbol);
        return "Rp" + formatter.format(harga);
    }

    public static String cetakTiket(Tiket tiket) {
        Jadwal jadwal = tiket.getJadwal();
        Penumpang penumpang = tiket.getPenumpang();
        Kursi kursi = tiket.getKursi();

        StringBuilder sb = new StringBuilder();
        sb.append("===== E-TIKET CITYRAILGO =====\n");
        sb.append("Kode Tiket  : ").append(tiket.getKodeTiket()).append("\n");
        sb.append("Nama        : ").append(penumpang.getNamaLengkap()).append("\n");
        sb.append("NIK         : ").append(penumpang.getNik()).append("\n");
        sb.append("Kereta      : ").append(jadwal.getKereta().getNama())
                .append(" (").append(jadwal.getKereta().getNomorKereta()).append(")\n");
        sb.append("Rute        : ").append(jadwal.getRute().getStasiunAsal())
                .append(" -> ").append(jadwal.getRute().getStasiunTujuan()).append("\n");
        sb.append("Berangkat   : ").append(DateTimeUtil.formatWaktu(jadwal.getWaktuBerangkat())).append("\n");
        sb.append("Tiba        : ").append(DateTimeUtil.formatWaktu(jadwal.getWaktuTiba())).append("\n");
        sb.append("Kelas       : ").append(jadwal.getJenisKelas().getNamaKelas()).append("\n");
        sb.append("Kursi       : ").append(kursi.getNomorKursi()).append("\n");
        sb.append("Total Bayar : ").append(formatHarga(tiket.getHargaTotal())).append("\n");
        sb.append("Status      : ").append(tiket.getStatus()).append("\n");
        sb.append("===============================");

        return sb.toString();
    }
}