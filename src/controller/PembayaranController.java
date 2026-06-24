package controller;

import dao.PembayaranDAO;
import model.Pembayaran;
import model.Pembayaran.MetodePembayaran;
import model.Pembayaran.StatusPembayaran;
import util.DatabaseUtil;
import util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PembayaranController - logika bisnis pembayaran tiket
 *
 * Sesuai class diagram:
 *   + proses(pemesananId, jumlah, metode, keterangan) : Pembayaran
 *   + verifikasi(pembayaranId) : boolean
 *   + batalkan(pembayaranId) : boolean
 *   + getByPemesanan(pemesananId) : Pembayaran
 *   + getStatus(pembayaranId) : StatusPembayaran
 */
public class PembayaranController {

    private final PembayaranDAO pembayaranDAO;

    public PembayaranController() {
        this.pembayaranDAO = new PembayaranDAO();
    }

    // ── PROSES PEMBAYARAN ─────────────────────────────────────────
    /**
     * Memproses pembayaran baru untuk sebuah pemesanan.
     * Melempar IllegalArgumentException jika validasi gagal.
     */
    public Pembayaran proses(int pemesananId, double jumlah,
                              MetodePembayaran metode, String keterangan) {

        // Validasi input
        if (pemesananId <= 0)
            throw new IllegalArgumentException("ID pemesanan tidak valid.");
        if (jumlah <= 0)
            throw new IllegalArgumentException("Jumlah pembayaran harus lebih dari 0.");
        if (metode == null)
            throw new IllegalArgumentException("Metode pembayaran wajib dipilih.");

        // Cek apakah pemesanan sudah pernah dibayar
        Pembayaran existing = pembayaranDAO.findByPemesananId(pemesananId);
        if (existing != null && existing.isSukses()) {
            throw new IllegalStateException("Pemesanan ini sudah dibayar.");
        }

        // Buat objek pembayaran baru
        Pembayaran pembayaran = new Pembayaran(pemesananId, jumlah, metode, keterangan);

        // Simpan ke database
        boolean berhasil = pembayaranDAO.insert(pembayaran);
        if (!berhasil) {
            throw new RuntimeException("Gagal menyimpan data pembayaran ke database.");
        }

        // Simulasi gateway pembayaran (untuk metode non-tunai)
        if (metode != MetodePembayaran.TUNAI) {
            boolean sukses = simulasiGateway(pembayaran);
            if (sukses) {
                pembayaran.setStatus(StatusPembayaran.SUKSES);
                updateStatusPemesanan(pemesananId, "LUNAS");
            } else {
                pembayaran.setStatus(StatusPembayaran.GAGAL);
            }
            pembayaranDAO.updateStatus(pembayaran.getId(), pembayaran.getStatus());
        }

        return pembayaran;
    }

    // ── VERIFIKASI ────────────────────────────────────────────────
    /**
     * Verifikasi manual pembayaran (misal untuk metode TUNAI oleh admin).
     */
    public boolean verifikasi(int pembayaranId) {
        Pembayaran p = pembayaranDAO.findById(pembayaranId);
        if (p == null)
            throw new IllegalArgumentException("Data pembayaran tidak ditemukan.");
        if (p.isSukses())
            throw new IllegalStateException("Pembayaran sudah berstatus SUKSES.");
        if (p.isKadaluarsa())
            throw new IllegalStateException("Pembayaran telah kadaluarsa.");

        boolean berhasil = pembayaranDAO.verifikasi(pembayaranId);
        if (berhasil) {
            updateStatusPemesanan(p.getPemesananId(), "LUNAS");
        }
        return berhasil;
    }

    // ── BATALKAN ──────────────────────────────────────────────────
    /**
     * Membatalkan pembayaran yang masih berstatus MENUNGGU.
     */
    public boolean batalkan(int pembayaranId) {
        Pembayaran p = pembayaranDAO.findById(pembayaranId);
        if (p == null)
            throw new IllegalArgumentException("Data pembayaran tidak ditemukan.");
        if (p.getStatus() == StatusPembayaran.SUKSES)
            throw new IllegalStateException("Pembayaran yang sudah sukses tidak bisa dibatalkan.");
        if (p.getStatus() == StatusPembayaran.DIBATALKAN)
            throw new IllegalStateException("Pembayaran sudah dibatalkan sebelumnya.");

        return pembayaranDAO.batalkan(pembayaranId);
    }

    // ── GET BY PEMESANAN ──────────────────────────────────────────
    public Pembayaran getByPemesanan(int pemesananId) {
        return pembayaranDAO.findByPemesananId(pemesananId);
    }

    // ── GET STATUS ────────────────────────────────────────────────
    public StatusPembayaran getStatus(int pembayaranId) {
        Pembayaran p = pembayaranDAO.findById(pembayaranId);
        if (p == null) return null;
        return p.getStatus();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────

    /**
     * Simulasi gateway pembayaran eksternal.
     * Di produksi, ganti dengan integrasi API Midtrans/Xendit/dll.
     */
    private boolean simulasiGateway(Pembayaran pembayaran) {
        try {
            Thread.sleep(500); // simulasi latency network
        } catch (InterruptedException ignored) {}
        // 90% sukses untuk simulasi
        return Math.random() < 0.9;
    }

    /**
     * Update status pemesanan setelah pembayaran sukses.
     */
    private void updateStatusPemesanan(int pemesananId, String status) {
        String sql = "UPDATE pemesanan SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, pemesananId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PembayaranController] updateStatusPemesanan gagal: "
                    + e.getMessage());
        }
    }
}