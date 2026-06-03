package cityrailgo.model;

import javafx.beans.property.*;

public class Rute {
    private final IntegerProperty id;
    private final StringProperty asal;
    private final StringProperty tujuan;
    private final DoubleProperty jarak;

    public Rute(int id, String asal, String tujuan, double jarak) {
        this.id = new SimpleIntegerProperty(id);
        this.asal = new SimpleStringProperty(asal);
        this.tujuan = new SimpleStringProperty(tujuan);
        this.jarak = new SimpleDoubleProperty(jarak);
    }

    public int getId() { return id.get(); }
    public String getAsal() { return asal.get(); }
    public String getTujuan() { return tujuan.get(); }
    public double getJarak() { return jarak.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty asalProperty() { return asal; }
    public StringProperty tujuanProperty() { return tujuan; }
    public DoubleProperty jarakProperty() { return jarak; }

    public String getInfoRute() {
        return getAsal() + " -> " + getTujuan() + " (" + getJarak() + " km)";
    }
}