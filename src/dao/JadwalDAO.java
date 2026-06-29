package dao;

import model.JenisKelas;
import model.Jadwal;
import model.Kereta;
import model.Rute;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JadwalDAO {

    private static final String SQL_SELECT_BASE =
            "SELECT j.id, j.waktu_berangkat, j.waktu_tiba, j.status, " +
            "k.id AS kereta_id, k.nama AS kereta_nama, k.nomor_kereta, k.jenis AS kereta_jenis, k.kapasitas_total, " +
            "r.id AS rute_id, r.stasiun_asal, r.stasiun_tujuan, r.jarak_km, r.estimasi_menit, " +
            "jk.id AS jenis_kelas_id, jk.nama_kelas, jk.harga_per_km, jk.fasilitas " +
            "FROM jadwal j " +
            "JOIN kereta k ON j.kereta_id = k.id " +
            "JOIN rute r ON j.rute_id = r.id " +
            "JOIN jenis_kelas jk ON j.jenis_kelas_id = jk.id ";

    public Jadwal findById(int id) {
        String sql = SQL_SELECT_BASE + "WHERE j.id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildJadwalFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil jadwal (findById): " + e.getMessage());
        }
        return null;
    }

    public List<Jadwal> findAll() {
        List<Jadwal> daftarJadwal = new ArrayList<>();
        String sql = SQL_SELECT_BASE + "ORDER BY j.waktu_berangkat ASC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarJadwal.add(buildJadwalFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua jadwal: " + e.getMessage());
        }
        return daftarJadwal;
    }

    public List<Jadwal> findByRuteDanTanggal(String stasiunAsal, String stasiunTujuan, LocalDate tanggal) {
        List<Jadwal> daftarJadwal = new ArrayList<>();
        String sql = SQL_SELECT_BASE +
                "WHERE r.stasiun_asal = ? " +
                "AND r.stasiun_tujuan = ? " +
                "AND DATE(j.waktu_berangkat) = ? " +
                "AND j.status = 'TERSEDIA' " +
                "ORDER BY j.waktu_berangkat ASC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stasiunAsal);
            ps.setString(2, stasiunTujuan);
            ps.setDate(3, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftarJadwal.add(buildJadwalFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil jadwal (findByRuteDanTanggal): " + e.getMessage());
        }
        return daftarJadwal;
    }

    public boolean save(Jadwal jadwal) {
        String sql = "INSERT INTO jadwal (kereta_id, rute_id, jenis_kelas_id, waktu_berangkat, waktu_tiba, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, jadwal.getKereta().getId());
            ps.setInt(2, jadwal.getRute().getId());
            ps.setInt(3, jadwal.getJenisKelas().getId());
            ps.setTimestamp(4, Timestamp.valueOf(jadwal.getWaktuBerangkat()));
            ps.setTimestamp(5, Timestamp.valueOf(jadwal.getWaktuTiba()));
            ps.setString(6, jadwal.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    jadwal.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan jadwal: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Jadwal jadwal) {
        String sql = "UPDATE jadwal SET kereta_id=?, rute_id=?, jenis_kelas_id=?, waktu_berangkat=?, waktu_tiba=?, status=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jadwal.getKereta().getId());
            ps.setInt(2, jadwal.getRute().getId());
            ps.setInt(3, jadwal.getJenisKelas().getId());
            ps.setTimestamp(4, Timestamp.valueOf(jadwal.getWaktuBerangkat()));
            ps.setTimestamp(5, Timestamp.valueOf(jadwal.getWaktuTiba()));
            ps.setString(6, jadwal.getStatus());
            ps.setInt(7, jadwal.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui jadwal: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM jadwal WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal menghapus jadwal: " + e.getMessage());
            return false;
        }
    }

    private Jadwal buildJadwalFromResultSet(ResultSet rs) throws SQLException {
        Kereta kereta = new Kereta(
                rs.getString("kereta_nama"),
                rs.getString("nomor_kereta"),
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
                kereta,
                rute,
                jenisKelas,
                rs.getTimestamp("waktu_berangkat").toLocalDateTime(),
                rs.getTimestamp("waktu_tiba").toLocalDateTime()
        );
        jadwal.setId(rs.getInt("id"));
        jadwal.setStatus(rs.getString("status"));
        return jadwal;
    }
}