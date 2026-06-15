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

    public Jadwal(Kereta kereta, Rute rute, JenisKelas jenisKelas, LocalDateTime waktuBerangkat, LocalDateTime waktuTiba) {
        this.kereta = kereta;
        this.rute = rute;
        this.jenisKelas = jenisKelas;
        this.waktuBerangkat = waktuBerangkat;
        this.waktuTiba = waktuTiba;
        this.status = "TERSEDIA";
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
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isAvailable() {
        return status.equalsIgnoreCase("TERSEDIA");
    }
}
