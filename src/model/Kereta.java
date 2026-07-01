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

    private int gerbongEksekutif;
    private int gerbongBisnis;
    private int gerbongEkonomi;

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
        applyFallbackCarriages();
    }

    public Kereta(String nama, String nomorKereta, int gerbongEks, int gerbongBis, int gerbongEko,
                  String kelasTersedia, String status) {
        this.nama = nama;
        this.nomorKereta = nomorKereta;
        this.gerbongEksekutif = gerbongEks;
        this.gerbongBisnis = gerbongBis;
        this.gerbongEkonomi = gerbongEko;
        this.jumlahGerbong = gerbongEks + gerbongBis + gerbongEko;
        this.kapasitasTotal = gerbongEks * 50 + gerbongBis * 60 + gerbongEko * 80;
        this.kelasTersedia = kelasTersedia;
        this.status = status;
    }

    private void applyFallbackCarriages() {
        if (this.gerbongEksekutif == 0 && this.gerbongBisnis == 0 && this.gerbongEkonomi == 0 && this.jumlahGerbong > 0) {
            String kelas = (this.kelasTersedia == null ? "" : this.kelasTersedia).toLowerCase();
            boolean hasEks = kelas.contains("eksekutif");
            boolean hasBis = kelas.contains("bisnis");
            boolean hasEko = kelas.contains("ekonomi");
            int count = (hasEks ? 1 : 0) + (hasBis ? 1 : 0) + (hasEko ? 1 : 0);
            if (count > 0) {
                int base = this.jumlahGerbong / count;
                int rem = this.jumlahGerbong % count;
                if (hasEks) {
                    this.gerbongEksekutif = base + rem;
                    rem = 0;
                }
                if (hasBis) {
                    this.gerbongBisnis = base + (hasEks ? 0 : rem);
                    rem = 0;
                }
                if (hasEko) {
                    this.gerbongEkonomi = base + (hasEks || hasBis ? 0 : rem);
                }
            } else {
                this.gerbongEkonomi = this.jumlahGerbong;
            }
        }
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

    public int getGerbongEksekutif() {
        return gerbongEksekutif;
    }

    public void setGerbongEksekutif(int gerbongEksekutif) {
        this.gerbongEksekutif = gerbongEksekutif;
    }

    public int getGerbongBisnis() {
        return gerbongBisnis;
    }

    public void setGerbongBisnis(int gerbongBisnis) {
        this.gerbongBisnis = gerbongBisnis;
    }

    public int getGerbongEkonomi() {
        return gerbongEkonomi;
    }

    public void setGerbongEkonomi(int gerbongEkonomi) {
        this.gerbongEkonomi = gerbongEkonomi;
    }

    public boolean isAktif() {
        return STATUS_AKTIF.equalsIgnoreCase(status);
    }
}
