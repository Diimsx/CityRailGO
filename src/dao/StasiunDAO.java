package dao;

import model.Stasiun;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StasiunDAO {

    public Stasiun findById(int id) {
        String sql = "SELECT * FROM stasiun WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildStasiunFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil stasiun (findById): " + e.getMessage());
        }
        return null;
    }

    public Stasiun findByKode(String kodeStasiun) {
        String sql = "SELECT * FROM stasiun WHERE kode_stasiun = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeStasiun);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildStasiunFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil stasiun (findByKode): " + e.getMessage());
        }
        return null;
    }

    public List<Stasiun> findAll() {
        List<Stasiun> daftarStasiun = new ArrayList<>();
        String sql = "SELECT * FROM stasiun ORDER BY nama_stasiun ASC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarStasiun.add(buildStasiunFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua stasiun: " + e.getMessage());
        }
        return daftarStasiun;
    }

    public boolean kodeStasiunExists(String kodeStasiun) {
        String sql = "SELECT COUNT(*) as cnt FROM stasiun WHERE kode_stasiun = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeStasiun);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal cek kode stasiun: " + e.getMessage());
        }
        return false;
    }

    public boolean kodeStasiunExistsExcept(String kodeStasiun, int stasiunId) {
        String sql = "SELECT COUNT(*) as cnt FROM stasiun WHERE kode_stasiun = ? AND id != ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeStasiun);
            ps.setInt(2, stasiunId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal cek kode stasiun (except): " + e.getMessage());
        }
        return false;
    }

    public boolean isUsedInRute(int stasiunId) {
        String sql = "SELECT COUNT(*) as cnt FROM rute WHERE stasiun_asal_id = ? OR stasiun_tujuan_id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stasiunId);
            ps.setInt(2, stasiunId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal cek stasiun di rute: " + e.getMessage());
        }
        return false;
    }

    public boolean isUsedInRuteStasiun(int stasiunId) {
        String sql = "SELECT COUNT(*) as cnt FROM rute_stasiun WHERE stasiun_id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stasiunId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal cek stasiun di rute_stasiun: " + e.getMessage());
        }
        return false;
    }

    public String getUsageInfo(int stasiunId) {
        StringBuilder info = new StringBuilder();

        // Cek di rute
        String sqlRute = "SELECT DISTINCT r.id, r.nama_rute FROM rute r WHERE r.stasiun_asal_id = ? OR r.stasiun_tujuan_id = ? LIMIT 5";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sqlRute)) {
            ps.setInt(1, stasiunId);
            ps.setInt(2, stasiunId);
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next() && count < 5) {
                    if (count > 0) info.append("\n");
                    info.append("• Rute: ").append(rs.getString("nama_rute"));
                    count++;
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal ambil usage info: " + e.getMessage());
        }

        return info.toString();
    }

    public boolean save(Stasiun stasiun) {
        String sql = "INSERT INTO stasiun (kode_stasiun, nama_stasiun, kota) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, stasiun.getKodeStasiun());
            ps.setString(2, stasiun.getNamaStasiun());
            ps.setString(3, stasiun.getKota());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    stasiun.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan stasiun: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Stasiun stasiun) {
        String sql = "UPDATE stasiun SET nama_stasiun=?, kota=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stasiun.getNamaStasiun());
            ps.setString(2, stasiun.getKota());
            ps.setInt(3, stasiun.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui stasiun: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM stasiun WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal menghapus stasiun: " + e.getMessage());
            return false;
        }
    }

    private Stasiun buildStasiunFromResultSet(ResultSet rs) throws SQLException {
        Stasiun stasiun = new Stasiun(
                rs.getString("kode_stasiun"),
                rs.getString("nama_stasiun"),
                rs.getString("kota")
        );
        stasiun.setId(rs.getInt("id"));
        return stasiun;
    }
}