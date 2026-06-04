package cityrailgo.model;

public class Rute {
    private int id;
    private String asal;
    private String tujuan;
    private double jarak;

    public Rute() {}

    public Rute(int id, String asal, String tujuan, double jarak) {
        this.id     = id;
        this.asal   = asal;
        this.tujuan = tujuan;
        this.jarak  = jarak;
    }

    public String getInfoRute() {
        return asal + " → " + tujuan + " (" + jarak + " km)";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAsal() {
        return asal;
    }

    public void setAsal(String asal) {
        this.asal = asal;
    }

    public String getTujuan() {
        return tujuan;
    }

    public void setTujuan(String tujuan) {
        this.tujuan = tujuan;
    }

    public double getJarak() {
        return jarak;
    }

    public void setJarak(double jarak) {
        this.jarak = jarak;
    }

    @Override
    public String toString() {
        return "Rute{" +
                "id=" + id +
                ", asal='" + asal + '\'' +
                ", tujuan='" + tujuan + '\'' +
                ", jarak=" + jarak + " km" +
                '}';
    }
}
