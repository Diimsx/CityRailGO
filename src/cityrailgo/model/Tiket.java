package cityrailgo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Tiket {
    private int id;
    private Jadwal jadwal;
    private Penumpang penumpang;
    private Kursi kursi;
    private Promo promo;
    private String kodeTiket;
    private double hargaAkhir;
    private StatusTiket status;
    private LocalDateTime tanggalPesan;

    public Tiket() {
        this.status       = StatusTiket.DIPESAN;
        this.tanggalPesan = LocalDateTime.now();
    }

    public Tiket(int id, Jadwal jadwal, Penumpang penumpang, Kursi kursi,
                 Promo promo, String kodeTiket, double hargaAkhir,
                 StatusTiket status, LocalDateTime tanggalPesan) {
        this.id           = id;
        this.jadwal       = jadwal;
        this.penumpang    = penumpang;
        this.kursi        = kursi;
        this.promo        = promo;
        this.kodeTiket    = kodeTiket;
        this.hargaAkhir   = hargaAkhir;
        this.status       = status;
        this.tanggalPesan = tanggalPesan;
    }

    public String generateKodeTiket() {
        String tanggal  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String unikPart = UUID.randomUUID().toString()
                              .replace("-", "")
                              .substring(0, 4)
                              .toUpperCase();
        return "TKT-" + tanggal + "-" + unikPart;
    }

    public double hitungHargaAkhir() {
        if (jadwal == null || kursi == null) {
            return 0.0;
        }

        double hargaBase       = jadwal.getHargaBase();
        double multiplierKelas = kursi.getJenisKelas() != null
                                 ? kursi.getJenisKelas().getMultiplier()
                                 : 1.0;
        double diskon          = 0.0;

        if (promo != null && promo.isAktif()) {
            diskon = promo.getDiskon();
        }

        double hasil = hargaBase * multiplierKelas * (1 - diskon);
        return Math.round(hasil * 100.0) / 100.0;
    }

    public void batalkan() {
        if (!status.isBisaDibatalkan()) {
            System.out.println("[Tiket] Tidak dapat dibatalkan. Status saat ini: " + status.getLabel());
            return;
        }
        this.status = StatusTiket.DIBATALKAN;
        if (kursi != null) {
            kursi.setTersedia(true);
        }
        System.out.println("[Tiket] Tiket " + kodeTiket + " berhasil dibatalkan.");
    }

    public String getInfoTiket() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return "==============================\n" +
               "Kode Tiket  : " + kodeTiket + "\n" +
               "Penumpang   : " + (penumpang != null ? penumpang.getNama() : "-") + "\n" +
               "Jadwal      : " + (jadwal    != null ? jadwal.getInfoJadwal() : "-") + "\n" +
               "Kursi       : " + (kursi     != null ? kursi.getNomorKursi() : "-") + "\n" +
               "Kelas       : " + (kursi     != null && kursi.getJenisKelas() != null
                                   ? kursi.getJenisKelas().getNamaKelas() : "-") + "\n" +
               "Promo       : " + (promo     != null ? promo.getKodePromo() : "Tidak ada") + "\n" +
               "Harga Akhir : Rp " + String.format("%,.0f", hargaAkhir) + "\n" +
               "Status      : " + status.getLabel() + "\n" +
               "Tgl Pesan   : " + (tanggalPesan != null ? tanggalPesan.format(fmt) : "-") + "\n" +
               "==============================";
    }

    public void konfirmasiBayar() {
        if (this.status == StatusTiket.DIPESAN) {
            this.status = StatusTiket.DIBAYAR;
            System.out.println("[Tiket] Tiket " + kodeTiket + " telah dikonfirmasi pembayarannya.");
        }
    }

    public void kadaluarsa() {
        if (this.status == StatusTiket.DIPESAN) {
            this.status = StatusTiket.KADALUARSA;
            if (kursi != null) {
                kursi.setTersedia(true);
            }
            System.out.println("[Tiket] Tiket " + kodeTiket + " telah kadaluarsa.");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Jadwal getJadwal() {
        return jadwal;
    }

    public void setJadwal(Jadwal jadwal) {
        this.jadwal = jadwal;
    }

    public Penumpang getPenumpang() {
        return penumpang;
    }

    public void setPenumpang(Penumpang penumpang) {
        this.penumpang = penumpang;
    }

    public Kursi getKursi() {
        return kursi;
    }

    public void setKursi(Kursi kursi) {
        this.kursi = kursi;
    }

    public Promo getPromo() {
        return promo;
    }

    public void setPromo(Promo promo) {
        this.promo = promo;
    }

    public String getKodeTiket() {
        return kodeTiket;
    }

    public void setKodeTiket(String kodeTiket) {
        this.kodeTiket = kodeTiket;
    }

    public double getHargaAkhir() {
        return hargaAkhir;
    }

    public void setHargaAkhir(double hargaAkhir) {
        this.hargaAkhir = hargaAkhir;
    }

    public StatusTiket getStatus() {
        return status;
    }

    public void setStatus(StatusTiket status) {
        this.status = status;
    }

    public LocalDateTime getTanggalPesan() {
        return tanggalPesan;
    }

    public void setTanggalPesan(LocalDateTime tanggalPesan) {
        this.tanggalPesan = tanggalPesan;
    }

    @Override
    public String toString() {
        return "Tiket{" +
                "id=" + id +
                ", kodeTiket='" + kodeTiket + '\'' +
                ", hargaAkhir=" + String.format("%.2f", hargaAkhir) +
                ", status=" + status.getLabel() +
                '}';
    }
}
