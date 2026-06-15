import util.DBConnection;
import java.sql.Connection;

public class main {
    public static void main(String[] args) {
        Connection conn = DBConnection.getInstance();
        if (conn != null) {
            System.out.println("Koneksi ke database 'cityrailgo' berhasil!");
        } else {
            System.out.println("Koneksi gagal. Cek pesan error di atas.");
        }
        DBConnection.closeConnection();
    }
}