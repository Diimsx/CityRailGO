package model;

import java.time.LocalDateTime;

public class Pembayaran {

    private int id;
    private static final int BATAS_PEMBATALAN_JAM = 3;
    private Tiket tiket;
    private Promo promo;
    private double jumlahBayar;
    private String metodePembayaran;
    private String status;
    private LocalDateTime tanggalBayar;

    public Pembayaran(Tiket tiket, double jumlahBayar, String metodePembayaran) {
        this.tiket = tiket;
        this.jumlahBayar = jumlahBayar;
        this.metodePembayaran = metodePembayaran;
        this.status = "PENDING";
        this.tanggalBayar = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public Tiket getTiket() {
        return tiket;
    }

    public Promo getPromo() {
        return promo;
    }

    public double getJumlahBayar() {
        return jumlahBayar;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTanggalBayar() {
        return tanggalBayar;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public void setPromo(Promo promo) {
        this.promo = promo;
    }

    public void setTanggalBayar(LocalDateTime tanggalBayar) {
        this.tanggalBayar = tanggalBayar;
    }
    
    public boolean isLewatBatasPembatalan() {
        LocalDateTime batasPembatalan = tiket.getJadwal().getWaktuBerangkat().minusHours(BATAS_PEMBATALAN_JAM);
        return LocalDateTime.now().isAfter(batasPembatalan);
    }

    public boolean isSudahDibatalkan() {
        return status.equalsIgnoreCase("DIBATALKAN");
    }

    public boolean batalkan() {
        if (isSudahDibatalkan() || isLewatBatasPembatalan()) {
            return false;
        }
        this.status = "DIBATALKAN";
        tiket.setStatus("DIBATALKAN");
        tiket.getKursi().setStatus("TERSEDIA");
        return true;
    }
}
