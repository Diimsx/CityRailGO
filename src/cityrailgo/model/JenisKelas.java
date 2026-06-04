package cityrailgo.model;

public class JenisKelas {
    private int id;
    private String namaKelas;
    private String deskripsi;
    private double multiplierHarga;

    public JenisKelas() {}

    public JenisKelas(int id, String namaKelas, String deskripsi, double multiplierHarga) {
        this.id              = id;
        this.namaKelas       = namaKelas;
        this.deskripsi       = deskripsi;
        this.multiplierHarga = multiplierHarga;
    }

    public double hitungHarga(double hargaBase) {
        return hargaBase * multiplierHarga;
    }

    public double getMultiplier() {
        return multiplierHarga;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNamaKelas() {
        return namaKelas;
    }

    public void setNamaKelas(String namaKelas) {
        this.namaKelas = namaKelas;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public double getMultiplierHarga() {
        return multiplierHarga;
    }

    public void setMultiplierHarga(double multiplierHarga) {
        this.multiplierHarga = multiplierHarga;
    }

    @Override
    public String toString() {
        return "JenisKelas{" +
                "id=" + id +
                ", namaKelas='" + namaKelas + '\'' +
                ", multiplierHarga=" + multiplierHarga +
                '}';
    }
}
