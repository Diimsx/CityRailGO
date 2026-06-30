package model;

public class Admin extends User {

    private String levelAkses;

    /** Constructor lengkap (dipakai saat build dari ResultSet) */
    public Admin(String username, String password, String email, String namaLengkap, String noTelepon, String levelAkses) {
        super(username, password, email, namaLengkap, noTelepon);
        this.levelAkses = levelAkses;
    }

    /** Constructor ringkas untuk Admin — tidak perlu email/nama/telepon */
    public Admin(String username, String password, String levelAkses) {
        super(username, password, null, null, null);
        this.levelAkses = levelAkses;
    }

    public String getLevelAkses() {
        return levelAkses;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}