package util;

import model.Jadwal;
import model.Kursi;
import java.time.LocalDate;
import java.util.List;

public class PenumpangSession {

    private static String stasiunAsal;
    private static String stasiunTujuan;
    private static LocalDate tanggalBerangkat;
    private static int jumlahDewasa = 1;
    private static int jumlahBayi = 0;

    private static Jadwal jadwalDipilih;
    private static List<Kursi> listKursiDipilih = new java.util.ArrayList<>();
    private static List<String[]> manifesPenumpang;
    private static int sisaDetikTimer = 15 * 60;

    public static void reset() {
        stasiunAsal = null;
        stasiunTujuan = null;
        tanggalBerangkat = null;
        jumlahDewasa = 1;
        jumlahBayi = 0;
        jadwalDipilih = null;
        listKursiDipilih.clear();
        manifesPenumpang = null;
        sisaDetikTimer = 15 * 60;
    }

    public static String getStasiunAsal(){ 
        return stasiunAsal; 
    }
    
    public static void setStasiunAsal(String v){ 
        stasiunAsal = v; 
    }

    public static String getStasiunTujuan(){ 
        return stasiunTujuan; 
    }
    
    public static void setStasiunTujuan(String v){
        stasiunTujuan = v; 
    }

    public static LocalDate getTanggalBerangkat(){
        return tanggalBerangkat; 
    }
    
    public static void      setTanggalBerangkat(LocalDate v){ 
        tanggalBerangkat = v; 
    }

    public static int getJumlahDewasa(){ 
        return jumlahDewasa; 
    }
    
    public static void setJumlahDewasa(int v) {
        jumlahDewasa = v; 
    }

    public static int getJumlahBayi() {
        return jumlahBayi; 
    }
    
    public static void setJumlahBayi(int v) { 
        jumlahBayi = v; 
    }

    public static int getJumlahPenumpang() { 
        return jumlahDewasa + jumlahBayi; 
    }

    public static Jadwal getJadwalDipilih() { 
        return jadwalDipilih; 
    }
    
    public static void setJadwalDipilih(Jadwal v) { 
        jadwalDipilih = v; 
    }

    public static List<Kursi> getListKursiDipilih() {
        return listKursiDipilih; 
    }
    
    public static void setListKursiDipilih(List<Kursi> v) { 
        listKursiDipilih = v; 
    }

    public static Kursi getKursiDipilih() {
        return listKursiDipilih.isEmpty() ? null : listKursiDipilih.get(0); 
    }
    
    public static void setKursiDipilih(Kursi v) {
        listKursiDipilih.clear();
        if (v != null) listKursiDipilih.add(v);
    }

    public static List<String[]> getManifesPenumpang() { 
        return manifesPenumpang; 
    }
    public static void setManifesPenumpang(List<String[]> v) {
        manifesPenumpang = v; 
    }

    public static String getNamaPenumpang(int index) {
        if (manifesPenumpang == null || index >= manifesPenumpang.size()) return null;
        return manifesPenumpang.get(index)[0];
    }

    public static String getNikPenumpang(int index) {
        if (manifesPenumpang == null || index >= manifesPenumpang.size()) return null;
        return manifesPenumpang.get(index)[1];
    }

    public static String getUsiaPenumpang(int index) {
        if (manifesPenumpang == null || index >= manifesPenumpang.size()) return null;
        return manifesPenumpang.get(index)[2];
    }

    public static String getTipePenumpang(int index) {
        if (manifesPenumpang == null || index >= manifesPenumpang.size()) return null;
        return manifesPenumpang.get(index)[3];
    }

    public static int  getSisaDetikTimer() { 
        return sisaDetikTimer; 
    }
    
    public static void setSisaDetikTimer(int v) {
        sisaDetikTimer = v; 
    }
}