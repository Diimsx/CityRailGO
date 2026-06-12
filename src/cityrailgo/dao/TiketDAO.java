package cityrailgo.dao;

import cityrailgo.model.*;
import java.util.ArrayList;
import java.util.List;

public class TiketDAO {

    private List<Tiket> daftarTiket = new ArrayList<>();

    public Tiket findById(int id) {

        for (Tiket tiket : daftarTiket) {
            if (tiket.getId() == id) {
                return tiket;
            }
        }

        return null;
    }

    public List<Tiket> findByPenumpang(Penumpang penumpang) {

        List<Tiket> hasil = new ArrayList<>();

        for (Tiket tiket : daftarTiket) {
            if (tiket.getPenumpang().equals(penumpang)) {
                hasil.add(tiket);
            }
        }

        return hasil;
    }

    public List<Tiket> findAll() {
        return daftarTiket;
    }

    public boolean save(Tiket tiket) {
        return daftarTiket.add(tiket);
    }

    public boolean update(Tiket tiket) {
        return true;
    }
}
