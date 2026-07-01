package util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class PricingEngine {

    public static class AturanHarga {
        public final String nama;
        public final double persentase;
        public final String emoji;

        public AturanHarga(String emoji, String nama, double persentase) {
            this.emoji      = emoji;
            this.nama       = nama;
            this.persentase = persentase;
        }

        public String getLabel() {
            String sign = persentase >= 0 ? "+" : "";
            return emoji + " " + sign + (int)(persentase * 100) + "% " + nama;
        }
    }

    public static class HasilPerhitungan {
        public final double hargaDasar;
        public final double hargaFinal;
        public final double totalMultiplier;
        public final List<AturanHarga> aturanAktif;

        public HasilPerhitungan(double hargaDasar, double hargaFinal,
                                double totalMultiplier, List<AturanHarga> aturanAktif) {
            this.hargaDasar      = hargaDasar;
            this.hargaFinal      = hargaFinal;
            this.totalMultiplier = totalMultiplier;
            this.aturanAktif     = aturanAktif;
        }

        public boolean adaAdjustment() {
            return !aturanAktif.isEmpty();
        }

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

    public static HasilPerhitungan hitung(double hargaPerKm, double jarakKm,
                                          LocalDateTime waktuBerangkat) {
        double hargaDasar = hargaPerKm * jarakKm;
        List<AturanHarga> aturanAktif = new ArrayList<>();

        cekPeakSeason(waktuBerangkat, aturanAktif);
        cekWeekend(waktuBerangkat, aturanAktif);
        cekEarlyBird(waktuBerangkat, aturanAktif);
        cekNightSurcharge(waktuBerangkat, aturanAktif);

        double totalAdj = aturanAktif.stream()
            .mapToDouble(a -> a.persentase)
            .sum();

        double hargaFinal = hargaDasar * (1.0 + totalAdj);
        hargaFinal = Math.floor(hargaFinal / 100.0) * 100.0;

        return new HasilPerhitungan(hargaDasar, hargaFinal, totalAdj, aturanAktif);
    }

    private static void cekPeakSeason(LocalDateTime dt, List<AturanHarga> hasil) {
        Month m = dt.getMonth();
        boolean isPeak = (m == Month.DECEMBER || m == Month.JANUARY
                       || m == Month.JUNE     || m == Month.JULY);
        if (isPeak) {
            hasil.add(new AturanHarga("🔥", "Peak Season", 0.30));
        }
    }

    private static void cekWeekend(LocalDateTime dt, List<AturanHarga> hasil) {
        DayOfWeek day = dt.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            hasil.add(new AturanHarga("📅", "Akhir Pekan", 0.20));
        }
    }

    private static void cekEarlyBird(LocalDateTime dt, List<AturanHarga> hasil) {
        int jam = dt.getHour();
        if (jam < 7) {
            hasil.add(new AturanHarga("🌅", "Early Bird", -0.10));
        }
    }

    private static void cekNightSurcharge(LocalDateTime dt, List<AturanHarga> hasil) {
        int jam = dt.getHour();
        if (jam >= 22 || jam < 5) {
            hasil.add(new AturanHarga("🌙", "Kereta Malam", 0.15));
        }
    }

    public static String formatRupiah(double harga) {
        return "Rp " + String.format("%,.0f", harga).replace(",", ".");
    }
}
