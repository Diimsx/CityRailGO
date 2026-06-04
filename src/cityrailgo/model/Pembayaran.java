package cityrailgo.model;

import java.time.LocalDateTime;

public class Pembayaran {

    public static final String METODE_TRANSFER = "Transfer";
    public static final String METODE_DOMPET = "E-Wallet";

    private int id;
    private Tiket tiket;
    private double jumlahBayar;
    private String metodePembayaran;
    private StatusPembayaran statusPembayaran;
    private LocalDateTime tanggalBayar;

    public Pembayaran(int id, Tiket tiket,
                      double jumlahBayar,
                      String metodePembayaran) {

        this.id = id;
        this.tiket = tiket;
        this.jumlahBayar = jumlahBayar;
        this.metodePembayaran = metodePembayaran;

        this.statusPembayaran = StatusPembayaran.PENDING;
        this.tanggalBayar = LocalDateTime.now();
    }

    public boolean proses() {
        statusPembayaran = StatusPembayaran.SUKSES;
        return true;
    }
    
    public boolean verifikasi() {
        return statusPembayaran == StatusPembayaran.SUKSES;
    }

    public String generateBukti() {
        return "=== BUKTI PEMBAYARAN ===\n" +
               "ID Pembayaran : " + id +
               "\nMetode : " + metodePembayaran +
               "\nJumlah : Rp" + jumlahBayar +
               "\nStatus : " + statusPembayaran;
    }

    public int getId() {
        return id;
    }

    public Tiket getTiket() {
        return tiket;
    }

    public double getJumlahBayar() {
        return jumlahBayar;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public StatusPembayaran getStatusPembayaran() {
        return statusPembayaran;
    }

    public LocalDateTime getTanggalBayar() {
        return tanggalBayar;
    }
}
