package model;

public class Kereta {

    private int id;
    private String nama;
    private String nomorKereta;
    private int kapasitasTotal;

    public Kereta(String nama, String nomorKereta, int kapasitasTotal) {
        this.nama = nama;
        this.nomorKereta = nomorKereta;
        this.kapasitasTotal = kapasitasTotal;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getNomorKereta() {
        return nomorKereta;
    }

    public int getKapasitasTotal() {
        return kapasitasTotal;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setNomorKereta(String nomorKereta) {
        this.nomorKereta = nomorKereta;
    }

    public void setKapasitasTotal(int kapasitasTotal) {
        this.kapasitasTotal = kapasitasTotal;
    }
}