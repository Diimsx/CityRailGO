package cityrailgo.model;
 
public class Admin extends User {
    private String nama;
 
    public Admin() {
        super();
    }
 
    public Admin(int id, String username, String email, String password, String nama) {
        super(id, username, email, password);
        this.nama = nama;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
 
    @Override
    public String getInfo() {
        return "Admin: " + nama + " (@" + getUsername() + ")";
    }

    public void tambahJadwal(Jadwal jadwal) {
        JadwalDAO dao = new JadwalDAO();
        dao.tambah(jadwal);
        System.out.println("[Admin] Jadwal berhasil ditambahkan: " + jadwal);
    }
 
    public void hapusJadwal(int id) {
        JadwalDAO dao = new JadwalDAO();
        dao.hapus(id);
        System.out.println("[Admin] Jadwal dengan ID " + id + " berhasil dihapus.");
    }
 
    public void tambahKereta(Kereta kereta) {
        KeretaDAO dao = new KeretaDAO();
        dao.tambah(kereta);
        System.out.println("[Admin] Kereta berhasil ditambahkan: " + kereta);
    }

    public void tambahRute(Rute rute) {
        RuteDAO dao = new RuteDAO();
        dao.tambah(rute);
        System.out.println("[Admin] Rute berhasil ditambahkan: " + rute);
    }

    public void tambahPromo(Promo promo) {
        PromoDAO dao = new PromoDAO();
        dao.tambah(promo);
        System.out.println("[Admin] Promo berhasil ditambahkan: " + promo.getKodePromo());
    }

    public void nonaktifkanPromo(int id) {
        PromoDAO dao = new PromoDAO();
        dao.nonaktifkan(id);
        System.out.println("[Admin] Promo dengan ID " + id + " telah dinonaktifkan.");
    }

    public String getNama() {
        return nama;
    }
 
    public void setNama(String nama) {
        this.nama = nama;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", nama='" + nama + '\'' +
                '}';
    }
}