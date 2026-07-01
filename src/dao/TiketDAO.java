package dao;

import model.JenisKelas;
import model.Jadwal;
import model.Kereta;
import model.Kursi;
import model.Penumpang;
import model.Rute;
import model.Tiket;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TiketDAO {

    public TiketDAO() {
        ensureSchema();
    }

    private static final String SQL_SELECT_BASE =
            "SELECT t.id AS tiket_id, t.kode_tiket, t.harga_total, t.status AS tiket_status, t.nama_penumpang, t.nik_penumpang, t.usia_penumpang, " +
            "u.id AS penumpang_id, u.username, u.password, u.email, u.nama_lengkap, u.no_telepon, " +
            "p.nik, p.tgl_lahir AS penumpang_tgl_lahir, p.jenis_kelamin AS penumpang_jenis_kelamin, " +
            "j.id AS jadwal_id, j.waktu_berangkat, j.waktu_tiba, j.status AS jadwal_status, " +
            "k.id AS kereta_id, k.nama AS kereta_nama, k.nomor_kereta, k.kapasitas_total, " +
            "r.id AS rute_id, r.stasiun_asal, r.stasiun_tujuan, r.jarak_km, r.estimasi_menit, " +
            "jk.id AS jenis_kelas_id, jk.nama_kelas, jk.harga_per_km, jk.fasilitas, " +
            "ku.id AS kursi_id, ku.nomor_kursi, ku.status AS kursi_status " +
            "FROM tiket t " +
            "JOIN penumpang p ON t.penumpang_id = p.id " +
            "JOIN users u ON p.id = u.id " +
            "JOIN jadwal j ON t.jadwal_id = j.id " +
            "JOIN kereta k ON j.kereta_id = k.id " +
            "JOIN rute r ON j.rute_id = r.id " +
            "JOIN jenis_kelas jk ON j.jenis_kelas_id = jk.id " +
            "LEFT JOIN kursi ku ON t.kursi_id = ku.id ";

    public Tiket findById(int id) {
        String sql = SQL_SELECT_BASE + "WHERE t.id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildTiketFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil tiket (findById): " + e.getMessage());
        }
        return null;
    }

    public List<Tiket> findByPenumpang(Penumpang penumpang) {
        List<Tiket> daftarTiket = new ArrayList<>();
        String sql = SQL_SELECT_BASE +
                "WHERE t.penumpang_id = ? " +
                "ORDER BY j.waktu_berangkat DESC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, penumpang.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftarTiket.add(buildTiketFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil tiket (findByPenumpang): " + e.getMessage());
        }
        return daftarTiket;
    }

    public List<Tiket> findAll() {
        List<Tiket> daftarTiket = new ArrayList<>();
        String sql = SQL_SELECT_BASE + "ORDER BY j.waktu_berangkat DESC";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                daftarTiket.add(buildTiketFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil semua tiket: " + e.getMessage());
        }
        return daftarTiket;
    }

    public boolean save(Tiket tiket) {
        String sql = "INSERT INTO tiket (kode_tiket, penumpang_id, jadwal_id, kursi_id, harga_total, status, nama_penumpang, nik_penumpang, usia_penumpang) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tiket.getKodeTiket());
            ps.setInt(2, tiket.getPenumpang().getId());
            ps.setInt(3, tiket.getJadwal().getId());
            if (tiket.getKursi() != null && tiket.getKursi().getId() > 0) {
                ps.setInt(4, tiket.getKursi().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setDouble(5, tiket.getHargaTotal());
            ps.setString(6, tiket.getStatus());
            ps.setString(7, tiket.getNamaPenumpang());
            ps.setString(8, tiket.getNikPenumpang());
            ps.setInt(9, tiket.getUsiaPenumpang());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tiket.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan tiket: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Tiket tiket) {
        String sql = "UPDATE tiket SET kode_tiket=?, status=?, nama_penumpang=?, nik_penumpang=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tiket.getKodeTiket());
            ps.setString(2, tiket.getStatus());
            ps.setString(3, tiket.getNamaPenumpang());
            ps.setString(4, tiket.getNikPenumpang());
            ps.setInt(5, tiket.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui tiket: " + e.getMessage());
            return false;
        }
    }

    private Tiket buildTiketFromResultSet(ResultSet rs) throws SQLException {
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

        Kursi kursi = null;
        int kursiId = rs.getInt("kursi_id");
        if (!rs.wasNull()) {
            kursi = new Kursi(jadwal, jenisKelas, rs.getString("nomor_kursi"));
            kursi.setId(kursiId);
            kursi.setStatus(rs.getString("kursi_status"));
        }

        Tiket tiket = new Tiket(penumpang, jadwal, kursi, rs.getDouble("harga_total"));
        tiket.setId(rs.getInt("tiket_id"));
        tiket.setKodeTiket(rs.getString("kode_tiket"));
        tiket.setStatus(rs.getString("tiket_status"));
        tiket.setNamaPenumpang(rs.getString("nama_penumpang"));
        tiket.setNikPenumpang(rs.getString("nik_penumpang"));
        tiket.setUsiaPenumpang(rs.getInt("usia_penumpang"));
        return tiket;
    }

    private LocalDate toLocalDate(Date sqlDate) {
        return (sqlDate != null) ? sqlDate.toLocalDate() : null;
    }

    private void ensureSchema() {
        Connection conn = DBConnection.getInstance();
        if (conn == null) return;
        try {
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            boolean hasNama = false;
            boolean hasNik = false;
            boolean hasUsia = false;
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, "tiket", "nama_penumpang")) {
                if (rs.next()) hasNama = true;
            }
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, "tiket", "nik_penumpang")) {
                if (rs.next()) hasNik = true;
            }
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, "tiket", "usia_penumpang")) {
                if (rs.next()) hasUsia = true;
            }
            try (Statement statement = conn.createStatement()) {
                if (!hasNama) {
                    statement.executeUpdate("ALTER TABLE tiket ADD COLUMN nama_penumpang VARCHAR(255) NULL");
                }
                if (!hasNik) {
                    statement.executeUpdate("ALTER TABLE tiket ADD COLUMN nik_penumpang VARCHAR(50) NULL");
                }
                if (!hasUsia) {
                    statement.executeUpdate("ALTER TABLE tiket ADD COLUMN usia_penumpang INT NULL");
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal memastikan schema tiket: " + e.getMessage());
        }
    }
}