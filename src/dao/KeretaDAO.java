package dao;

import model.Kereta;
import util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KeretaDAO {

    private static final DateTimeFormatter FORMAT_JADWAL = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    public KeretaDAO() {
        ensureSchema();
    }

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

    public Kereta findByNomorKereta(String nomorKereta) {
        String sql = "SELECT * FROM kereta WHERE nomor_kereta = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomorKereta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildKeretaFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil kereta (findByNomorKereta): " + e.getMessage());
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

    public List<Kereta> findAktif() {
        List<Kereta> daftarKereta = new ArrayList<>();
        String sql = "SELECT * FROM kereta WHERE status = ? ORDER BY nama";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Kereta.STATUS_AKTIF);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftarKereta.add(buildKeretaFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil kereta aktif: " + e.getMessage());
        }
        return daftarKereta;
    }

    public boolean save(Kereta kereta) {
        String sql = "INSERT INTO kereta (nama, nomor_kereta, jumlah_gerbong, kapasitas_total, kelas_tersedia, status, gerbong_eksekutif, gerbong_bisnis, gerbong_ekonomi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kereta.getNama());
            ps.setString(2, kereta.getNomorKereta());
            ps.setInt(3, kereta.getJumlahGerbong());
            ps.setInt(4, kereta.getKapasitasTotal());
            ps.setString(5, kereta.getKelasTersedia());
            ps.setString(6, kereta.getStatus());
            ps.setInt(7, kereta.getGerbongEksekutif());
            ps.setInt(8, kereta.getGerbongBisnis());
            ps.setInt(9, kereta.getGerbongEkonomi());
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
        String sql = "UPDATE kereta SET nama=?, nomor_kereta=?, jumlah_gerbong=?, kapasitas_total=?, kelas_tersedia=?, status=?, gerbong_eksekutif=?, gerbong_bisnis=?, gerbong_ekonomi=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kereta.getNama());
            ps.setString(2, kereta.getNomorKereta());
            ps.setInt(3, kereta.getJumlahGerbong());
            ps.setInt(4, kereta.getKapasitasTotal());
            ps.setString(5, kereta.getKelasTersedia());
            ps.setString(6, kereta.getStatus());
            ps.setInt(7, kereta.getGerbongEksekutif());
            ps.setInt(8, kereta.getGerbongBisnis());
            ps.setInt(9, kereta.getGerbongEkonomi());
            ps.setInt(10, kereta.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui kereta: " + e.getMessage());
            return false;
        }
    }

    public String getJadwalAktifPertama(int keretaId) {
        String sql =
                "SELECT j.waktu_berangkat, r.stasiun_asal, r.stasiun_tujuan " +
                "FROM jadwal j " +
                "JOIN rute r ON j.rute_id = r.id " +
                "WHERE j.kereta_id = ? " +
                "AND j.waktu_berangkat >= NOW() " +
                "AND j.status <> 'DIBATALKAN' " +
                "ORDER BY j.waktu_berangkat ASC " +
                "LIMIT 1";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, keretaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp waktu = rs.getTimestamp("waktu_berangkat");
                    return waktu.toLocalDateTime().format(FORMAT_JADWAL)
                            + " (Rute: " + rs.getString("stasiun_asal")
                            + " - " + rs.getString("stasiun_tujuan") + ")";
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengecek jadwal aktif kereta: " + e.getMessage());
        }
        return null;
    }

    public String getJadwalDenganTiketAktifPertama(int keretaId) {
        String sql =
                "SELECT j.waktu_berangkat, r.stasiun_asal, r.stasiun_tujuan, COUNT(t.id) AS jumlah_penumpang " +
                "FROM jadwal j " +
                "JOIN rute r ON j.rute_id = r.id " +
                "JOIN tiket t ON t.jadwal_id = j.id " +
                "WHERE j.kereta_id = ? " +
                "AND j.waktu_berangkat >= NOW() " +
                "AND j.status <> 'DIBATALKAN' " +
                "AND t.status = 'AKTIF' " +
                "GROUP BY j.id, j.waktu_berangkat, r.stasiun_asal, r.stasiun_tujuan " +
                "ORDER BY j.waktu_berangkat ASC " +
                "LIMIT 1";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, keretaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp waktu = rs.getTimestamp("waktu_berangkat");
                    return waktu.toLocalDateTime().format(FORMAT_JADWAL)
                            + " (Rute: " + rs.getString("stasiun_asal")
                            + " - " + rs.getString("stasiun_tujuan")
                            + ", " + rs.getInt("jumlah_penumpang") + " penumpang aktif)";
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengecek tiket aktif kereta: " + e.getMessage());
        }
        return null;
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
        int gEks = rs.getInt("gerbong_eksekutif");
        int gBis = rs.getInt("gerbong_bisnis");
        int gEko = rs.getInt("gerbong_ekonomi");
        Kereta kereta;
        if (gEks == 0 && gBis == 0 && gEko == 0) {
            kereta = new Kereta(
                    rs.getString("nama"),
                    rs.getString("nomor_kereta"),
                    rs.getInt("jumlah_gerbong"),
                    rs.getInt("kapasitas_total"),
                    rs.getString("kelas_tersedia"),
                    rs.getString("status")
            );
        } else {
            kereta = new Kereta(
                    rs.getString("nama"),
                    rs.getString("nomor_kereta"),
                    gEks, gBis, gEko,
                    rs.getString("kelas_tersedia"),
                    rs.getString("status")
            );
        }
        kereta.setId(rs.getInt("id"));
        return kereta;
    }

    private void ensureSchema() {
        Connection conn = DBConnection.getInstance();
        if (conn == null) {
            return;
        }

        try {
            if (!columnExists(conn, "jumlah_gerbong")) {
                executeAlter(conn, "ALTER TABLE kereta ADD COLUMN jumlah_gerbong INT NOT NULL DEFAULT 1 AFTER nomor_kereta");
            }
            if (!columnExists(conn, "kelas_tersedia")) {
                executeAlter(conn, "ALTER TABLE kereta ADD COLUMN kelas_tersedia VARCHAR(255) NOT NULL DEFAULT '' AFTER kapasitas_total");
            }
            if (!columnExists(conn, "status")) {
                executeAlter(conn, "ALTER TABLE kereta ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AKTIF' AFTER kelas_tersedia");
            }
            if (!columnExists(conn, "gerbong_eksekutif")) {
                executeAlter(conn, "ALTER TABLE kereta ADD COLUMN gerbong_eksekutif INT NOT NULL DEFAULT 0 AFTER status");
            }
            if (!columnExists(conn, "gerbong_bisnis")) {
                executeAlter(conn, "ALTER TABLE kereta ADD COLUMN gerbong_bisnis INT NOT NULL DEFAULT 0 AFTER gerbong_eksekutif");
            }
            if (!columnExists(conn, "gerbong_ekonomi")) {
                executeAlter(conn, "ALTER TABLE kereta ADD COLUMN gerbong_ekonomi INT NOT NULL DEFAULT 0 AFTER gerbong_bisnis");
            }
            if (!indexExists(conn, "uk_kereta_nomor")) {
                executeAlter(conn, "ALTER TABLE kereta ADD CONSTRAINT uk_kereta_nomor UNIQUE (nomor_kereta)");
            }
        } catch (SQLException e) {
            System.out.println("Gagal memastikan schema kereta: " + e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, "kereta", columnName)) {
            return rs.next();
        }
    }

    private boolean indexExists(Connection conn, String indexName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getIndexInfo(conn.getCatalog(), null, "kereta", false, false)) {
            while (rs.next()) {
                if (indexName.equalsIgnoreCase(rs.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void executeAlter(Connection conn, String sql) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
