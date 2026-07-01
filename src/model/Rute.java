package model;

import java.util.ArrayList;
import java.util.List;

public class Rute {

    private int id;
    private String namaRute;
    private Stasiun stasiunAsalObj;
    private Stasiun stasiunTujuanObj;
    private double jarakKm;
    private int estimasiMenit;

    private List<Stasiun> stasiunPemberhentian = new ArrayList<>();

    public Rute(String namaRute, Stasiun stasiunAsal, Stasiun stasiunTujuan,
                double jarakKm, int estimasiMenit) {
        this.namaRute       = namaRute;
        this.stasiunAsalObj = stasiunAsal;
        this.stasiunTujuanObj = stasiunTujuan;
        this.jarakKm        = jarakKm;
        this.estimasiMenit  = estimasiMenit;
    }

    public Rute(String stasiunAsal, String stasiunTujuan, double jarakKm, int estimasiMenit) {
        this.namaRute         = stasiunAsal + " \u2192 " + stasiunTujuan;
        this.stasiunAsalObj   = new Stasiun("", stasiunAsal, "");
        this.stasiunTujuanObj = new Stasiun("", stasiunTujuan, "");
        this.jarakKm          = jarakKm;
        this.estimasiMenit    = estimasiMenit;
    }

    public int getId() {
        return id;
    }

    public String getNamaRute() {
        return namaRute;
    }

    public Stasiun getStasiunAsalObj() {
        return stasiunAsalObj;
    }

    public Stasiun getStasiunTujuanObj() {
        return stasiunTujuanObj;
    }

    public String getStasiunAsal() {
        return stasiunAsalObj == null ? "" : stasiunAsalObj.getNamaStasiun();
    }

    public String getStasiunTujuan() {
        return stasiunTujuanObj == null ? "" : stasiunTujuanObj.getNamaStasiun();
    }

    public double getJarakKm() {
        return jarakKm;
    }

    public int getEstimasiMenit() {
        return estimasiMenit;
    }

    public List<Stasiun> getStasiunPemberhentian() {
        return stasiunPemberhentian;
    }

    public String getPreviewUrutan() {
        StringBuilder sb = new StringBuilder(getStasiunAsal());
        for (Stasiun s : stasiunPemberhentian) {
            sb.append(" \u2192 ").append(s.getNamaStasiun());
        }
        sb.append(" \u2192 ").append(getStasiunTujuan());
        return sb.toString();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNamaRute(String namaRute) {
        this.namaRute = namaRute;
    }

    public void setStasiunAsalObj(Stasiun stasiunAsalObj) {
        this.stasiunAsalObj = stasiunAsalObj;
    }

    public void setStasiunTujuanObj(Stasiun stasiunTujuanObj) {
        this.stasiunTujuanObj = stasiunTujuanObj;
    }

    public void setStasiunAsal(String stasiunAsal) {
        if (this.stasiunAsalObj == null) {
            this.stasiunAsalObj = new Stasiun("", stasiunAsal, "");
        } else {
            this.stasiunAsalObj.setNamaStasiun(stasiunAsal);
        }
    }

    public void setStasiunTujuan(String stasiunTujuan) {
        if (this.stasiunTujuanObj == null) {
            this.stasiunTujuanObj = new Stasiun("", stasiunTujuan, "");
        } else {
            this.stasiunTujuanObj.setNamaStasiun(stasiunTujuan);
        }
    }

    public void setJarakKm(double jarakKm) { this.jarakKm = jarakKm; }

    public void setEstimasiMenit(int estimasiMenit) { this.estimasiMenit = estimasiMenit; }

    public void setStasiunPemberhentian(List<Stasiun> stasiunPemberhentian) {
        this.stasiunPemberhentian = stasiunPemberhentian == null ? new ArrayList<>() : stasiunPemberhentian;
    }

    @Override
    public String toString() {
        return namaRute + " (" + getStasiunAsal() + " \u2192 " + getStasiunTujuan() + ")";
    }
}