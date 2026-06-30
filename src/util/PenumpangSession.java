package util;

import model.Jadwal;
import model.Kursi;
import java.time.LocalDate;

/**
 * Shared session state untuk alur pemesanan penumpang.
 * Data tersimpan selama proses: Cari -> PilihJadwal -> PilihKursi -> Bayar -> Selesai
 */
public class PenumpangSession {

    private static String stasiunAsal;
    private static String stasiunTujuan;
    private static LocalDate tanggalBerangkat;
    private static int jumlahPenumpang = 1;

    private static Jadwal jadwalDipilih;
    private static Kursi  kursiDipilih;

    public static void reset() {
        stasiunAsal      = null;
        stasiunTujuan    = null;
        tanggalBerangkat = null;
        jumlahPenumpang  = 1;
        jadwalDipilih    = null;
        kursiDipilih     = null;
    }

    // ---- Getters & Setters ----

    public static String getStasiunAsal()                { return stasiunAsal; }
    public static void   setStasiunAsal(String v)        { stasiunAsal = v; }

    public static String getStasiunTujuan()              { return stasiunTujuan; }
    public static void   setStasiunTujuan(String v)      { stasiunTujuan = v; }

    public static LocalDate getTanggalBerangkat()        { return tanggalBerangkat; }
    public static void      setTanggalBerangkat(LocalDate v) { tanggalBerangkat = v; }

    public static int  getJumlahPenumpang()              { return jumlahPenumpang; }
    public static void setJumlahPenumpang(int v)         { jumlahPenumpang = v; }

    public static Jadwal getJadwalDipilih()              { return jadwalDipilih; }
    public static void   setJadwalDipilih(Jadwal v)      { jadwalDipilih = v; }

    public static Kursi getKursiDipilih()                { return kursiDipilih; }
    public static void  setKursiDipilih(Kursi v)         { kursiDipilih = v; }
}
