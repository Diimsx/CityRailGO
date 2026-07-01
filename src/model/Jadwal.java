package model;

import java.time.LocalDateTime;

public class Jadwal {

    private int id;
    private Kereta kereta;
    private Rute rute;
    private JenisKelas jenisKelas;
    private LocalDateTime waktuBerangkat;
    private LocalDateTime waktuTiba;
    private String status;
    private double hargaFinal;
    private String infoHarga;

    public Jadwal(Kereta kereta, Rute rute, JenisKelas jenisKelas,
                  LocalDateTime waktuBerangkat, LocalDateTime waktuTiba) {
        this.kereta        = kereta;
        this.rute          = rute;
        this.jenisKelas    = jenisKelas;
        this.waktuBerangkat = waktuBerangkat;
        this.waktuTiba     = waktuTiba;
        this.status        = "TERSEDIA";
        this.hargaFinal    = 0;
        this.infoHarga     = "";
    }

    public int getId() { 
        return id;
    }

    public Kereta getKereta() {
        return kereta;
    }

    public Rute getRute() {
        return rute;
    }

    public JenisKelas getJenisKelas() {
        return jenisKelas;
    }

    public LocalDateTime getWaktuBerangkat() {
        return waktuBerangkat;
    }

    public LocalDateTime getWaktuTiba() {
        return waktuTiba;
    }

    public String getStatus() {
        return status;
    }

    public double getHargaFinal() {
        return hargaFinal;
    }

    public String getInfoHarga() {
        return infoHarga;
    }

    public double getHargaEfektif() {
        if (hargaFinal > 0) return hargaFinal;
        if (jenisKelas != null && rute != null) {
            return jenisKelas.hitungHarga(rute.getJarakKm());
        }
        return 0;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHargaFinal(double hargaFinal) {
        this.hargaFinal = hargaFinal;
    }

    public void setInfoHarga(String infoHarga) {
        this.infoHarga = infoHarga;
    }

    public boolean isAvailable() {
        return "TERSEDIA".equalsIgnoreCase(status);
    }
}
