package dao;

import model.JenisKelas;
import model.Jadwal;
import model.Kereta;
import model.Kursi;
import model.Pembayaran;
import model.Penumpang;
import model.Promo;
import model.Rute;
import model.Tiket;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PembayaranDAO {

    private static final String SQL_SELECT_BASE =
            "SELECT pb.id AS pembayaran_id, pb.jumlah_bayar, pb.metode_pembayaran, " +
            "pb.status AS pembayaran_status, pb.tanggal_bayar, " +
            "t.id AS tiket_id, t.kode_tiket, t.harga_total, t.status AS tiket_status, " +
            "u.id AS penumpang_id, u.username, u.password, u.email, u.nama_lengkap, u.no_telepon, " +
            "p.nik, p.tgl_lahir AS penumpang_tgl_lahir, p.jenis_kelamin AS penumpang_jenis_kelamin, " +
            "j.id AS jadwal_id, j.waktu_berangkat, j.waktu_tiba, j.status AS jadwal_status, " +
            "k.id AS kereta_id, k.nama AS kereta_nama, k.nomor_kereta, k.jenis AS kereta_jenis, k.kapasitas_total, " +
            "r.id AS rute_id, r.stasiun_asal, r.stasiun_tujuan, r.jarak_km, r.estimasi_menit, " +
            "jk.id AS jenis_kelas_id, jk.nama_kelas, jk.harga_per_km, jk.fasilitas, " +
            "ku.id AS kursi_id, ku.nomor_kursi, ku.status AS kursi_status, " +
            "pr.id AS promo_id, pr.kode_promo, pr.deskripsi, pr.diskon_persen, " +
            "pr.tanggal_mulai, pr.tanggal_berakhir, pr.aktif " +
            "FROM pembayaran pb " +
            "JOIN tiket t ON pb.tiket_id = t.id " +
            "JOIN penumpang p ON t.penumpang_id = p.id " +
            "JOIN users u ON p.id = u.id " +
            "JOIN jadwal j ON t.jadwal_id = j.id " +
            "JOIN kereta k ON j.kereta_id = k.id " +
            "JOIN rute r ON j.rute_id = r.id " +
            "JOIN jenis_kelas jk ON j.jenis_kelas_id = jk.id " +
            "JOIN kursi ku ON t.kursi_id = ku.id " +
            "LEFT JOIN promo pr ON pb.promo_id = pr.id ";

    public Pembayaran findById(int id) {
        String sql = SQL_SELECT_BASE + "WHERE pb.id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildPembayaranFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil pembayaran (findById): " + e.getMessage());
        }
        return null;
    }

    public Pembayaran findByTiket(Tiket tiket) {
        String sql = SQL_SELECT_BASE + "WHERE pb.tiket_id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tiket.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildPembayaranFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil pembayaran (findByTiket): " + e.getMessage());
        }
        return null;
    }

    public List<Pembayaran> findAll() {
        List<Pembayaran> daftarPembayaran = new ArrayList<>();
        String sql = SQL_SELECT_BASE + "ORDER BY pb.tanggal_bayar DESC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarPembayaran.add(buildPembayaranFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua pembayaran: " + e.getMessage());
        }
        return daftarPembayaran;
    }

    public boolean save(Pembayaran pembayaran) {
        String sql = "INSERT INTO pembayaran (tiket_id, promo_id, jumlah_bayar, metode_pembayaran, status, tanggal_bayar) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pembayaran.getTiket().getId());
            if (pembayaran.getPromo() != null) {
                ps.setInt(2, pembayaran.getPromo().getId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDouble(3, pembayaran.getJumlahBayar());
            ps.setString(4, pembayaran.getMetodePembayaran());
            ps.setString(5, pembayaran.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(pembayaran.getTanggalBayar()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pembayaran.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan pembayaran: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Pembayaran pembayaran) {
        String sql = "UPDATE pembayaran SET status=?, promo_id=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pembayaran.getStatus());
            if (pembayaran.getPromo() != null) {
                ps.setInt(2, pembayaran.getPromo().getId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, pembayaran.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui pembayaran: " + e.getMessage());
            return false;
        }
    }

    private Pembayaran buildPembayaranFromResultSet(ResultSet rs) throws SQLException {
        Penumpang penumpang = new Penumpang(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("nama_lengkap"),
                rs.getString("no_telepon"),
                rs.getString("nik"),
                toLocalDate(rs.getDate("penumpang_tgl_lahir")),
                rs.getString("penumpang_jenis_kelamin")
        );
        penumpang.setId(rs.getInt("penumpang_id"));

        Kereta kereta = new Kereta(
                rs.getString("kereta_nama"),
                rs.getString("nomor_kereta"),
                rs.getString("kereta_jenis"),
                rs.getInt("kapasitas_total")
        );
        kereta.setId(rs.getInt("kereta_id"));

        Rute rute = new Rute(
                rs.getString("stasiun_asal"),
                rs.getString("stasiun_tujuan"),
                rs.getDouble("jarak_km"),
                rs.getInt("estimasi_menit")
        );
        rute.setId(rs.getInt("rute_id"));

        JenisKelas jenisKelas = new JenisKelas(
                rs.getString("nama_kelas"),
                rs.getDouble("harga_per_km"),
                rs.getString("fasilitas")
        );
        jenisKelas.setId(rs.getInt("jenis_kelas_id"));

        Jadwal jadwal = new Jadwal(
                kereta, rute, jenisKelas,
                rs.getTimestamp("waktu_berangkat").toLocalDateTime(),
                rs.getTimestamp("waktu_tiba").toLocalDateTime()
        );
        jadwal.setId(rs.getInt("jadwal_id"));
        jadwal.setStatus(rs.getString("jadwal_status"));

        Kursi kursi = new Kursi(jadwal, jenisKelas, rs.getString("nomor_kursi"));
        kursi.setId(rs.getInt("kursi_id"));
        kursi.setStatus(rs.getString("kursi_status"));

        Tiket tiket = new Tiket(penumpang, jadwal, kursi, rs.getDouble("harga_total"));
        tiket.setId(rs.getInt("tiket_id"));
        tiket.setKodeTiket(rs.getString("kode_tiket"));
        tiket.setStatus(rs.getString("tiket_status"));

        Pembayaran pembayaran = new Pembayaran(tiket, rs.getDouble("jumlah_bayar"), rs.getString("metode_pembayaran"));
        pembayaran.setId(rs.getInt("pembayaran_id"));
        pembayaran.setStatus(rs.getString("pembayaran_status"));
        pembayaran.setTanggalBayar(rs.getTimestamp("tanggal_bayar").toLocalDateTime());

        int promoId = rs.getInt("promo_id");
        if (!rs.wasNull()) {
            Promo promo = new Promo(
                    rs.getString("kode_promo"),
                    rs.getString("deskripsi"),
                    rs.getDouble("diskon_persen"),
                    rs.getDate("tanggal_mulai").toLocalDate(),
                    rs.getDate("tanggal_berakhir").toLocalDate()
            );
            promo.setId(promoId);
            promo.setAktif(rs.getBoolean("aktif"));
            pembayaran.setPromo(promo);
        }

        return pembayaran;
    }

    private LocalDate toLocalDate(Date sqlDate) {
        return (sqlDate != null) ? sqlDate.toLocalDate() : null;
    }
}