package cityrailgo.dao;

import cityrailgo.model.Rute;
import java.util.ArrayList;
import java.util.List;

public class RuteDAO {

    private List<Rute> daftarRute = new ArrayList<>();

    public Rute findById(int id) {

        for (Rute rute : daftarRute) {
            if (rute.getId() == id) {
                return rute;
            }
        }

        return null;
    }

    public List<Rute> findAll() {
        return daftarRute;
    }

    public List<Rute> findByAsal(String stasiunAsal) {

        List<Rute> hasil = new ArrayList<>();

        for (Rute rute : daftarRute) {
            if (rute.getAsal().equalsIgnoreCase(stasiunAsal)) {
                hasil.add(rute);
            }
        }

        return hasil;
    }

    public boolean save(Rute rute) {
        return daftarRute.add(rute);
    }

    public boolean update(Rute rute) {
        return true;
    }

    public boolean delete(int id) {

        Rute rute = findById(id);

        if (rute != null) {
            return daftarRute.remove(rute);
        }

        return false;
    }
}
