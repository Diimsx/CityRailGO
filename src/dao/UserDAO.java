package dao;

import model.Admin;
import model.Penumpang;
import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;

public class UserDAO {

    private static final String SQL_SELECT_BASE =
            "SELECT u.id, u.username, u.password, u.email, u.nama_lengkap, u.no_telepon, " +
            "a.level_akses, a.jenis_kelamin AS admin_jenis_kelamin, a.tgl_lahir AS admin_tgl_lahir, " +
            "p.nik, p.jenis_kelamin AS penumpang_jenis_kelamin, p.tgl_lahir AS penumpang_tgl_lahir " +
            "FROM users u " +
            "LEFT JOIN admin a ON u.id = a.id " +
            "LEFT JOIN penumpang p ON u.id = p.id ";

    public User findById(int id) {
        String sql = SQL_SELECT_BASE + "WHERE u.id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil user (findById): " + e.getMessage());
        }
        return null;
    }

    public User findByUsername(String username) {
        String sql = SQL_SELECT_BASE + "WHERE u.username = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil user (findByUsername): " + e.getMessage());
        }
        return null;
    }

    public boolean save(User user) {
        String sqlUser = "INSERT INTO users (username, password, email, nama_lengkap, no_telepon) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getNamaLengkap());
                ps.setString(5, user.getNoTelepon());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }

            if (user instanceof Admin admin) {
                String sqlAdmin = "INSERT INTO admin (id, level_akses, jenis_kelamin, tgl_lahir) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlAdmin)) {
                    ps.setInt(1, admin.getId());
                    ps.setString(2, admin.getLevelAkses());
                    ps.setString(3, admin.getJenisKelamin());
                    setNullableDate(ps, 4, admin.getTglLahir());
                    ps.executeUpdate();
                }
            } else if (user instanceof Penumpang penumpang) {
                String sqlPenumpang = "INSERT INTO penumpang (id, nik, tgl_lahir, jenis_kelamin) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlPenumpang)) {
                    ps.setInt(1, penumpang.getId());
                    ps.setString(2, penumpang.getNik());
                    setNullableDate(ps, 3, penumpang.getTglLahir());
                    ps.setString(4, penumpang.getJenisKelamin());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollback(conn);
            System.out.println("Gagal menyimpan user: " + e.getMessage());
            return false;
        } finally {
            resetAutoCommit(conn);
        }
    }

    public boolean update(User user) {
        String sqlUser = "UPDATE users SET username=?, password=?, email=?, nama_lengkap=?, no_telepon=? WHERE id=?";
        Connection conn = DBConnection.getInstance();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getNamaLengkap());
                ps.setString(5, user.getNoTelepon());
                ps.setInt(6, user.getId());
                ps.executeUpdate();
            }

            if (user instanceof Admin admin) {
                String sqlAdmin = "UPDATE admin SET level_akses=?, jenis_kelamin=?, tgl_lahir=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlAdmin)) {
                    ps.setString(1, admin.getLevelAkses());
                    ps.setString(2, admin.getJenisKelamin());
                    setNullableDate(ps, 3, admin.getTglLahir());
                    ps.setInt(4, admin.getId());
                    ps.executeUpdate();
                }
            } else if (user instanceof Penumpang penumpang) {
                String sqlPenumpang = "UPDATE penumpang SET nik=?, tgl_lahir=?, jenis_kelamin=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlPenumpang)) {
                    ps.setString(1, penumpang.getNik());
                    setNullableDate(ps, 2, penumpang.getTglLahir());
                    ps.setString(3, penumpang.getJenisKelamin());
                    ps.setInt(4, penumpang.getId());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollback(conn);
            System.out.println("Gagal memperbarui user: " + e.getMessage());
            return false;
        } finally {
            resetAutoCommit(conn);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Gagal menghapus user: " + e.getMessage());
            return false;
        }
    }

    public int countPenumpang() {
        String sql = "SELECT COUNT(*) FROM penumpang";
        Connection conn = DBConnection.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Gagal menghitung jumlah penumpang: " + e.getMessage());
        }
        return 0;
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String namaLengkap = rs.getString("nama_lengkap");
        String noTelepon = rs.getString("no_telepon");

        User user;

        if (rs.getString("level_akses") != null) {
            String levelAkses = rs.getString("level_akses");
            String jenisKelamin = rs.getString("admin_jenis_kelamin");
            LocalDate tglLahir = toLocalDate(rs.getDate("admin_tgl_lahir"));
            user = new Admin(username, password, email, namaLengkap, noTelepon, levelAkses, jenisKelamin, tglLahir);

        } else if (rs.getString("nik") != null) {
            String nik = rs.getString("nik");
            String jenisKelamin = rs.getString("penumpang_jenis_kelamin");
            LocalDate tglLahir = toLocalDate(rs.getDate("penumpang_tgl_lahir"));
            user = new Penumpang(username, password, email, namaLengkap, noTelepon, nik, tglLahir, jenisKelamin);

        } else {
            return null;
        }

        user.setId(rs.getInt("id"));
        return user;
    }

    private LocalDate toLocalDate(Date sqlDate) {
        return (sqlDate != null) ? sqlDate.toLocalDate() : null;
    }

    private void setNullableDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date != null) {
            ps.setDate(index, Date.valueOf(date));
        } else {
            ps.setNull(index, Types.DATE);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.out.println("Gagal rollback: " + e.getMessage());
        }
    }

    private void resetAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("Gagal mengembalikan autoCommit: " + e.getMessage());
        }
    }
}