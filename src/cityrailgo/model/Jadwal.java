package cityrailgo.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Jadwal {

    public static final String STATUS_TERSEDIA   = "TERSEDIA";
    public static final String STATUS_PENUH      = "PENUH";
    public static final String STATUS_DIBATALKAN = "DIBATALKAN";
    public static final String STATUS_SELESAI    = "SELESAI";

    private int id;
    private Kereta kereta;
    private Rute rute;
    private LocalDateTime waktuBerangkat;
    private LocalDateTime waktuTiba;
    private double hargaBase;
    private String status;
    private List<Tiket> tiketList;

    public Jadwal() {
        this.status    = STATUS_TERSEDIA;
        this.tiketList = new ArrayList<>();
    }

    public Jadwal(int id, Kereta kereta, Rute rute,
                  LocalDateTime waktuBerangkat, LocalDateTime waktuTiba,
                  double hargaBase) {
        this.id              = id;
        this.kereta          = kereta;
        this.rute            = rute;
        this.waktuBerangkat  = waktuBerangkat;
        this.waktuTiba       = waktuTiba;
        this.hargaBase       = hargaBase;
        this.status          = STATUS_TERSEDIA;
        this.tiketList       = new ArrayList<>();
    }

    public Jadwal(int id, Kereta kereta, Rute rute,
                  LocalDateTime waktuBerangkat, LocalDateTime waktuTiba,
                  double hargaBase, String status) {
        this.id              = id;
        this.kereta          = kereta;
        this.rute            = rute;
        this.waktuBerangkat  = waktuBerangkat;
        this.waktuTiba       = waktuTiba;
        this.hargaBase       = hargaBase;
        this.status          = status;
        this.tiketList       = new ArrayList<>();
    }

    public List<Kursi> getKursiTersedia() {
        if (kereta == null) return new ArrayList<>();
        return kereta.getKursiTersedia(this);
    }

    public long hitungDurasi() {
        if (waktuBerangkat == null || waktuTiba == null) return -1;
        return Duration.between(waktuBerangkat, waktuTiba).toMinutes();
    }

    public String getDurasiFormatted() {
        long totalMenit = hitungDurasi();
        if (totalMenit < 0) return "-";
        long jam   = totalMenit / 60;
        long menit = totalMenit % 60;
        if (jam > 0) {
            return jam + "j " + menit + "m";
        }
        return menit + "m";
    }

    public boolean isBisaDipesan() {
        if (!STATUS_TERSEDIA.equals(status)) return false;
        if (waktuBerangkat == null) return false;
        if (LocalDateTime.now().isAfter(waktuBerangkat)) return false;
        return !getKursiTersedia().isEmpty();
    }

    public int getJumlahKursiTerisi() {
        if (tiketList == null) return 0;
        return (int) tiketList.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().isAktif())
                .count();
    }

    public String getInfoJadwal() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String namaKereta = kereta != null ? kereta.getNama() : "-";
        String infoRute   = rute   != null ? rute.getInfoRute() : "-";
        String berangkat  = waktuBerangkat != null ? waktuBerangkat.format(fmt) : "-";
        String tiba       = waktuTiba      != null ? waktuTiba.format(fmt)      : "-";
        return namaKereta + " | " + infoRute + " | " + berangkat + " → " + tiba
                + " (" + getDurasiFormatted() + ")";
    }

    public void updateStatusOtomatis() {
        if (STATUS_DIBATALKAN.equals(status) || STATUS_SELESAI.equals(status)) return;
        if (getKursiTersedia().isEmpty()) {
            this.status = STATUS_PENUH;
        } else {
            this.status = STATUS_TERSEDIA;
        }
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Kereta getKereta() {
        return kereta;
    }

    public void setKereta(Kereta kereta) {
        this.kereta = kereta;
    }

    public Rute getRute() {
        return rute;
    }

    public void setRute(Rute rute) {
        this.rute = rute;
    }

    public LocalDateTime getWaktuBerangkat() {
        return waktuBerangkat;
    }

    public void setWaktuBerangkat(LocalDateTime waktuBerangkat) {
        this.waktuBerangkat = waktuBerangkat;
    }

    public LocalDateTime getWaktuTiba() {
        return waktuTiba;
    }

    public void setWaktuTiba(LocalDateTime waktuTiba) {
        this.waktuTiba = waktuTiba;
    }

    public double getHargaBase() {
        return hargaBase;
    }

    public void setHargaBase(double hargaBase) {
        this.hargaBase = hargaBase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Tiket> getTiketList() {
        return tiketList;
    }

    public void setTiketList(List<Tiket> tiketList) {
        this.tiketList = tiketList != null ? tiketList : new ArrayList<>();
    }

    public void addTiket(Tiket tiket) {
        this.tiketList.add(tiket);
        updateStatusOtomatis();
    }

    @Override
    public String toString() {
        return "Jadwal{" +
                "id=" + id +
                ", kereta=" + (kereta != null ? kereta.getNama() : "-") +
                ", rute=" + (rute != null ? rute.getInfoRute() : "-") +
                ", waktuBerangkat=" + waktuBerangkat +
                ", status='" + status + '\'' +
                ", hargaBase=" + String.format("%.0f", hargaBase) +
                '}';
    }
}
