package dao;

import model.Kereta;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KeretaDAO {

    public Kereta findById(int id) {
        String sql = "SELECT * FROM kereta WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildKeretaFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil kereta (findById): " + e.getMessage());
        }
        return null;
    }

    public List<Kereta> findAll() {
        List<Kereta> daftarKereta = new ArrayList<>();
        String sql = "SELECT * FROM kereta";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarKereta.add(buildKeretaFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua kereta: " + e.getMessage());
        }
        return daftarKereta;
    }

    public boolean save(Kereta kereta) {
        String sql = "INSERT INTO kereta (nama, nomor_kereta, kapasitas_total) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kereta.getNama());
            ps.setString(2, kereta.getNomorKereta());
            ps.setInt(3, kereta.getKapasitasTotal());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    kereta.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan kereta: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Kereta kereta) {
        String sql = "UPDATE kereta SET nama=?, nomor_kereta=?, kapasitas_total=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kereta.getNama());
            ps.setString(2, kereta.getNomorKereta());
            ps.setInt(3, kereta.getKapasitasTotal());
            ps.setInt(4, kereta.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui kereta: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM kereta WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal menghapus kereta: " + e.getMessage());
            return false;
        }
    }

    private Kereta buildKeretaFromResultSet(ResultSet rs) throws SQLException {
        Kereta kereta = new Kereta(
                rs.getString("nama"),
                rs.getString("nomor_kereta"),
                rs.getInt("kapasitas_total")
        );
        kereta.setId(rs.getInt("id"));
        return kereta;
    }
}