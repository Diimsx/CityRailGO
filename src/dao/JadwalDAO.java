package dao;

import model.JenisKelas;
import model.Jadwal;
import model.Kereta;
import model.Rute;
import util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JadwalDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private static final String SQL_SELECT_BASE =
        "SELECT j.id, j.waktu_berangkat, j.waktu_tiba, j.status, j.harga_final, j.info_harga, " +
        "k.id AS kereta_id, k.nama AS kereta_nama, k.nomor_kereta, " +
        "k.jumlah_gerbong, k.kapasitas_total, k.kelas_tersedia, k.status AS kereta_status, " +
        "r.id AS rute_id, r.nama_rute, r.stasiun_asal, r.stasiun_tujuan, r.jarak_km, r.estimasi_menit, " +
        "jk.id AS jenis_kelas_id, jk.nama_kelas, jk.harga_per_km, jk.fasilitas " +
        "FROM jadwal j " +
        "JOIN kereta k ON j.kereta_id = k.id " +
        "JOIN rute r ON j.rute_id = r.id " +
        "JOIN jenis_kelas jk ON j.jenis_kelas_id = jk.id ";

    public JadwalDAO() {
        ensureSchema();
    }

    // =========================================================
    // SCHEMA MIGRATION
    // =========================================================

    private void ensureSchema() {
        Connection conn = DBConnection.getInstance();
        if (conn == null) return;
        try {
            if (!columnExists(conn, "harga_final")) {
                execute(conn, "ALTER TABLE jadwal ADD COLUMN harga_final DOUBLE NOT NULL DEFAULT 0 AFTER status");
            }
            if (!columnExists(conn, "info_harga")) {
                execute(conn, "ALTER TABLE jadwal ADD COLUMN info_harga VARCHAR(255) NOT NULL DEFAULT '' AFTER harga_final");
            }
        } catch (SQLException e) {
            System.out.println("JadwalDAO ensureSchema: " + e.getMessage());
        }
    }

    // =========================================================
    // FIND OPERATIONS
    // =========================================================

    public Jadwal findById(int id) {
        String sql = SQL_SELECT_BASE + "WHERE j.id = ?";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildJadwalFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("JadwalDAO findById: " + e.getMessage());
        }
        return null;
    }

    public List<Jadwal> findAll() {
        List<Jadwal> list = new ArrayList<>();
        String sql = SQL_SELECT_BASE + "ORDER BY j.waktu_berangkat ASC";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(buildJadwalFromResultSet(rs));
        } catch (SQLException e) {
            System.out.println("JadwalDAO findAll: " + e.getMessage());
        }
        return list;
    }

    public List<Jadwal> findByRuteDanTanggal(String stasiunAsal, String stasiunTujuan, LocalDate tanggal) {
        List<Jadwal> list = new ArrayList<>();
        String sql = SQL_SELECT_BASE +
            "WHERE r.stasiun_asal = ? AND r.stasiun_tujuan = ? " +
            "AND DATE(j.waktu_berangkat) = ? AND j.status = 'TERSEDIA' " +
            "ORDER BY j.waktu_berangkat ASC";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stasiunAsal);
            ps.setString(2, stasiunTujuan);
            ps.setDate(3, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(buildJadwalFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("JadwalDAO findByRuteDanTanggal: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // VALIDASI KONFLIK KERETA
    // =========================================================

    /**
     * Cek apakah kereta sudah memiliki jadwal aktif yang tumpang-tindih waktu
     * dengan jadwal yang akan dibuat/diupdate.
     *
     * Logika konflik: jadwal baru [berangkat, tiba] overlap dengan jadwal existing
     * jika waktuBerangkatBaru < tibaSudahAda AND waktuTibaBaru > berangkatSudahAda
     *
     * @param keretaId       ID kereta
     * @param waktuBerangkat waktu berangkat jadwal baru
     * @param waktuTiba      waktu tiba jadwal baru
     * @param kecualiJadwalId ID jadwal yang dikecualikan (untuk mode edit), 0 jika tambah baru
     * @return String deskripsi konflik jika ada, null jika aman
     */
    public String cekKonflikKereta(int keretaId, LocalDateTime waktuBerangkat,
                                   LocalDateTime waktuTiba, int kecualiJadwalId) {
        String sql =
            "SELECT j.id, j.waktu_berangkat, j.waktu_tiba, r.stasiun_asal, r.stasiun_tujuan " +
            "FROM jadwal j JOIN rute r ON j.rute_id = r.id " +
            "WHERE j.kereta_id = ? " +
            "AND j.status <> 'DIBATALKAN' " +
            "AND j.id <> ? " +
            "AND ? < j.waktu_tiba AND ? > j.waktu_berangkat " +
            "LIMIT 1";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, keretaId);
            ps.setInt(2, kecualiJadwalId);
            ps.setTimestamp(3, Timestamp.valueOf(waktuBerangkat));
            ps.setTimestamp(4, Timestamp.valueOf(waktuTiba));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime tgtBerangkat = rs.getTimestamp("waktu_berangkat").toLocalDateTime();
                    String asal   = rs.getString("stasiun_asal");
                    String tujuan = rs.getString("stasiun_tujuan");
                    return tgtBerangkat.format(FMT) + " (" + asal + " \u2192 " + tujuan + ")";
                }
            }
        } catch (SQLException e) {
            System.out.println("JadwalDAO cekKonflikKereta: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // SAVE / UPDATE / DELETE
    // =========================================================

    public boolean save(Jadwal jadwal) {
        String sql =
            "INSERT INTO jadwal (kereta_id, rute_id, jenis_kelas_id, " +
            "waktu_berangkat, waktu_tiba, status, harga_final, info_harga) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, jadwal.getKereta().getId());
            ps.setInt(2, jadwal.getRute().getId());
            ps.setInt(3, jadwal.getJenisKelas().getId());
            ps.setTimestamp(4, Timestamp.valueOf(jadwal.getWaktuBerangkat()));
            ps.setTimestamp(5, Timestamp.valueOf(jadwal.getWaktuTiba()));
            ps.setString(6, jadwal.getStatus());
            ps.setDouble(7, jadwal.getHargaFinal());
            ps.setString(8, jadwal.getInfoHarga() != null ? jadwal.getInfoHarga() : "");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) jadwal.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.out.println("JadwalDAO save: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Jadwal jadwal) {
        String sql =
            "UPDATE jadwal SET kereta_id=?, rute_id=?, jenis_kelas_id=?, " +
            "waktu_berangkat=?, waktu_tiba=?, status=?, harga_final=?, info_harga=? WHERE id=?";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jadwal.getKereta().getId());
            ps.setInt(2, jadwal.getRute().getId());
            ps.setInt(3, jadwal.getJenisKelas().getId());
            ps.setTimestamp(4, Timestamp.valueOf(jadwal.getWaktuBerangkat()));
            ps.setTimestamp(5, Timestamp.valueOf(jadwal.getWaktuTiba()));
            ps.setString(6, jadwal.getStatus());
            ps.setDouble(7, jadwal.getHargaFinal());
            ps.setString(8, jadwal.getInfoHarga() != null ? jadwal.getInfoHarga() : "");
            ps.setInt(9, jadwal.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("JadwalDAO update: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM jadwal WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("JadwalDAO delete: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    // BUILD FROM RESULT SET
    // =========================================================

    private Jadwal buildJadwalFromResultSet(ResultSet rs) throws SQLException {
        Kereta kereta = new Kereta(
            rs.getString("kereta_nama"),
            rs.getString("nomor_kereta"),
            rs.getInt("jumlah_gerbong"),
            rs.getInt("kapasitas_total"),
            rs.getString("kelas_tersedia"),
            rs.getString("kereta_status")
        );
        kereta.setId(rs.getInt("kereta_id"));

        Rute rute = new Rute(
            rs.getString("nama_rute"),
            null, null,
            rs.getDouble("jarak_km"),
            rs.getInt("estimasi_menit")
        );
        // Isi nama stasiun lewat backward-compat setter
        rute.setStasiunAsal(rs.getString("stasiun_asal"));
        rute.setStasiunTujuan(rs.getString("stasiun_tujuan"));
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
        jadwal.setId(rs.getInt("id"));
        jadwal.setStatus(rs.getString("status"));

        // Load harga_final (kolom baru, fallback 0 jika belum ada)
        try {
            jadwal.setHargaFinal(rs.getDouble("harga_final"));
            jadwal.setInfoHarga(rs.getString("info_harga"));
        } catch (SQLException ignored) { /* kolom belum ada di DB lama */ }

        return jadwal;
    }

    // =========================================================
    // SCHEMA HELPERS
    // =========================================================

    private boolean columnExists(Connection conn, String column) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "jadwal", column)) {
            return rs.next();
        }
    }

    private void execute(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }
}