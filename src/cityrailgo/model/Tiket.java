package cityrailgo.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Tiket {
    private final IntegerProperty id;
    private final ObjectProperty<Jadwal> jadwal;
    private final ObjectProperty<Penumpang> penumpang; 
    private final ObjectProperty<Object> kursi;       
    private final ObjectProperty<Promo> promo;          
    private final StringProperty kodeTiket;
    private final DoubleProperty hargaAkhir;
    private final ObjectProperty<StatusTiket> status;  
    private final ObjectProperty<LocalDateTime> tanggalPesan;

    // --- CONSTRUCTOR ---
    public Tiket(int id, Jadwal jadwal, Penumpang penumpang, Object kursi, Promo promo, String kodeTiket, double hargaAkhir, StatusTiket status, LocalDateTime tanggalPesan) {
        this.id = new SimpleIntegerProperty(id);
        this.jadwal = new SimpleObjectProperty<>(jadwal);
        this.penumpang = new SimpleObjectProperty<>(penumpang);
        this.kursi = new SimpleObjectProperty<>(kursi);
        this.promo = new SimpleObjectProperty<>(promo);
        this.kodeTiket = new SimpleStringProperty(kodeTiket);
        this.hargaAkhir = new SimpleDoubleProperty(hargaAkhir);
        this.status = new SimpleObjectProperty<>(status);
        this.tanggalPesan = new SimpleObjectProperty<>(tanggalPesan);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }

    public Jadwal getJadwal() { return jadwal.get(); }
    public void setJadwal(Jadwal value) { jadwal.set(value); }

    public Penumpang getPenumpang() { return penumpang.get(); }
    public void setPenumpang(Penumpang value) { penumpang.set(value); }

    public Object getKursi() { return kursi.get(); }
    public void setKursi(Object value) { kursi.set(value); }

    public Promo getPromo() { return promo.get(); }
    public void setPromo(Promo value) { promo.set(value); }

    public String getKodeTiket() { return kodeTiket.get(); }
    public void setKodeTiket(String value) { kodeTiket.set(value); }

    public double getHargaAkhir() { return hargaAkhir.get(); }
    public void setHargaAkhir(double value) { hargaAkhir.set(value); }

    public StatusTiket getStatus() { return status.get(); }
    public void setStatus(StatusTiket value) { status.set(value); }

    public LocalDateTime getTanggalPesan() { return tanggalPesan.get(); }
    public void setTanggalPesan(LocalDateTime value) { tanggalPesan.set(value); }

    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<Jadwal> jadwalProperty() { return jadwal; }
    public ObjectProperty<Penumpang> penumpangProperty() { return penumpang; }
    public ObjectProperty<Object> kursiProperty() { return kursi; }
    public ObjectProperty<Promo> promoProperty() { return promo; }
    public StringProperty kodeTiketProperty() { return kodeTiket; }
    public DoubleProperty hargaAkhirProperty() { return hargaAkhir; }
    public ObjectProperty<StatusTiket> statusProperty() { return status; }
    public ObjectProperty<LocalDateTime> tanggalPesanProperty() { return tanggalPesan; }


    public String generateKodeTiket() {
        int randomNum = (int)(Math.random() * 90000) + 10000;
        String kode = "CRG-" + randomNum;
        setKodeTiket(kode);
        return kode;
    }

    public double hitungHargaAkhir() {
        double harga = 0.0;
        
        if (getJadwal() != null) {
            harga = getJadwal().getHargaBase();
        }
        
        if (getPromo() != null) {
            double diskon = getPromo().hitungDiskon(harga);
            harga = harga - diskon;
        }
        
        setHargaAkhir(harga);
        return harga;
    }

    public void batalkan() 
        setStatus(StatusTiket.BATAL); 
    }

    public String getInfoTiket() {
        String namaPenumpang = (getPenumpang() != null) ? getPenumpang().getNama() : "Tanpa Nama";
        String namaKereta = (getJadwal() != null && getJadwal().getKereta() != null) ? getJadwal().getKereta().getNama() : "-";
        
        return "Tiket: " + getKodeTiket() + " | Penumpang: " + namaPenumpang + " | Kereta: " + namaKereta + " | Total: Rp" + getHargaAkhir();
    }
}