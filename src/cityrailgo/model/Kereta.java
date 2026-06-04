package cityrailgo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Kereta {
    private int id;
    private String nama;
    private int kapasitas;
    private List<Kursi> kursiList;

    public Kereta() {
        this.kursiList = new ArrayList<>();
    }

    public Kereta(int id, String nama, int kapasitas) {
        this.id        = id;
        this.nama      = nama;
        this.kapasitas = kapasitas;
        this.kursiList = new ArrayList<>();
    }

    public Kereta(int id, String nama, int kapasitas, List<Kursi> kursiList) {
        this.id        = id;
        this.nama      = nama;
        this.kapasitas = kapasitas;
        this.kursiList = kursiList != null ? kursiList : new ArrayList<>();
    }

    public void tambahKursi(Kursi kursi) {
        if (kursi == null) {
            System.out.println("[Kereta] Kursi tidak boleh null.");
            return;
        }
        if (kursiList.size() >= kapasitas) {
            System.out.println("[Kereta] Kapasitas penuh. Tidak dapat menambah kursi baru.");
            return;
        }
        kursiList.add(kursi);
        System.out.println("[Kereta] Kursi " + kursi.getNomorKursi() + " berhasil ditambahkan ke " + nama + ".");
    }

    public List<Kursi> getKursiTersedia(Jadwal jadwal) {
        if (jadwal == null) return new ArrayList<>();
        return kursiList.stream()
                .filter(k -> k.isTersediaUntuk(jadwal))
                .collect(Collectors.toList());
    }

    public List<Kursi> getKursiByKelas(JenisKelas jenisKelas) {
        if (jenisKelas == null) return new ArrayList<>();
        return kursiList.stream()
                .filter(k -> k.getJenisKelas() != null
                          && k.getJenisKelas().getId() == jenisKelas.getId())
                .collect(Collectors.toList());
    }

    public int getJumlahKursiTersedia() {
        return (int) kursiList.stream()
                .filter(Kursi::isTersedia)
                .count();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public int getKapasitas() {
        return kapasitas;
    }

    public void setKapasitas(int kapasitas) {
        this.kapasitas = kapasitas;
    }

    public List<Kursi> getKursiList() {
        return kursiList;
    }

    public void setKursiList(List<Kursi> kursiList) {
        this.kursiList = kursiList != null ? kursiList : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Kereta{" +
                "id=" + id +
                ", nama='" + nama + '\'' +
                ", kapasitas=" + kapasitas +
                ", jumlahKursi=" + kursiList.size() +
                '}';
    }
}
