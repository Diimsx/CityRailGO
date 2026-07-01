package model;

public class Tiket {

    private int id;
    private String kodeTiket;
    private Penumpang penumpang;
    private Jadwal jadwal;
    private Kursi kursi;
    private double hargaTotal;
    private String status;
    private String namaPenumpang;
    private String nikPenumpang;
    private int usiaPenumpang;

    public Tiket(Penumpang penumpang, Jadwal jadwal, Kursi kursi, double hargaTotal) {
        this.penumpang = penumpang;
        this.jadwal = jadwal;
        this.kursi = kursi;
        this.hargaTotal = hargaTotal;
        this.status = "AKTIF";
        if (penumpang != null) {
            this.namaPenumpang = penumpang.getNamaLengkap();
            this.nikPenumpang = penumpang.getNik();
        }
    }

    public int getId() {
        return id;
    }

    public String getKodeTiket() {
        return kodeTiket;
    }

    public Penumpang getPenumpang() {
        return penumpang;
    }

    public Jadwal getJadwal() {
        return jadwal;
    }

    public Kursi getKursi() {
        return kursi;
    }

    public double getHargaTotal() {
        return hargaTotal;
    }

    public String getStatus() {
        return status;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setKodeTiket(String kodeTiket) {
        this.kodeTiket = kodeTiket;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNamaPenumpang() {
        return namaPenumpang;
    }

    public void setNamaPenumpang(String namaPenumpang) {
        this.namaPenumpang = namaPenumpang;
    }

    public String getNikPenumpang() {
        return nikPenumpang;
    }

    public void setNikPenumpang(String nikPenumpang) {
        this.nikPenumpang = nikPenumpang;
    }

    public int getUsiaPenumpang() {
        return usiaPenumpang;
    }

    public void setUsiaPenumpang(int usiaPenumpang) {
        this.usiaPenumpang = usiaPenumpang;
    }
}
