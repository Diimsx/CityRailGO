package cityrailgo.model;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Jadwal {
    public static final String STATUS_TERSEDIA = "TERSEDIA";
    public static final String STATUS_PENUH = "PENUH";
    public static final String STATUS_DIBATALKAN = "DIBATALKAN";
    public static final String STATUS_SELESAI = "SELESAI";

    private int id;
    private Kereta kereta; // Dibuat oleh temanmu
    private Rute rute;
    private LocalDateTime waktuBerangkat;
    private LocalDateTime waktuTiba;
    private double hargaBase;
    private String status;
    private List<Tiket> tiketList;

    public Jadwal() {
        this.tiketList = new ArrayList<>();
        this.status = STATUS_TERSEDIA;
    }

    public Jadwal(int id, Kereta kereta, Rute rute, LocalDateTime waktuBerangkat, LocalDateTime waktuTiba, double hargaBase, String status, List<Tiket> tiketList) {
        this.id = id;
        this.kereta = kereta;
        this.rute = rute;
        this.waktuBerangkat = waktuBerangkat;
        this.waktuTiba = waktuTiba;
        this.hargaBase = hargaBase;
        this.status = status;
        this.tiketList = (tiketList != null) ? tiketList : new ArrayList<>();
    }

    public List<Kursi> getKursiTersedia() {
        List<Kursi> kursiKosong = new ArrayList<>();
        if (kereta != null && kereta.getKursiList() != null) {
            for (Kursi kursi : kereta.getKursiList()) {
                boolean sudahDipesan = false;
                for (Tiket tiket : tiketList) {
                    if (tiket.getKursi() != null && tiket.getKursi().getId() == kursi.getId()) {
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
        if (waktuBerangkat != null && waktuTiba != null) {
            return Duration.between(waktuBerangkat, waktuTiba).toMinutes();
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
        return STATUS_TERSEDIA.equals(status) && LocalDateTime.now().isBefore(waktuBerangkat);
    }

    public int getJumlahKursiTerisi() {
        return tiketList.size();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Kereta getKereta() { return kereta; }
    public void setKereta(Kereta kereta) { this.kereta = kereta; }

    public Rute getRute() { return rute; }
    public void setRute(Rute rute) { this.rute = rute; }

    public LocalDateTime getWaktuBerangkat() { return waktuBerangkat; }
    public void setWaktuBerangkat(LocalDateTime waktuBerangkat) { this.waktuBerangkat = waktuBerangkat; }

    public LocalDateTime getWaktuTiba() { return waktuTiba; }
    public void setWaktuTiba(LocalDateTime waktuTiba) { this.waktuTiba = waktuTiba; }

    public double getHargaBase() { return hargaBase; }
    public void setHargaBase(double hargaBase) { this.hargaBase = hargaBase; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Tiket> getTiketList() { return tiketList; }
    public void setTiketList(List<Tiket> tiketList) { this.tiketList = tiketList; }

    @Override
    public String toString() {
        return "Jadwal{" + "id=" + id + ", status='" + status + '\'' + '}';
    }
}