package model;

import java.time.LocalDate;

public class Penumpang extends User {

    private String nik;
    private LocalDate tglLahir;
    private String jenisKelamin;

    public Penumpang(String username, String password, String email, String namaLengkap, String noTelepon, String nik, LocalDate tglLahir, String jenisKelamin) {
        super(username, password, email, namaLengkap, noTelepon);
        this.nik = nik;
        this.tglLahir = tglLahir;
        this.jenisKelamin = jenisKelamin;
    }

    public String getNik() {
        return nik;
    }

    public LocalDate getTglLahir() {
        return tglLahir;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public void setTglLahir(LocalDate tglLahir) {
        this.tglLahir = tglLahir;
    }

    public void setJenisKelamin(String jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    @Override
    public String getRole() {
        return "PENUMPANG";
    }
}
