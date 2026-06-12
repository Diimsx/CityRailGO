package cityrailgo.dao;

import cityrailgo.model.*;
import java.util.ArrayList;
import java.util.List;

public class KursiDAO {

    private List<Kursi> daftarKursi = new ArrayList<>();

    public Kursi findById(int id) {

        for (Kursi kursi : daftarKursi) {
            if (kursi.getId() == id) {
                return kursi;
            }
        }

        return null;
    }

    public List<Kursi> findByJadwal(Jadwal jadwal) {
        return daftarKursi;
    }

    public List<Kursi> findTersediaByJadwal(Jadwal jadwal) {
        return jadwal.getKursiTersedia();
    }

    public boolean update(Kursi kursi) {
        return true;
    }
}
