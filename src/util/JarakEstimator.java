package util;

import model.Stasiun;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JarakEstimator {
    private static final double KECEPATAN_KMH = 80.0;
    private static final int MENIT_BERHENTI_PER_TRANSIT = 10;
    private static final double JARAK_MINIMUM = 10.0;

    public record Hasil(double jarakKm, int estimasiMenit) {
        public String formatJarak()    { return String.format("%.0f km", jarakKm); }
        public String formatEstimasi() {
            int jam  = estimasiMenit / 60;
            int sisa = estimasiMenit % 60;
            return jam <= 0 ? sisa + " menit" : jam + " jam " + sisa + " menit";
        }
    }

    private static final Map<String, Double> TABEL_JARAK = new HashMap<>();

    static {
        // Jalur Jakarta (GMR/PSE)
        put("GMR", "BD",  173);
        put("GMR", "CN",  246);
        put("GMR", "PWT", 391);
        put("GMR", "KY",  476);
        put("GMR", "YK",  513);
        put("GMR", "SLO", 565);
        put("GMR", "SMT", 447);
        put("GMR", "SGU", 784);
        put("GMR", "SBI", 726);
        put("GMR", "MLG", 843);
        put("GMR", "BJR", 1003);
        put("GMR", "MKS", 639);

        put("PSE", "BD",  182);
        put("PSE", "CN",  251);
        put("PSE", "PWT", 397);
        put("PSE", "KY",  481);
        put("PSE", "YK",  519);
        put("PSE", "SLO", 571);
        put("PSE", "SMT", 452);
        put("PSE", "SGU", 826);
        put("PSE", "SBI", 768);
        put("PSE", "MLG", 848);
        put("PSE", "BJR", 1008);

        // Jalur Barat–Tengah
        put("BD",  "CN",  130);
        put("BD",  "PWT", 237);
        put("BD",  "YK",  356);
        put("CN",  "PWT", 145);
        put("CN",  "KY",  228);
        put("CN",  "SMT", 201);
        put("CN",  "YK",  267);
        put("PWT", "KY",   85);
        put("PWT", "YK",  121);
        put("KY",  "YK",   37);

        // Jalur Tengah–Timur
        put("YK",  "SLO",  60);
        put("YK",  "SGU", 311);
        put("YK",  "SBI", 309);
        put("YK",  "MLG", 371);
        put("YK",  "MKS", 137);
        put("SLO", "SGU", 257);
        put("SLO", "SBI", 255);
        put("SLO", "MLG", 311);
        put("SLO", "MKS",  85);
        put("SLO", "SMT", 117);
        put("SMT", "SBI", 398);
        put("SMT", "SGU", 352);
        put("SMT", "MKS", 230);
        put("MKS", "SGU", 160);
        put("MKS", "SBI", 158);

        // Jalur Surabaya–Timur
        put("SGU", "SBI",   8);
        put("SGU", "MLG",  89);
        put("SGU", "BJR", 274);
        put("SBI", "MLG",  93);
        put("SBI", "BJR", 272);
        put("MLG", "BJR", 196);
    }

    /** Helper: tambah ke tabel dua arah */
    private static void put(String a, String b, double km) {
        String key = kunci(a, b);
        TABEL_JARAK.put(key, km);
    }

    /** Key selalu urut alfabet agar simetris */
    private static String kunci(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    /**
     * Hitung total jarak dan estimasi waktu untuk rute:
     *   asal → [stops...] → tujuan
     *
     * @param asal    Stasiun keberangkatan
     * @param stops   Daftar stasiun transit (boleh kosong)
     * @param tujuan  Stasiun tujuan akhir
     * @return Hasil (jarakKm, estimasiMenit), atau null jika input tidak valid
     */
    public static Hasil hitung(Stasiun asal, List<Stasiun> stops, Stasiun tujuan) {
        if (asal == null || tujuan == null) return null;

        // Susun urutan lengkap: asal + stops + tujuan
        List<Stasiun> urutan = new java.util.ArrayList<>();
        urutan.add(asal);
        if (stops != null) urutan.addAll(stops);
        urutan.add(tujuan);

        double totalKm = 0;
        boolean adaDataTidakDiketahui = false;

        for (int i = 0; i < urutan.size() - 1; i++) {
            String kodeA = urutan.get(i).getKodeStasiun();
            String kodeB = urutan.get(i + 1).getKodeStasiun();
            Double segmen = TABEL_JARAK.get(kunci(kodeA, kodeB));
            if (segmen != null) {
                totalKm += segmen;
            } else {
                // Fallback: tidak ada data pasangan ini
                adaDataTidakDiketahui = true;
                totalKm += JARAK_MINIMUM;
            }
        }

        // Estimasi menit: (jarak / kecepatan) × 60  +  10 menit per transit
        int transitCount = (stops == null) ? 0 : stops.size();
        int estimasiMenit = (int) Math.round((totalKm / KECEPATAN_KMH) * 60)
                           + (transitCount * MENIT_BERHENTI_PER_TRANSIT);

        // Bulatkan jarak ke 1 desimal
        double jarakFinal = Math.round(totalKm * 10.0) / 10.0;
        return new Hasil(jarakFinal, estimasiMenit);
    }

    /**
     * Kembalikan true jika pasangan stasiun ada di tabel jarak.
     */
    public static boolean adaData(Stasiun a, Stasiun b) {
        if (a == null || b == null) return false;
        return TABEL_JARAK.containsKey(kunci(a.getKodeStasiun(), b.getKodeStasiun()));
    }
}
