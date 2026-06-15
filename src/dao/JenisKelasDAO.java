package dao;

import model.JenisKelas;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JenisKelasDAO {

    public JenisKelas findById(int id) {
        String sql = "SELECT * FROM jenis_kelas WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildJenisKelasFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil jenis kelas (findById): " + e.getMessage());
        }
        return null;
    }

    public List<JenisKelas> findAll() {
        List<JenisKelas> daftarJenisKelas = new ArrayList<>();
        String sql = "SELECT * FROM jenis_kelas";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarJenisKelas.add(buildJenisKelasFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua jenis kelas: " + e.getMessage());
        }
        return daftarJenisKelas;
    }

    public boolean save(JenisKelas jenisKelas) {
        String sql = "INSERT INTO jenis_kelas (nama_kelas, harga_per_km, fasilitas) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, jenisKelas.getNamaKelas());
            ps.setDouble(2, jenisKelas.getHargaPerKm());
            ps.setString(3, jenisKelas.getFasilitas());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    jenisKelas.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan jenis kelas: " + e.getMessage());
            return false;
        }
    }

    public boolean update(JenisKelas jenisKelas) {
        String sql = "UPDATE jenis_kelas SET nama_kelas=?, harga_per_km=?, fasilitas=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jenisKelas.getNamaKelas());
            ps.setDouble(2, jenisKelas.getHargaPerKm());
            ps.setString(3, jenisKelas.getFasilitas());
            ps.setInt(4, jenisKelas.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui jenis kelas: " + e.getMessage());
            return false;
        }
    }

    private JenisKelas buildJenisKelasFromResultSet(ResultSet rs) throws SQLException {
        JenisKelas jenisKelas = new JenisKelas(
                rs.getString("nama_kelas"),
                rs.getDouble("harga_per_km"),
                rs.getString("fasilitas")
        );
        jenisKelas.setId(rs.getInt("id"));
        return jenisKelas;
    }
}
