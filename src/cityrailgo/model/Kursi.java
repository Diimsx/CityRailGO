package cityrailgo.model;

import javafx.beans.property.*;

public class Kursi {
    private final IntegerProperty id;
    private final StringProperty nomorKursi;

    public Kursi(int id, String nomorKursi) {
        this.id = new SimpleIntegerProperty(id);
        this.nomorKursi = new SimpleStringProperty(nomorKursi);
    }

    public int getId() { return id.get(); }
    public String getNomorKursi() { return nomorKursi.get(); }
    
    public IntegerProperty idProperty() { return id; }
    public StringProperty nomorKursiProperty() { return nomorKursi; }
}