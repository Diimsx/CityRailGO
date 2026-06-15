import util.DBConnection;
import java.sql.Connection;
import dao.UserDAO;
import java.time.LocalDate;
import model.Penumpang;
import model.User;


public class main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        // Test save Penumpang
        Penumpang p = new Penumpang("budi123", "rahasia", "budi@mail.com", "Budi Santoso", "08123456789", "3201xxxxxxxxxxxx", LocalDate.of(2000, 5, 10), "Laki-laki");
        boolean berhasil = userDAO.save(p);
        System.out.println("Save: " + berhasil + ", ID baru: " + p.getId());

        // Test findByUsername
        User hasil = userDAO.findByUsername("budi123");
        System.out.println("Role: " + hasil.getRole() + ", Nama: " + hasil.getNamaLengkap());
    }
}