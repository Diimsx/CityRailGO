package dao;

import model.Rute;
import model.Stasiun;
import util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RuteDAO {

    public RuteDAO() {
        ensureSchema();
    }

    private void ensureSchema() {
        Connection conn = DBConnection.getInstance();
        if (conn == null) return;

        try {
            if (!columnExists(conn, "rute", "nama_rute")) {
                execute(conn, "ALTER TABLE rute ADD COLUMN nama_rute VARCHAR(255) NOT NULL DEFAULT '' AFTER id");
            }
            if (!columnExists(conn, "rute", "stasiun_asal_id")) {
                execute(conn, "ALTER TABLE rute ADD COLUMN stasiun_asal_id INT NOT NULL DEFAULT 0 AFTER nama_rute");
            }
            if (!columnExists(conn, "rute", "stasiun_tujuan_id")) {
                execute(conn, "ALTER TABLE rute ADD COLUMN stasiun_tujuan_id INT NOT NULL DEFAULT 0 AFTER stasiun_asal_id");
            }
            execute(conn,
                "CREATE TABLE IF NOT EXISTS rute_stasiun (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  rute_id INT NOT NULL," +
                "  stasiun_id INT NOT NULL," +
                "  urutan INT NOT NULL," +
                "  FOREIGN KEY (rute_id) REFERENCES rute(id) ON DELETE CASCADE," +
                "  FOREIGN KEY (stasiun_id) REFERENCES stasiun(id)" +
                ")"
            );
            migrasiDataLama(conn);
        } catch (SQLException e) {
            System.out.println("RuteDAO ensureSchema error: " + e.getMessage());
        }
    }

    private void migrasiDataLama(Connection conn) {
        String sql = "SELECT id, stasiun_asal, stasiun_tujuan FROM rute WHERE stasiun_asal_id = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int ruteId = rs.getInt("id");
                String namaAsal   = rs.getString("stasiun_asal");
                String namaTujuan = rs.getString("stasiun_tujuan");
                int idAsal   = cariIdStasiunByNama(conn, namaAsal);
                int idTujuan = cariIdStasiunByNama(conn, namaTujuan);
                if (idAsal > 0 && idTujuan > 0) {
                    try (PreparedStatement upd = conn.prepareStatement(
                            "UPDATE rute SET stasiun_asal_id=?, stasiun_tujuan_id=?, nama_rute=? WHERE id=?")) {
                        upd.setInt(1, idAsal);
                        upd.setInt(2, idTujuan);
                        upd.setString(3, namaAsal + " \u2192 " + namaTujuan);
                        upd.setInt(4, ruteId);
                        upd.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Migrasi data rute lama: " + e.getMessage());
        }
    }

    private int cariIdStasiunByNama(Connection conn, String nama) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM stasiun WHERE nama_stasiun = ? LIMIT 1")) {
            ps.setString(1, nama);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("cariIdStasiunByNama error: " + e.getMessage());
        }
        return 0;
    }

    public Rute findById(int id) {
        String sql =
            "SELECT r.*, " +
            "  sa.id AS asal_id, sa.kode_stasiun AS asal_kode, sa.nama_stasiun AS asal_nama, sa.kota AS asal_kota, " +
            "  st.id AS tujuan_id, st.kode_stasiun AS tujuan_kode, st.nama_stasiun AS tujuan_nama, st.kota AS tujuan_kota " +
            "FROM rute r " +
            "LEFT JOIN stasiun sa ON r.stasiun_asal_id = sa.id " +
            "LEFT JOIN stasiun st ON r.stasiun_tujuan_id = st.id " +
            "WHERE r.id = ?";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rute rute = buildRuteFromResultSet(rs);
                    rute.setStasiunPemberhentian(loadStops(conn, rute.getId()));
                    return rute;
                }
            }
        } catch (SQLException e) {
            System.out.println("RuteDAO findById error: " + e.getMessage());
        }
        return null;
    }

    public List<Rute> findAll() {
        List<Rute> list = new ArrayList<>();
        String sql =
            "SELECT r.*, " +
            "  sa.id AS asal_id, sa.kode_stasiun AS asal_kode, sa.nama_stasiun AS asal_nama, sa.kota AS asal_kota, " +
            "  st.id AS tujuan_id, st.kode_stasiun AS tujuan_kode, st.nama_stasiun AS tujuan_nama, st.kota AS tujuan_kota " +
            "FROM rute r " +
            "LEFT JOIN stasiun sa ON r.stasiun_asal_id = sa.id " +
            "LEFT JOIN stasiun st ON r.stasiun_tujuan_id = st.id " +
            "ORDER BY r.id ASC";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rute rute = buildRuteFromResultSet(rs);
                rute.setStasiunPemberhentian(loadStops(conn, rute.getId()));
                list.add(rute);
            }
        } catch (SQLException e) {
            System.out.println("RuteDAO findAll error: " + e.getMessage());
        }
        return list;
    }

    public List<Rute> findByAsal(String stasiunAsal) {
        List<Rute> list = new ArrayList<>();
        String sql =
            "SELECT r.*, " +
            "  sa.id AS asal_id, sa.kode_stasiun AS asal_kode, sa.nama_stasiun AS asal_nama, sa.kota AS asal_kota, " +
            "  st.id AS tujuan_id, st.kode_stasiun AS tujuan_kode, st.nama_stasiun AS tujuan_nama, st.kota AS tujuan_kota " +
            "FROM rute r " +
            "LEFT JOIN stasiun sa ON r.stasiun_asal_id = sa.id " +
            "LEFT JOIN stasiun st ON r.stasiun_tujuan_id = st.id " +
            "WHERE sa.nama_stasiun = ?";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stasiunAsal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rute rute = buildRuteFromResultSet(rs);
                    rute.setStasiunPemberhentian(loadStops(conn, rute.getId()));
                    list.add(rute);
                }
            }
        } catch (SQLException e) {
            System.out.println("RuteDAO findByAsal error: " + e.getMessage());
        }
        return list;
    }

    public boolean save(Rute rute) {
        String sql =
            "INSERT INTO rute (nama_rute, stasiun_asal_id, stasiun_tujuan_id, " +
            "stasiun_asal, stasiun_tujuan, jarak_km, estimasi_menit) VALUES (?,?,?,?,?,?,?)";
        Connection conn = DBConnection.getInstance();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, rute.getNamaRute());
                ps.setInt(2, rute.getStasiunAsalObj() != null ? rute.getStasiunAsalObj().getId() : 0);
                ps.setInt(3, rute.getStasiunTujuanObj() != null ? rute.getStasiunTujuanObj().getId() : 0);
                ps.setString(4, rute.getStasiunAsal());
                ps.setString(5, rute.getStasiunTujuan());
                ps.setDouble(6, rute.getJarakKm());
                ps.setInt(7, rute.getEstimasiMenit());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) rute.setId(rs.getInt(1));
                }
            }
            saveStops(conn, rute);
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            System.out.println("RuteDAO save error: " + e.getMessage());
            return false;
        } finally {
            restoreAutoCommit(conn);
        }
    }

    public boolean update(Rute rute) {
        String sql =
            "UPDATE rute SET nama_rute=?, stasiun_asal_id=?, stasiun_tujuan_id=?, " +
            "stasiun_asal=?, stasiun_tujuan=?, jarak_km=?, estimasi_menit=? WHERE id=?";
        Connection conn = DBConnection.getInstance();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rute.getNamaRute());
                ps.setInt(2, rute.getStasiunAsalObj() != null ? rute.getStasiunAsalObj().getId() : 0);
                ps.setInt(3, rute.getStasiunTujuanObj() != null ? rute.getStasiunTujuanObj().getId() : 0);
                ps.setString(4, rute.getStasiunAsal());
                ps.setString(5, rute.getStasiunTujuan());
                ps.setDouble(6, rute.getJarakKm());
                ps.setInt(7, rute.getEstimasiMenit());
                ps.setInt(8, rute.getId());
                ps.executeUpdate();
            }
            deleteStops(conn, rute.getId());
            saveStops(conn, rute);
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            System.out.println("RuteDAO update error: " + e.getMessage());
            return false;
        } finally {
            restoreAutoCommit(conn);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM rute WHERE id = ?";
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("RuteDAO delete error: " + e.getMessage());
            return false;
        }
    }

    private Rute buildRuteFromResultSet(ResultSet rs) throws SQLException {
        Stasiun asal = null;
        int asalId = rs.getInt("asal_id");
        if (!rs.wasNull() && asalId > 0) {
            asal = new Stasiun(rs.getString("asal_kode"), rs.getString("asal_nama"), rs.getString("asal_kota"));
            asal.setId(asalId);
        }
        Stasiun tujuan = null;
        int tujuanId = rs.getInt("tujuan_id");
        if (!rs.wasNull() && tujuanId > 0) {
            tujuan = new Stasiun(rs.getString("tujuan_kode"), rs.getString("tujuan_nama"), rs.getString("tujuan_kota"));
            tujuan.setId(tujuanId);
        }
        if (asal == null) {
            asal = new Stasiun("", rs.getString("stasiun_asal"), "");
        }
        if (tujuan == null) {
            tujuan = new Stasiun("", rs.getString("stasiun_tujuan"), "");
        }

        Rute rute = new Rute(
            rs.getString("nama_rute"),
            asal,
            tujuan,
            rs.getDouble("jarak_km"),
            rs.getInt("estimasi_menit")
        );
        rute.setId(rs.getInt("id"));
        return rute;
    }

    private List<Stasiun> loadStops(Connection conn, int ruteId) {
        List<Stasiun> stops = new ArrayList<>();
        String sql =
            "SELECT s.id, s.kode_stasiun, s.nama_stasiun, s.kota " +
            "FROM rute_stasiun rs " +
            "JOIN stasiun s ON rs.stasiun_id = s.id " +
            "WHERE rs.rute_id = ? ORDER BY rs.urutan ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ruteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stasiun s = new Stasiun(
                        rs.getString("kode_stasiun"),
                        rs.getString("nama_stasiun"),
                        rs.getString("kota")
                    );
                    s.setId(rs.getInt("id"));
                    stops.add(s);
                }
            }
        } catch (SQLException e) {
            System.out.println("loadStops error: " + e.getMessage());
        }
        return stops;
    }

    private void saveStops(Connection conn, Rute rute) throws SQLException {
        List<Stasiun> stops = rute.getStasiunPemberhentian();
        if (stops == null || stops.isEmpty()) return;
        String sql = "INSERT INTO rute_stasiun (rute_id, stasiun_id, urutan) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < stops.size(); i++) {
                ps.setInt(1, rute.getId());
                ps.setInt(2, stops.get(i).getId());
                ps.setInt(3, i + 1);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteStops(Connection conn, int ruteId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM rute_stasiun WHERE rute_id = ?")) {
            ps.setInt(1, ruteId);
            ps.executeUpdate();
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, column)) {
            return rs.next();
        }
    }

    private void execute(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private void rollback(Connection conn) {
        try { conn.rollback(); } catch (SQLException ignored) {}
    }

    private void restoreAutoCommit(Connection conn) {
        try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
    }
}
