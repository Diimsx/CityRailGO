package cityrailgo.dao;

import cityrailgo.model.Kereta;
import java.util.ArrayList;
import java.util.List;

public class KeretaDAO {

    private List<Kereta> daftarKereta = new ArrayList<>();

    public Kereta findById(int id) {

        for (Kereta kereta : daftarKereta) {
            if (kereta.getId() == id) {
                return kereta;
            }
        }

        return null;
    }

    public List<Kereta> findAll() {
        return daftarKereta;
    }

    public boolean save(Kereta kereta) {
        return daftarKereta.add(kereta);
    }

    public boolean update(Kereta kereta) {
        return true;
    }

    public boolean delete(int id) {

        Kereta kereta = findById(id);

        if (kereta != null) {
            return daftarKereta.remove(kereta);
        }

        return false;
    }
}
