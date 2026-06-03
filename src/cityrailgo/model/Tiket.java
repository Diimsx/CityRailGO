package cityrailgo.model;

import java.time.LocalDateTime;

public class Tiket {
    private int id;
    private Jadwal jadwal;
    private Penumpang penumpang; 
    private Kursi kursi;
    private Promo promo;        
    private String kodeTiket;
    private double hargaAkhir;
    private String status;      
    private LocalDateTime tanggalPesan;

    public Tiket() {
        this.tanggalPesan = LocalDateTime.now();
        this.status = "AKTIF";
    }

    public Tiket(int id, Jadwal jadwal, Penumpang penumpang, Kursi kursi, Promo promo, String kodeTiket, double hargaAkhir, String status, LocalDateTime tanggalPesan) {
        this.id = id;
        this.jadwal = jadwal;
        this.penumpang = penumpang;
        this.kursi = kursi;
        this.promo = promo;
        this.kodeTiket = kodeTiket;
        this.hargaAkhir = hargaAkhir;
        this.status = status;
        this.tanggalPesan = tanggalPesan;
    }

    public String generateKodeTiket() {
        int randomNum = (int)(Math.random() * 90000) + 10000;
        this.kodeTiket = "CRG-" + randomNum;
        return this.kodeTiket;
    }

    public double hitungHargaAkhir() {
        double harga = 0.0;
        if (jadwal != null) {
            harga = jadwal.getHargaBase();
        }
        if (promo != null) {
            double diskon = promo.hitungDiskon(harga);
            harga = harga - diskon;
        }
        this.hargaAkhir = harga;
        return harga;
    }

    public void batalkan() {
        this.status = "BATAL";
    }

    public String getInfoTiket() {
        String namaPen = (penumpang != null) ? penumpang.getNama() : "Tanpa Nama";
        return "Tiket: " + kodeTiket + " | Penumpang: " + namaPen + " | Total: Rp" + hargaAkhir;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Jadwal getJadwal() { return jadwal; }
    public void setJadwal(Jadwal jadwal) { this.jadwal = jadwal; }

    public Penumpang getPenumpang() { return penumpang; }
    public void setPenumpang(Penumpang penumpang) { this.penumpang = penumpang; }

    public Kursi getKursi() { return kursi; }
    public void setKursi(Kursi kursi) { this.kursi = kursi; }

    public Promo getPromo() { return promo; }
    public void setPromo(Promo promo) { this.promo = promo; }

    public String getKodeTiket() { return kodeTiket; }
    public void setKodeTiket(String kodeTiket) { this.kodeTiket = kodeTiket; }

    public double getHargaAkhir() { return hargaAkhir; }
    public void setHargaAkhir(double hargaAkhir) { this.hargaAkhir = hargaAkhir; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTanggalPesan() { return tanggalPesan; }
    public void setTanggalPesan(LocalDateTime tanggalPesan) { this.tanggalPesan = tanggalPesan; }
}