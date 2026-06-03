package cityrailgo.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class Jadwal {
    public static final String STATUS_TERSEDIA = "TERSEDIA";
    public static final String STATUS_PENUH = "PENUH";
    public static final String STATUS_DIBATALKAN = "DIBATALKAN";
    public static final String STATUS_SELESAI = "SELESAI";

    private final IntegerProperty id;
    private final ObjectProperty<Kereta> kereta;
    private final ObjectProperty<Rute> rute;
    private final ObjectProperty<LocalDateTime> waktuBerangkat;
    private final ObjectProperty<LocalDateTime> waktuTiba;
    private final DoubleProperty hargaBase;
    private final StringProperty status;
    private final ObservableList<Tiket> tiketList;

    public Jadwal(int id, Kereta kereta, Rute rute, LocalDateTime waktuBerangkat, LocalDateTime waktuTiba, double hargaBase, String status, List<Tiket> tiketList) {
        this.id = new SimpleIntegerProperty(id);
        this.kereta = new SimpleObjectProperty<>(kereta);
        this.rute = new SimpleObjectProperty<>(rute);
        this.waktuBerangkat = new SimpleObjectProperty<>(waktuBerangkat);
        this.waktuTiba = new SimpleObjectProperty<>(waktuTiba);
        this.hargaBase = new SimpleDoubleProperty(hargaBase);
        this.status = new SimpleStringProperty(status);
        this.tiketList = FXCollections.observableArrayList(tiketList != null ? tiketList : new ArrayList<>());
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }

    public Kereta getKereta() { return kereta.get(); }
    public void setKereta(Kereta value) { kereta.set(value); }

    public Rute getRute() { return rute.get(); }
    public void setRute(Rute value) { rute.set(value); }

    public LocalDateTime getWaktuBerangkat() { return waktuBerangkat.get(); }
    public void setWaktuBerangkat(LocalDateTime value) { waktuBerangkat.set(value); }

    public LocalDateTime getWaktuTiba() { return waktuTiba.get(); }
    public void setWaktuTiba(LocalDateTime value) { waktuTiba.set(value); }

    public double getHargaBase() { return hargaBase.get(); }
    public void setHargaBase(double value) { hargaBase.set(value); }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }

    public ObservableList<Tiket> getTiketList() { return tiketList; }

    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<Kereta> keretaProperty() { return kereta; }
    public ObjectProperty<Rute> ruteProperty() { return rute; }
    public ObjectProperty<LocalDateTime> waktuBerangkatProperty() { return waktuBerangkat; }
    public ObjectProperty<LocalDateTime> waktuTibaProperty() { return waktuTiba; }
    public DoubleProperty hargaBaseProperty() { return hargaBase; }
    public StringProperty statusProperty() { return status; }

    public List<Kursi> getKursiTersedia() {
        List<Kursi> kursiKosong = new ArrayList<>();
        if (getKereta() != null && getKereta().getKursiList() != null) {
            for (Kursi kursi : getKereta().getKursiList()) {
                boolean sudahDipesan = false;
                for (Tiket tiket : getTiketList()) {
                    if (tiket.getKursi() != null && tiket.getKursi().equals(kursi)) {
                        sudahDipesan = true;
                        break;
                    }
                }
                if (!sudahDipesan) {
                    kursiKosong.add(kursi);
                }
            }
        }
        return kursiKosong; 
    }

    public long hitungDurasi() {
        if (getWaktuBerangkat() != null && getWaktuTiba() != null) {
            return Duration.between(getWaktuBerangkat(), getWaktuTiba()).toMinutes();
        }
        return 0;
    }

    public String getDurasiFormatted() {
        long totalMenit = hitungDurasi();
        long jam = totalMenit / 60;
        long menit = totalMenit % 60;
        return jam + " Jam " + menit + " Menit";
    }

    public boolean isBisaDipesan() {
        return STATUS_TERSEDIA.equals(getStatus()) && LocalDateTime.now().isBefore(getWaktuBerangkat());
    }

    public int getJumlahKursiTerisi() {
        return tiketList.size();
    }
}