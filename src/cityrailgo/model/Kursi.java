package cityrailgo.model;

public class Kursi {
    private int id;
    private String nomorKursi;

    public Kursi() {}

    public Kursi(int id, String nomorKursi) {
        this.id = id;
        this.nomorKursi = nomorKursi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomorKursi() { return nomorKursi; }
    public void setNomorKursi(String nomorKursi) { this.nomorKursi = nomorKursi; }

    @Override
    public String toString() {
        return "Kursi{" + "id=" + id + ", nomorKursi='" + nomorKursi + '\'' + '}';
    }
}