package cityrailgo.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.ArrayList;

public class Kereta {
    private final IntegerProperty id;
    private final StringProperty nama;
    private final IntegerProperty kapasitas;
    
    private final ObservableList<Object> kursiList; 

    public Kereta(int id, String nama, int kapasitas, List<Object> kursiList) {
        this.id = new SimpleIntegerProperty(id);
        this.nama = new SimpleStringProperty(nama);
        this.kapasitas = new SimpleIntegerProperty(kapasitas);
        this.kursiList = FXCollections.observableArrayList(kursiList != null ? kursiList : new ArrayList<>());
    }

    public int getId() { return id.get(); }
    public String getNama() { return nama.get(); }
    public int getKapasitas() { return kapasitas.get(); }
    public ObservableList<Object> getKursiList() { return kursiList; }

    public IntegerProperty idProperty() { return id; }
    public StringProperty namaProperty() { return nama; }
    public IntegerProperty kapasitasProperty() { return kapasitas; }

    public void tambahKursi(Object kursi) {
        this.kursiList.add(kursi);
    }

    public List<Object> getKursiTersedia(Jadwal jadwal) {
        return new ArrayList<>(this.kursiList); 
    }

    public List<Object> getKursiByKelas(JenisKelas jenisKelas) {
        return new ArrayList<>(this.kursiList);
    }
}