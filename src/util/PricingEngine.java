package util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 * Mesin kalkulasi harga dinamis CityRailGO.
 *
 * Urutan penerapan multiplier:
 *   basePrice = hargaPerKm × jarakKm
 *   finalPrice = basePrice × (1 + totalAdjustment)
 *
 * Setiap aturan berkontribusi sebagai persentase adjustment (+/-).
 * Semua aturan bersifat additif (tidak bertingkat/compound).
 */
public class PricingEngine {

    // =========================================================
    // DATA KLASS ATURAN HARGA
    // =========================================================

    public static class AturanHarga {
        public final String nama;
        public final double persentase; // positif = surcharge, negatif = diskon
        public final String emoji;

        public AturanHarga(String emoji, String nama, double persentase) {
            this.emoji      = emoji;
            this.nama       = nama;
            this.persentase = persentase;
        }

        /** Formatted display: "+20% Weekend" atau "-10% Early Bird" */
        public String getLabel() {
            String sign = persentase >= 0 ? "+" : "";
            return emoji + " " + sign + (int)(persentase * 100) + "% " + nama;
        }
    }

    public static class HasilPerhitungan {
        public final double hargaDasar;
        public final double hargaFinal;
        public final double totalMultiplier;   // total adjustment, misal 0.30 = +30%
        public final List<AturanHarga> aturanAktif;

        public HasilPerhitungan(double hargaDasar, double hargaFinal,
                                double totalMultiplier, List<AturanHarga> aturanAktif) {
            this.hargaDasar      = hargaDasar;
            this.hargaFinal      = hargaFinal;
            this.totalMultiplier = totalMultiplier;
            this.aturanAktif     = aturanAktif;
        }

        /** Apakah ada adjustment (bukan harga standar) */
        public boolean adaAdjustment() {
            return !aturanAktif.isEmpty();
        }

        /** String ringkas semua aturan aktif, pisah newline */
        public String getRingkasanAturan() {
            if (aturanAktif.isEmpty()) return "Harga standar";
            StringBuilder sb = new StringBuilder();
            for (AturanHarga a : aturanAktif) {
                if (sb.length() > 0) sb.append("  |  ");
                sb.append(a.getLabel());
            }
            return sb.toString();
        }
    }

    // =========================================================
    // MAIN CALCULATION
    // =========================================================

    /**
     * Hitung harga final berdasarkan base price dan waktu berangkat.
     *
     * @param hargaPerKm   tarif per km dari JenisKelas
     * @param jarakKm      jarak rute
     * @param waktuBerangkat waktu keberangkatan jadwal
     * @return HasilPerhitungan berisi harga dasar, harga final, dan daftar aturan aktif
     */
    public static HasilPerhitungan hitung(double hargaPerKm, double jarakKm,
                                          LocalDateTime waktuBerangkat) {
        double hargaDasar = hargaPerKm * jarakKm;
        List<AturanHarga> aturanAktif = new ArrayList<>();

        // --- Evaluasi semua aturan ---
        cekPeakSeason(waktuBerangkat, aturanAktif);
        cekWeekend(waktuBerangkat, aturanAktif);
        cekEarlyBird(waktuBerangkat, aturanAktif);
        cekNightSurcharge(waktuBerangkat, aturanAktif);

        // --- Hitung total adjustment (additif) ---
        double totalAdj = aturanAktif.stream()
            .mapToDouble(a -> a.persentase)
            .sum();

        double hargaFinal = hargaDasar * (1.0 + totalAdj);
        // Floor ke ratusan terdekat (UX: harga tidak ada desimalnya)
        hargaFinal = Math.floor(hargaFinal / 100.0) * 100.0;

        return new HasilPerhitungan(hargaDasar, hargaFinal, totalAdj, aturanAktif);
    }

    // =========================================================
    // ATURAN-ATURAN HARGA
    // =========================================================

    /**
     * Peak Season: Desember–Januari & Juni–Juli
     * Surcharge +30% — musim liburan sekolah & akhir tahun.
     */
    private static void cekPeakSeason(LocalDateTime dt, List<AturanHarga> hasil) {
        Month m = dt.getMonth();
        boolean isPeak = (m == Month.DECEMBER || m == Month.JANUARY
                       || m == Month.JUNE     || m == Month.JULY);
        if (isPeak) {
            hasil.add(new AturanHarga("🔥", "Peak Season", 0.30));
        }
    }

    /**
     * Weekend Surcharge: Sabtu & Minggu
     * Surcharge +20% — permintaan tinggi saat akhir pekan.
     */
    private static void cekWeekend(LocalDateTime dt, List<AturanHarga> hasil) {
        DayOfWeek day = dt.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            hasil.add(new AturanHarga("📅", "Akhir Pekan", 0.20));
        }
    }

    /**
     * Early Bird Discount: keberangkatan sebelum 07:00
     * Diskon -10% — reward penumpang yang memilih jam sepi.
     */
    private static void cekEarlyBird(LocalDateTime dt, List<AturanHarga> hasil) {
        int jam = dt.getHour();
        if (jam < 7) {
            hasil.add(new AturanHarga("🌅", "Early Bird", -0.10));
        }
    }

    /**
     * Night Surcharge: keberangkatan 22:00 – 04:59
     * Surcharge +15% — kereta malam memerlukan biaya operasional lebih.
     */
    private static void cekNightSurcharge(LocalDateTime dt, List<AturanHarga> hasil) {
        int jam = dt.getHour();
        if (jam >= 22 || jam < 5) {
            hasil.add(new AturanHarga("🌙", "Kereta Malam", 0.15));
        }
    }

    // =========================================================
    // FORMAT HELPER
    // =========================================================

    public static String formatRupiah(double harga) {
        return "Rp " + String.format("%,.0f", harga).replace(",", ".");
    }
}
