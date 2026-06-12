package cityrailgo.dao;

import cityrailgo.model.Jadwal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JadwalDAO {

    private List<Jadwal> daftarJadwal = new ArrayList<>();

    public Jadwal findById(int id) {

        for (Jadwal jadwal : daftarJadwal) {
            if (jadwal.getId() == id) {
                return jadwal;
            }
        }

        return null;
    }

    public List<Jadwal> findAll() {
        return daftarJadwal;
    }

    public List<Jadwal> findByRuteDanTanggal(
            String stasiunAsal,
            String stasiunTujuan,
            LocalDate tanggal) {

        return daftarJadwal;
    }

    public boolean save(Jadwal jadwal) {
        return daftarJadwal.add(jadwal);
    }

    public boolean update(Jadwal jadwal) {
        return true;
    }

    public boolean delete(int id) {

        Jadwal jadwal = findById(id);

        if (jadwal != null) {
            return daftarJadwal.remove(jadwal);
        }

        return false;
    }
}
