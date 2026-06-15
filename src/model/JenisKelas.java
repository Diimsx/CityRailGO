package model;

public class JenisKelas {

    private int id;
    private String namaKelas;
    private double hargaPerKm;
    private String fasilitas;

    public JenisKelas(String namaKelas, double hargaPerKm, String fasilitas) {
        this.namaKelas = namaKelas;
        this.hargaPerKm = hargaPerKm;
        this.fasilitas = fasilitas;
    }

    public int getId() {
        return id;
    }

    public String getNamaKelas() {
        return namaKelas;
    }

    public double getHargaPerKm() {
        return hargaPerKm;
    }

    public String getFasilitas() {
        return fasilitas;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setHargaPerKm(double hargaPerKm) {
        this.hargaPerKm = hargaPerKm;
    }

    public double hitungHarga(double jarakKm) {
        return hargaPerKm * jarakKm;
    }
}
