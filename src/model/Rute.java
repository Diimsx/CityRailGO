package model;

public class Rute {

    private int id;
    private String stasiunAsal;
    private String stasiunTujuan;
    private double jarakKm;
    private int estimasiMenit;

    public Rute(String stasiunAsal, String stasiunTujuan, double jarakKm, int estimasiMenit) {
        this.stasiunAsal = stasiunAsal;
        this.stasiunTujuan = stasiunTujuan;
        this.jarakKm = jarakKm;
        this.estimasiMenit = estimasiMenit;
    }

    public int getId() {
        return id;
    }

    public String getStasiunAsal() {
        return stasiunAsal;
    }

    public String getStasiunTujuan() {
        return stasiunTujuan;
    }

    public double getJarakKm() {
        return jarakKm;
    }

    public int getEstimasiMenit() {
        return estimasiMenit;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setStasiunAsal(String stasiunAsal) {
        this.stasiunAsal = stasiunAsal;
    }

    public void setStasiunTujuan(String stasiunTujuan) {
        this.stasiunTujuan = stasiunTujuan;
    }

    public void setJarakKm(double jarakKm) {
        this.jarakKm = jarakKm;
    }

    public void setEstimasiMenit(int estimasiMenit) {
        this.estimasiMenit = estimasiMenit;
    }
}