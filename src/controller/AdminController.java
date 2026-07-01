package controller;

import dao.JadwalDAO;
import dao.KeretaDAO;
import dao.KursiDAO;
import dao.PembayaranDAO;
import dao.PromoDAO;
import dao.RuteDAO;
import dao.StasiunDAO;
import dao.TiketDAO;
import model.Jadwal;
import model.Kereta;
import model.Kursi;
import model.Pembayaran;
import model.Promo;
import model.Rute;
import model.Stasiun;
import model.Tiket;

import java.util.List;
import model.JenisKelas;

public class AdminController {

    private KeretaDAO keretaDAO;
    private RuteDAO ruteDAO;
    private StasiunDAO stasiunDAO;
    private JadwalDAO jadwalDAO;
    private PembayaranDAO pembayaranDAO;
    private TiketDAO tiketDAO;
    private PromoDAO promoDAO;
    private KursiDAO kursiDAO;

    public AdminController() {
        this.keretaDAO = new KeretaDAO();
        this.ruteDAO = new RuteDAO();
        this.stasiunDAO = new StasiunDAO();
        this.jadwalDAO = new JadwalDAO();
        this.pembayaranDAO = new PembayaranDAO();
        this.tiketDAO = new TiketDAO();
        this.promoDAO = new PromoDAO();
        this.kursiDAO = new KursiDAO();
    }

    public boolean tambahKereta(Kereta kereta) {
        return keretaDAO.save(kereta);
    }

    public boolean editKereta(Kereta kereta) {
        return keretaDAO.update(kereta);
    }

    public boolean hapusKereta(int id) {
        return keretaDAO.delete(id);
    }

    public String validateKereta(Kereta kereta) {
        if (kereta.getNama() == null || kereta.getNama().trim().isEmpty()) {
            return "Nama kereta tidak boleh kosong.";
        }
        if (kereta.getNomorKereta() == null || kereta.getNomorKereta().trim().isEmpty()) {
            return "Nomor kereta tidak boleh kosong.";
        }
        if (kereta.getKapasitasTotal() <= 0) {
            return "Kapasitas total harus lebih dari 0.";
        }
        if (kereta.getJumlahGerbong() <= 0) {
            return "Jumlah gerbong harus lebih dari 0.";
        }
        return null;
    }

    public String validateRute(Rute rute) {
        if (rute == null) {
            return "Data rute tidak valid.";
        }
        return null;
    }

    public String validateJadwal(Jadwal jadwal) {
        if (jadwal == null) {
            return "Data jadwal tidak valid.";
        }
        if (jadwal.getKereta() == null) {
            return "Pilih kereta untuk jadwal ini.";
        }
        if (jadwal.getRute() == null) {
            return "Pilih rute untuk jadwal ini.";
        }
        if (jadwal.getWaktuBerangkat() == null) {
            return "Tentukan waktu keberangkatan.";
        }
        if (jadwal.getWaktuTiba() == null) {
            return "Tentukan waktu tiba.";
        }
        if (jadwal.getWaktuTiba().isBefore(jadwal.getWaktuBerangkat())) {
            return "Waktu tiba tidak boleh lebih awal dari waktu berangkat.";
        }
        return null;
    }

    public String validatePromo(Promo promo) {
        if (promo == null || promo.getKodePromo() == null || promo.getKodePromo().trim().isEmpty()) {
            return "Kode promo tidak boleh kosong.";
        }
        if (promo.getDiskonPersen() <= 0 || promo.getDiskonPersen() > 100) {
            return "Diskon harus antara 0 dan 100 persen.";
        }
        if (promo.getTanggalBerakhir().isBefore(promo.getTanggalMulai())) {
            return "Tanggal berakhir tidak boleh sebelum tanggal mulai.";
        }
        return null;
    }

    public boolean tambahRute(Rute rute) {
        return ruteDAO.save(rute);
    }

    public boolean editRute(Rute rute) {
        return ruteDAO.update(rute);
    }

    public boolean hapusRute(int id) {
        return ruteDAO.delete(id);
    }

    public boolean tambahJadwal(Jadwal jadwal) {
        boolean berhasil = jadwalDAO.save(jadwal);
        if (berhasil) {
            buatKursiUntukJadwal(jadwal);
        }
        return berhasil;
    }

    public boolean editJadwal(Jadwal jadwal) {
        return jadwalDAO.update(jadwal);
    }

    public boolean hapusJadwal(int id) {
        return jadwalDAO.delete(id);
    }

    public boolean tambahPromo(Promo promo) {
        return promoDAO.save(promo);
    }

    public boolean editPromo(Promo promo) {
        return promoDAO.update(promo);
    }

    public boolean hapusPromo(int id) {
        return promoDAO.delete(id);
    }

    public boolean validasiPembayaran(Pembayaran pembayaran) {
        pembayaran.setStatus("LUNAS");
        return pembayaranDAO.update(pembayaran);
    }

    public List<Tiket> getLaporanPenjualan() {
        return tiketDAO.findAll();
    }

    private void buatKursiUntukJadwal(Jadwal jadwal) {
        JenisKelas jenisKelas = jadwal.getJenisKelas();
        Kereta kereta = jadwal.getKereta();

        int kapasitasJadwal = 0;
        String kelas = jenisKelas.getNamaKelas().toLowerCase();

        if (kelas.contains("eksekutif")) {
            kapasitasJadwal = kereta.getGerbongEksekutif() * 50;
        } else if (kelas.contains("bisnis")) {
            kapasitasJadwal = kereta.getGerbongBisnis() * 60;
        } else {
            kapasitasJadwal = kereta.getGerbongEkonomi() * 80;
        }

        if (kapasitasJadwal == 0) {
            kapasitasJadwal = kereta.getKapasitasTotal();
        }

        for (int i = 1; i <= kapasitasJadwal; i++) {
            String nomorKursi = String.format("%03d", i);
            Kursi kursi = new Kursi(jadwal, jenisKelas, nomorKursi);
            kursiDAO.save(kursi);
        }
    }
}