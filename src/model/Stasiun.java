package model;

public class Stasiun {

    private int id;
    private String kodeStasiun;
    private String namaStasiun;
    private String kota;

    public Stasiun(String kodeStasiun, String namaStasiun, String kota) {
        this.kodeStasiun = kodeStasiun;
        this.namaStasiun = namaStasiun;
        this.kota = kota;
    }

    public int getId() {
        return id;
    }

    public String getKodeStasiun() {
        return kodeStasiun;
    }

    public String getNamaStasiun() {
        return namaStasiun;
    }

    public String getKota() {
        return kota;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setKodeStasiun(String kodeStasiun) {
        this.kodeStasiun = kodeStasiun;
    }

    public void setNamaStasiun(String namaStasiun) {
        this.namaStasiun = namaStasiun;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }

    @Override
    public String toString() {
        return kodeStasiun + " - " + namaStasiun + " (" + kota + ")";
    }
}