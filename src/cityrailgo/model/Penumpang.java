package cityrailgo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Penumpang extends User {
    private String nama;
    private String nik;
    private String noTelp;
    private String jenisKelamin;
    private LocalDate tglLahir;
    private List<Tiket> tiketList;

    public Penumpang() {
        super();
        this.tiketList = new ArrayList<>();
    }

    public Penumpang(int id, String username, String email, String password,
                     String nama, String nik, String noTelp,
                     String jenisKelamin, LocalDate tglLahir) {
        super(id, username, email, password);
        this.nama         = nama;
        this.nik          = nik;
        this.noTelp       = noTelp;
        this.jenisKelamin = jenisKelamin;
        this.tglLahir     = tglLahir;
        this.tiketList    = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "PENUMPANG";
    }

    @Override
    public String getInfo() {
        return "Penumpang: " + nama + " (@" + getUsername() + ") | NIK: " + nik;
    }

    public String getPenumpangInfo() {
        return "Nama    : " + nama + "\n" +
               "NIK     : " + nik + "\n" +
               "No. Telp: " + noTelp + "\n" +
               "Kelamin : " + (jenisKelamin.equals("L") ? "Laki-laki" : "Perempuan");
    }

    public boolean isNikValid() {
        if (nik == null) return false;
        return nik.matches("\\d{16}");
    }

    public Tiket pesan(Jadwal jadwal, Kursi kursi, Promo promo) {
        Tiket tiket = new Tiket();
        tiket.setJadwal(jadwal);
        tiket.setPenumpang(this);
        tiket.setKursi(kursi);
        tiket.setPromo(promo);
        tiket.setStatus(StatusTiket.DIPESAN);
        tiket.setKodeTiket(tiket.generateKodeTiket());
        tiket.setHargaAkhir(tiket.hitungHargaAkhir());
        tiketList.add(tiket);
        return tiket;
    }

    public void batalTiket(Tiket tiket) {
        if (tiket == null) {
            System.out.println("[Penumpang] Tiket tidak ditemukan.");
            return;
        }
        if (tiket.getStatus() != StatusTiket.DIPESAN) {
            System.out.println("[Penumpang] Tiket tidak dapat dibatalkan. Status saat ini: "
                    + tiket.getStatus());
            return;
        }
        tiket.batalkan();
        System.out.println("[Penumpang] Tiket " + tiket.getKodeTiket() + " berhasil dibatalkan.");
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(String jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public LocalDate getTglLahir() {
        return tglLahir;
    }

    public void setTglLahir(LocalDate tglLahir) {
        this.tglLahir = tglLahir;
    }

    public List<Tiket> getTiketList() {
        return tiketList;
    }

    public void setTiketList(List<Tiket> tiketList) {
        this.tiketList = tiketList;
    }

    public void addTiket(Tiket tiket) {
        this.tiketList.add(tiket);
    }

    @Override
    public String toString() {
        return "Penumpang{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", nama='" + nama + '\'' +
                ", nik='" + nik + '\'' +
                ", noTelp='" + noTelp + '\'' +
                ", jenisKelamin='" + jenisKelamin + '\'' +
                ", tglLahir=" + tglLahir +
                ", jumlahTiket=" + tiketList.size() +
                '}';
    }
}