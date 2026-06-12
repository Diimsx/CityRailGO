package cityrailgo.dao;

import cityrailgo.model.Promo;
import java.util.ArrayList;
import java.util.List;

public class PromoDAO {

    private List<Promo> daftarPromo = new ArrayList<>();

    public Promo findById(int id) {
        for (Promo promo : daftarPromo) {
            if (promo.getId() == id) {
                return promo;
            }
        }
        return null;
    }

    public Promo findByKode(String kodePromo) {
        for (Promo promo : daftarPromo) {
            if (promo.getKodePromo().equals(kodePromo)) {
                return promo;
            }
        }
        return null;
    }

    public List<Promo> findAllAktif() {
        List<Promo> hasil = new ArrayList<>();

        for (Promo promo : daftarPromo) {
            if (promo.isValid()) {
                hasil.add(promo);
            }
        }

        return hasil;
    }

    public boolean save(Promo promo) {
        return daftarPromo.add(promo);
    }

    public boolean update(Promo promo) {
        return true;
    }

    public boolean delete(int id) {
        Promo promo = findById(id);

        if (promo != null) {
            return daftarPromo.remove(promo);
        }

        return false;
    }
}
