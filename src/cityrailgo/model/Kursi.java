package cityrailgo.model;

import java.util.List;

public class Kursi {
    private int id;
    private Kereta kereta;
    private JenisKelas jenisKelas;
    private String nomorKursi;
    private boolean tersedia;

    public Kursi() {
        this.tersedia = true;
    }

    public Kursi(int id, Kereta kereta, JenisKelas jenisKelas, String nomorKursi) {
        this.id          = id;
        this.kereta      = kereta;
        this.jenisKelas  = jenisKelas;
        this.nomorKursi  = nomorKursi;
        this.tersedia    = true;
    }

    public Kursi(int id, Kereta kereta, JenisKelas jenisKelas, String nomorKursi, boolean tersedia) {
        this.id          = id;
        this.kereta      = kereta;
        this.jenisKelas  = jenisKelas;
        this.nomorKursi  = nomorKursi;
        this.tersedia    = tersedia;
    }

    public boolean isAvailable(Jadwal jadwal) {
        if (jadwal == null) return false;

        List<Tiket> tiketJadwal = jadwal.getTiketList();
        if (tiketJadwal == null || tiketJadwal.isEmpty()) return true;

        return tiketJadwal.stream()
                .filter(t -> t.getKursi() != null && t.getKursi().getId() == this.id)
                .noneMatch(t -> t.getStatus() != null && t.getStatus().isAktif());
    }

    public boolean isTersediaUntuk(Jadwal jadwal) {
        return isAvailable(jadwal);
    }

    public boolean isTersedia() {
        return tersedia;
    }

    public String getLabel() {
        String kelas = jenisKelas != null ? jenisKelas.getNamaKelas() : "Unknown";
        return nomorKursi + " - " + kelas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Kereta getKereta() {
        return kereta;
    }

    public void setKereta(Kereta kereta) {
        this.kereta = kereta;
    }

    public JenisKelas getJenisKelas() {
        return jenisKelas;
    }

    public void setJenisKelas(JenisKelas jenisKelas) {
        this.jenisKelas = jenisKelas;
    }

    public String getNomorKursi() {
        return nomorKursi;
    }

    public void setNomorKursi(String nomorKursi) {
        this.nomorKursi = nomorKursi;
    }

    public void setTersedia(boolean tersedia) {
        this.tersedia = tersedia;
    }

    @Override
    public String toString() {
        return "Kursi{" +
                "id=" + id +
                ", nomorKursi='" + nomorKursi + '\'' +
                ", jenisKelas=" + (jenisKelas != null ? jenisKelas.getNamaKelas() : "-") +
                ", tersedia=" + tersedia +
                '}';
    }
}
