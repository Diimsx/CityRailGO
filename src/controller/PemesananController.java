package controller;

import dao.JadwalDAO;
import dao.KursiDAO;
import dao.PembayaranDAO;
import dao.PromoDAO;
import dao.TiketDAO;
import model.Jadwal;
import model.Kursi;
import model.Pembayaran;
import model.Penumpang;
import model.Promo;
import model.Tiket;
import util.TiketHelper;

import java.time.LocalDate;
import java.util.List;

public class PemesananController {

    private JadwalDAO jadwalDAO;
    private KursiDAO kursiDAO;
    private TiketDAO tiketDAO;
    private PembayaranDAO pembayaranDAO;
    private PromoDAO promoDAO;

    public PemesananController() {
        this.jadwalDAO = new JadwalDAO();
        this.kursiDAO = new KursiDAO();
        this.tiketDAO = new TiketDAO();
        this.pembayaranDAO = new PembayaranDAO();
        this.promoDAO = new PromoDAO();
    }

    public List<Jadwal> cariJadwal(String stasiunAsal, String stasiunTujuan, LocalDate tanggal) {
        return jadwalDAO.findByRuteDanTanggal(stasiunAsal, stasiunTujuan, tanggal);
    }

    public List<Kursi> getKursiTersedia(Jadwal jadwal) {
        return kursiDAO.findTersediaByJadwal(jadwal);
    }

    public double hitungHarga(Jadwal jadwal, Promo promo) {
        double jarakKm = jadwal.getRute().getJarakKm();
        double hargaBase = jadwal.getJenisKelas().hitungHarga(jarakKm);

        if (promo != null && promo.isValid()) {
            double diskon = promo.hitungDiskon(hargaBase);
            return hargaBase - diskon;
        }
        return hargaBase;
    }

    public Tiket buatTiket(Penumpang penumpang, Jadwal jadwal, Kursi kursi) {
        double hargaTotal = hitungHarga(jadwal, null);

        Tiket tiket = new Tiket(penumpang, jadwal, kursi, hargaTotal);
        tiket.setKodeTiket(TiketHelper.generateKodeTiket());

        kursi.setStatus("TERPESAN");
        kursiDAO.update(kursi);

        boolean berhasil = tiketDAO.save(tiket);
        if (berhasil) {
            return tiket;
        }
        kursi.setStatus("TERSEDIA");
        kursiDAO.update(kursi);
        return null;
    }

    public Pembayaran konfirmasiPembayaran(Tiket tiket, String metodePembayaran, Promo promo) {
        double hargaTotal = tiket.getHargaTotal();
        double jumlahBayar = hargaTotal;

        if (promo != null && promo.isValid()) {
            jumlahBayar = hargaTotal - promo.hitungDiskon(hargaTotal);
        }

        Pembayaran pembayaran = new Pembayaran(tiket, jumlahBayar, metodePembayaran);

        if (promo != null && promo.isValid()) {
            pembayaran.setPromo(promo);
        }

        pembayaran.setStatus("LUNAS");
        tiket.setStatus("AKTIF");
        tiketDAO.update(tiket);

        boolean berhasil = pembayaranDAO.save(pembayaran);
        if (berhasil) {
            return pembayaran;
        }
        return null;
    }

    public List<Tiket> getRiwayatTiket(Penumpang penumpang) {
        return tiketDAO.findByPenumpang(penumpang);
    }
}