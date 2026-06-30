package controller;

import dao.JadwalDAO;
import dao.KeretaDAO;
import dao.KursiDAO;
import dao.PembayaranDAO;
import dao.PerhentianDAO;
import dao.PromoDAO;
import dao.RuteDAO;
import dao.StasiunDAO;
import dao.TiketDAO;
import model.Jadwal;
import model.JenisKelas;
import model.Kereta;
import model.Kursi;
import model.Pembayaran;
import model.Perhentian;
import model.Promo;
import model.Rute;
import model.Stasiun;
import model.Tiket;

import java.util.List;

public class AdminController {

    private KeretaDAO keretaDAO;
    private RuteDAO ruteDAO;
    private StasiunDAO stasiunDAO;
    private PerhentianDAO perhentianDAO;
    private JadwalDAO jadwalDAO;
    private PembayaranDAO pembayaranDAO;
    private TiketDAO tiketDAO;
    private PromoDAO promoDAO;
    private KursiDAO kursiDAO;

    public AdminController() {
        this.keretaDAO = new KeretaDAO();
        this.ruteDAO = new RuteDAO();
        this.stasiunDAO = new StasiunDAO();
        this.perhentianDAO = new PerhentianDAO();
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

    // ===== Validation methods =====
    public String validateTambahKereta(Kereta kereta) {
        if (kereta.getNama().trim().isEmpty()) {
            return "Nama kereta tidak boleh kosong.";
        }
        if (kereta.getNomorKereta().trim().isEmpty()) {
            return "Nomor kereta tidak boleh kosong.";
        }
        if (kereta.getKapasitasTotal() <= 0) {
            return "Kapasitas total harus lebih dari 0.";
        }
        if (keretaDAO.nomorKeretaExists(kereta.getNomorKereta())) {
            return "Nomor kereta '" + kereta.getNomorKereta() + "' sudah terdaftar.";
        }
        if (kereta.getJenisKelasIds() == null || kereta.getJenisKelasIds().isEmpty()) {
            return "Pilih minimal satu jenis kelas untuk kereta ini.";
        }
        return null;  // Valid
    }

    public String validateEditKereta(Kereta kereta) {
        if (kereta.getNama().trim().isEmpty()) {
            return "Nama kereta tidak boleh kosong.";
        }
        if (kereta.getNomorKereta().trim().isEmpty()) {
            return "Nomor kereta tidak boleh kosong.";
        }
        if (kereta.getKapasitasTotal() <= 0) {
            return "Kapasitas total harus lebih dari 0.";
        }
        if (keretaDAO.nomorKeretaExistsExcept(kereta.getNomorKereta(), kereta.getId())) {
            return "Nomor kereta '" + kereta.getNomorKereta() + "' sudah digunakan kereta lain.";
        }
        if (kereta.getJenisKelasIds() == null || kereta.getJenisKelasIds().isEmpty()) {
            return "Pilih minimal satu jenis kelas untuk kereta ini.";
        }
        return null;  // Valid
    }

    public String validateHapusKereta(int keretaId) {
        if (keretaDAO.hasActiveSchedules(keretaId)) {
            String scheduleInfo = keretaDAO.getActiveSchedulesInfo(keretaId);
            return "Kereta ini masih memiliki jadwal aktif:\n\n" + scheduleInfo +
                    "\n\nSilakan ubah status kereta menjadi 'NON-AKTIF' terlebih dahulu,\n" +
                    "atau hapus/tunda jadwal-jadwal di atas.";
        }
        return null;  // Can delete
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

    public List<Perhentian> getPerhentianByRute(int ruteId) {
        return perhentianDAO.findByRuteId(ruteId);
    }

    public boolean tambahPerhentian(Perhentian perhentian) {
        return perhentianDAO.save(perhentian);
    }

    public boolean hapusPerhentian(int id) {
        return perhentianDAO.delete(id);
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
        int kapasitas = jadwal.getKereta().getKapasitasTotal();

        for (int i = 1; i <= kapasitas; i++) {
            String nomorKursi = String.format("%03d", i);
            Kursi kursi = new Kursi(jadwal, jenisKelas, nomorKursi);
            kursiDAO.save(kursi);
        }
    }
}