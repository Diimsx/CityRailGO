package model;

public class Kereta {

    public static final String STATUS_AKTIF = "AKTIF";
    public static final String STATUS_NON_AKTIF = "NON-AKTIF";

    private int id;
    private String nama;
    private String nomorKereta;
    private int jumlahGerbong;
    private int kapasitasTotal;
    private String kelasTersedia;
    private String status;

    public Kereta(String nama, String nomorKereta, int kapasitasTotal) {
        this(nama, nomorKereta, 1, kapasitasTotal, "", STATUS_AKTIF);
    }

    public Kereta(String nama, String nomorKereta, int jumlahGerbong, int kapasitasTotal,
                  String kelasTersedia, String status) {
        this.nama = nama;
        this.nomorKereta = nomorKereta;
        this.jumlahGerbong = jumlahGerbong;
        this.kapasitasTotal = kapasitasTotal;
        this.kelasTersedia = kelasTersedia;
        this.status = status;
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

    public int getJumlahGerbong() {
        return jumlahGerbong;
    }

    public int getKapasitasTotal() {
        return kapasitasTotal;
    }

    public String getKelasTersedia() {
        return kelasTersedia;
    }

    public String getStatus() {
        return status;
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

    public void setJumlahGerbong(int jumlahGerbong) {
        this.jumlahGerbong = jumlahGerbong;
    }

    public void setKapasitasTotal(int kapasitasTotal) {
        this.kapasitasTotal = kapasitasTotal;
    }

    public void setKelasTersedia(String kelasTersedia) {
        this.kelasTersedia = kelasTersedia;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAktif() {
        return STATUS_AKTIF.equalsIgnoreCase(status);
    }
}
