package cityrailgo.model;

import java.util.ArrayList;
import java.util.List;

public class Kereta {

    private int id;
    private String nama;
    private int kapasitas;
    private List<Kursi> kursiList;

    public Kereta(int id, String nama, int kapasitas) {
        this.id = id;
        this.nama = nama;
        this.kapasitas = kapasitas;
        this.kursiList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public int getKapasitas() {
        return kapasitas;
    }

    public List<Kursi> getKursiList() {
        return kursiList;
    }

    public void tambahKursi(Kursi kursi) {
        kursiList.add(kursi);
    }

    public List<Kursi> getKursiTersedia(Jadwal jadwal) {
        return kursiList;
    }

    public List<Kursi> getKursiByKelas(JenisKelas jenisKelas) {

        List<Kursi> hasil = new ArrayList<>();

        for (Kursi kursi : kursiList) {
            if (kursi.getJenisKelas().equals(jenisKelas)) {
                hasil.add(kursi);
            }
        }

        return hasil;
    }
}
