package model;

public class Kereta {

    private int id;
    private String nama;
    private String nomorKereta;
    private String jenis;
    private int kapasitasTotal;

    public Kereta(String nama, String nomorKereta, String jenis, int kapasitasTotal) {
        this.nama = nama;
        this.nomorKereta = nomorKereta;
        this.jenis = jenis;
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

    public String getJenis() {
        return jenis;
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

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }
}
