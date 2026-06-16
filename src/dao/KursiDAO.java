package dao;

import model.JenisKelas;
import model.Jadwal;
import model.Kereta;
import model.Kursi;
import model.Rute;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class KursiDAO {

    private static final String SQL_SELECT_BASE =
            "SELECT ku.id AS kursi_id, ku.nomor_kursi, ku.status AS kursi_status, " +
            "j.id AS jadwal_id, j.waktu_berangkat, j.waktu_tiba, j.status AS jadwal_status, " +
            "k.id AS kereta_id, k.nama AS kereta_nama, k.nomor_kereta, k.jenis AS kereta_jenis, k.kapasitas_total, " +
            "r.id AS rute_id, r.stasiun_asal, r.stasiun_tujuan, r.jarak_km, r.estimasi_menit, " +
            "jk.id AS jenis_kelas_id, jk.nama_kelas, jk.harga_per_km, jk.fasilitas " +
            "FROM kursi ku " +
            "JOIN jadwal j ON ku.jadwal_id = j.id " +
            "JOIN kereta k ON j.kereta_id = k.id " +
            "JOIN rute r ON j.rute_id = r.id " +
            "JOIN jenis_kelas jk ON ku.jenis_kelas_id = jk.id ";

    public Kursi findById(int id) {
        String sql = SQL_SELECT_BASE + "WHERE ku.id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildKursiFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil kursi (findById): " + e.getMessage());
        }
        return null;
    }

    public List<Kursi> findByJadwal(Jadwal jadwal) {
        List<Kursi> daftarKursi = new ArrayList<>();
        String sql = SQL_SELECT_BASE +
                "WHERE ku.jadwal_id = ? " +
                "ORDER BY ku.nomor_kursi ASC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jadwal.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftarKursi.add(buildKursiFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil kursi (findByJadwal): " + e.getMessage());
        }
        return daftarKursi;
    }

    public List<Kursi> findTersediaByJadwal(Jadwal jadwal) {
        List<Kursi> daftarKursi = new ArrayList<>();
        String sql = SQL_SELECT_BASE +
                "WHERE ku.jadwal_id = ? " +
                "AND ku.status = 'TERSEDIA' " +
                "ORDER BY ku.nomor_kursi ASC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jadwal.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftarKursi.add(buildKursiFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil kursi tersedia (findTersediaByJadwal): " + e.getMessage());
        }
        return daftarKursi;
    }

    public boolean update(Kursi kursi) {
        String sql = "UPDATE kursi SET status=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kursi.getStatus());
            ps.setInt(2, kursi.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui kursi: " + e.getMessage());
            return false;
        }
    }

    private Kursi buildKursiFromResultSet(ResultSet rs) throws SQLException {
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
                kereta,
                rute,
                jenisKelas,
                rs.getTimestamp("waktu_berangkat").toLocalDateTime(),
                rs.getTimestamp("waktu_tiba").toLocalDateTime()
        );
        jadwal.setId(rs.getInt("jadwal_id"));
        jadwal.setStatus(rs.getString("jadwal_status"));

        Kursi kursi = new Kursi(jadwal, jenisKelas, rs.getString("nomor_kursi"));
        kursi.setId(rs.getInt("kursi_id"));
        kursi.setStatus(rs.getString("kursi_status"));
        return kursi;
    }
}