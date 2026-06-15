package model;

public class Kursi {

    private int id;
    private Jadwal jadwal;
    private JenisKelas jenisKelas;
    private String nomorKursi;
    private String status;

    public Kursi(Jadwal jadwal, JenisKelas jenisKelas, String nomorKursi) {
        this.jadwal = jadwal;
        this.jenisKelas = jenisKelas;
        this.nomorKursi = nomorKursi;
        this.status = "TERSEDIA";
    }

    public int getId() {
        return id;
    }

    public Jadwal getJadwal() {
        return jadwal;
    }

    public String getNomorKursi() {
        return nomorKursi;
    }

    public String getStatus() {
        return status;
    }

    public JenisKelas getJenisKelas() {
        return jenisKelas;
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
