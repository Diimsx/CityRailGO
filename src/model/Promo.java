package model;

import java.time.LocalDate;

public class Promo {

    private int id;
    private String kodePromo;
    private String deskripsi;
    private double diskonPersen;
    private LocalDate tanggalMulai;
    private LocalDate tanggalBerakhir;
    private boolean aktif;

    public Promo(String kodePromo, String deskripsi, double diskonPersen, LocalDate tanggalMulai, LocalDate tanggalBerakhir) {
        this.kodePromo = kodePromo;
        this.deskripsi = deskripsi;
        this.diskonPersen = diskonPersen;
        this.tanggalMulai = tanggalMulai;
        this.tanggalBerakhir = tanggalBerakhir;
        this.aktif = true;
    }

    public int getId() {
        return id;
    }

    public String getKodePromo() {
        return kodePromo;
    }

    public double getDiskonPersen() {
        return diskonPersen;
    }
    
    public String getDeskripsi() {
        return deskripsi;
    }
    
    public LocalDate getTanggalMulai() {
        return tanggalMulai;
    }
    
    public LocalDate getTanggalBerakhir() {
        return tanggalBerakhir;
    }
    
    public boolean isAktif() {
        return aktif;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }

    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return aktif && !today.isBefore(tanggalMulai) && !today.isAfter(tanggalBerakhir);
    }

    public double hitungDiskon(double harga) {
        return harga * (diskonPersen / 100);
    }
}
