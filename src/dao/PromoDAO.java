package dao;

import model.Promo;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PromoDAO {

    public Promo findById(int id) {
        String sql = "SELECT * FROM promo WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildPromoFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil promo (findById): " + e.getMessage());
        }
        return null;
    }

    public Promo findByKode(String kodePromo) {
        String sql = "SELECT * FROM promo WHERE kode_promo = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodePromo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildPromoFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil promo (findByKode): " + e.getMessage());
        }
        return null;
    }

    public List<Promo> findAll() {
        List<Promo> daftarPromo = new ArrayList<>();
        String sql = "SELECT * FROM promo";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarPromo.add(buildPromoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua promo: " + e.getMessage());
        }
        return daftarPromo;
    }

    public boolean save(Promo promo) {
        String sql = "INSERT INTO promo (kode_promo, deskripsi, diskon_persen, tanggal_mulai, tanggal_berakhir, aktif) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, promo.getKodePromo());
            ps.setString(2, promo.getDeskripsi());
            ps.setDouble(3, promo.getDiskonPersen());
            ps.setDate(4, Date.valueOf(promo.getTanggalMulai()));
            ps.setDate(5, Date.valueOf(promo.getTanggalBerakhir()));
            ps.setBoolean(6, promo.isAktif());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    promo.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan promo: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Promo promo) {
        String sql = "UPDATE promo SET kode_promo=?, deskripsi=?, diskon_persen=?, tanggal_mulai=?, tanggal_berakhir=?, aktif=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, promo.getKodePromo());
            ps.setString(2, promo.getDeskripsi());
            ps.setDouble(3, promo.getDiskonPersen());
            ps.setDate(4, Date.valueOf(promo.getTanggalMulai()));
            ps.setDate(5, Date.valueOf(promo.getTanggalBerakhir()));
            ps.setBoolean(6, promo.isAktif());
            ps.setInt(7, promo.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui promo: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM promo WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal menghapus promo: " + e.getMessage());
            return false;
        }
    }

    private Promo buildPromoFromResultSet(ResultSet rs) throws SQLException {
        Promo promo = new Promo(
                rs.getString("kode_promo"),
                rs.getString("deskripsi"),
                rs.getDouble("diskon_persen"),
                rs.getDate("tanggal_mulai").toLocalDate(),
                rs.getDate("tanggal_berakhir").toLocalDate()
        );
        promo.setId(rs.getInt("id"));
        promo.setAktif(rs.getBoolean("aktif"));
        return promo;
    }
}