package cityrailgo.model;

import javafx.beans.property.*;

public class JenisKelas {
    private final IntegerProperty id;
    private final StringProperty namaKelas;
    private final StringProperty deskripsi;
    private final DoubleProperty multiplierHarga;

    public JenisKelas(int id, String namaKelas, String deskripsi, double multiplierHarga) {
        this.id = new SimpleIntegerProperty(id);
        this.namaKelas = new SimpleStringProperty(namaKelas);
        this.deskripsi = new SimpleStringProperty(deskripsi);
        this.multiplierHarga = new SimpleDoubleProperty(multiplierHarga);
    }

    public int getId() { return id.get(); }
    public String getNamaKelas() { return namaKelas.get(); }
    public String getDeskripsi() { return deskripsi.get(); }
    public double getMultiplierHarga() { return multiplierHarga.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty namaKelasProperty() { return namaKelas; }
    public StringProperty deskripsiProperty() { return deskripsi; }
    public DoubleProperty multiplierHargaProperty() { return multiplierHarga; }

    public double hitungHarga(double hargaBase) {
        return hargaBase * getMultiplierHarga();
    }
}