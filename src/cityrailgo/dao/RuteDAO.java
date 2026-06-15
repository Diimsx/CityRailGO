package dao;

import model.Rute;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RuteDAO {

    public Rute findById(int id) {
        String sql = "SELECT * FROM rute WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildRuteFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil rute (findById): " + e.getMessage());
        }
        return null;
    }

    public List<Rute> findAll() {
        List<Rute> daftarRute = new ArrayList<>();
        String sql = "SELECT * FROM rute";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarRute.add(buildRuteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua rute: " + e.getMessage());
        }
        return daftarRute;
    }

    public List<Rute> findByAsal(String stasiunAsal) {
        List<Rute> daftarRute = new ArrayList<>();
        String sql = "SELECT * FROM rute WHERE stasiun_asal = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stasiunAsal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftarRute.add(buildRuteFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil rute (findByAsal): " + e.getMessage());
        }
        return daftarRute;
    }

    public boolean save(Rute rute) {
        String sql = "INSERT INTO rute (stasiun_asal, stasiun_tujuan, jarak_km, estimasi_menit) VALUES (?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rute.getStasiunAsal());
            ps.setString(2, rute.getStasiunTujuan());
            ps.setDouble(3, rute.getJarakKm());
            ps.setInt(4, rute.getEstimasiMenit());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    rute.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan rute: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Rute rute) {
        String sql = "UPDATE rute SET stasiun_asal=?, stasiun_tujuan=?, jarak_km=?, estimasi_menit=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rute.getStasiunAsal());
            ps.setString(2, rute.getStasiunTujuan());
            ps.setDouble(3, rute.getJarakKm());
            ps.setInt(4, rute.getEstimasiMenit());
            ps.setInt(5, rute.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui rute: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM rute WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal menghapus rute: " + e.getMessage());
            return false;
        }
    }

    private Rute buildRuteFromResultSet(ResultSet rs) throws SQLException {
        Rute rute = new Rute(
                rs.getString("stasiun_asal"),
                rs.getString("stasiun_tujuan"),
                rs.getDouble("jarak_km"),
                rs.getInt("estimasi_menit")
        );
        rute.setId(rs.getInt("id"));
        return rute;
    }
}
