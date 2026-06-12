package cityrailgo.model;

import java.time.LocalDate;

public class Admin extends User {

    private String levelAkses;
    private String jenisKelamin;
    private LocalDate tglLahir;

    public Admin(String username,
                 String password,
                 String email,
                 String namaLengkap,
                 String noTelepon,
                 String levelAkses,
                 String jenisKelamin,
                 LocalDate tglLahir) {

        super(username,
                password,
                email,
                namaLengkap,
                noTelepon);

        this.levelAkses = levelAkses;
        this.jenisKelamin = jenisKelamin;
        this.tglLahir = tglLahir;
    }

    public String getLevelAkses() {
        return levelAkses;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public LocalDate getTglLahir() {
        return tglLahir;
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
