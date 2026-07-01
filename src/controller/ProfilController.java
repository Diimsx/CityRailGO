package controller;

import dao.TiketDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import model.Penumpang;
import util.AvatarManager;
import util.PenumpangSession;
import util.SceneManager;
import util.SessionManager;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProfilController implements Initializable {

    @FXML private Label lblNamaTopbar;
    @FXML private FontIcon topbarIcon;
    @FXML private ImageView topbarAvatar;

    @FXML private FontIcon avatarIcon;
    @FXML private ImageView mainAvatar;
    @FXML private Label lblNamaLengkap;
    @FXML private Label lblUsername;
    @FXML private Label lblStatTiket;
    @FXML private Label lblStatAktif;
    @FXML private Label lblFotoPath;

    @FXML private TextField tfUsername;
    @FXML private TextField tfNamaLengkap;
    @FXML private TextField tfEmail;
    @FXML private TextField tfNik;
    @FXML private TextField tfTelp;
    @FXML private ComboBox<String> cbJenisKelamin;
    @FXML private Label lblStatusData;

    @FXML private PasswordField pfPasswordLama;
    @FXML private PasswordField pfPasswordBaru;
    @FXML private PasswordField pfPasswordKonfirmasi;
    @FXML private Label lblStatusPassword;

    @FXML private Label lblNikInfo;

    private final UserDAO  userDAO  = new UserDAO();
    private final TiketDAO tiketDAO = new TiketDAO();
    private Penumpang penumpang;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = SessionManager.getInstance().getCurrentUser();
        lblNamaTopbar.setText(user.getUsername());

        if (!(user instanceof Penumpang p)) {
            SceneManager.switchScene("HomePenumpang.fxml");
            return;
        }
        penumpang = p;

        cbJenisKelamin.setItems(FXCollections.observableArrayList("Laki-laki", "Perempuan"));

        muatDataProfil();
        muatStatistik();
    }

    private void muatDataProfil() {
        String nama = penumpang.getNamaLengkap();
        lblNamaLengkap.setText(nama != null && !nama.isEmpty() ? nama : penumpang.getUsername());
        lblUsername.setText("@" + penumpang.getUsername());

        tfUsername.setText(penumpang.getUsername());
        tfNamaLengkap.setText(nama != null ? nama : "");
        tfEmail.setText(penumpang.getEmail() != null ? penumpang.getEmail() : "");
        tfNik.setText(penumpang.getNik() != null ? penumpang.getNik() : "");
        tfTelp.setText(penumpang.getNoTelepon() != null ? penumpang.getNoTelepon() : "");

        String jk = penumpang.getJenisKelamin();
        if (jk != null && !jk.isEmpty()) cbJenisKelamin.setValue(jk);

        lblNikInfo.setText(penumpang.getNik() != null && !penumpang.getNik().isEmpty()
                ? penumpang.getNik() : "Belum diisi");
                
        AvatarManager.loadAvatar(penumpang.getUsername(), topbarAvatar, topbarIcon, 24);
        AvatarManager.loadAvatar(penumpang.getUsername(), mainAvatar, avatarIcon, 120);
    }

    private void muatStatistik() {
        List<?> tikets = tiketDAO.findByPenumpang(penumpang);
        long aktif = tikets.stream()
                .filter(t -> t instanceof model.Tiket tk && "AKTIF".equalsIgnoreCase(tk.getStatus()))
                .count();
        lblStatTiket.setText(String.valueOf(tikets.size()));
        lblStatAktif.setText(String.valueOf(aktif));
    }

    @FXML
    private void handleUbahFoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Foto Profil");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fc.showOpenDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            lblFotoPath.setText("Foto: " + file.getName());
            lblFotoPath.setVisible(true);
            lblFotoPath.setManaged(true);

            AvatarManager.saveAvatar(penumpang.getUsername(), file);
            AvatarManager.loadAvatar(penumpang.getUsername(), topbarAvatar, topbarIcon, 24);
            AvatarManager.loadAvatar(penumpang.getUsername(), mainAvatar, avatarIcon, 120);
            
            tampilkanStatusData("✓ Foto profil berhasil diperbarui.", true);
        }
    }

    @FXML
    private void handleSimpanData() {
        String nama  = tfNamaLengkap.getText().trim();
        String email = tfEmail.getText().trim();
        String nik   = tfNik.getText().trim();
        String telp  = tfTelp.getText().trim();
        String jk    = cbJenisKelamin.getValue();

        if (nama.isEmpty()) {
            tampilkanStatusData("Nama lengkap tidak boleh kosong.", false);
            return;
        }
        if (!email.isEmpty() && !email.contains("@")) {
            tampilkanStatusData("Format email tidak valid.", false);
            return;
        }
        if (!nik.isEmpty() && nik.length() != 16) {
            tampilkanStatusData("NIK harus 16 digit.", false);
            return;
        }

        penumpang.setNamaLengkap(nama);
        penumpang.setEmail(email);
        if (jk != null) penumpang.setJenisKelamin(jk);

        boolean berhasil = userDAO.update(penumpang, nik, telp);
        if (berhasil) {
            tampilkanStatusData("✓ Data diri berhasil disimpan.", true);
            lblNamaLengkap.setText(nama);
            lblNikInfo.setText(nik.isEmpty() ? "Belum diisi" : nik);
        } else {
            tampilkanStatusData("Gagal menyimpan data. Silakan coba lagi.", false);
        }
    }

    @FXML
    private void handleGantiPassword() {
        String lama  = pfPasswordLama.getText();
        String baru  = pfPasswordBaru.getText();
        String konfirmasi = pfPasswordKonfirmasi.getText();

        if (lama.isEmpty() || baru.isEmpty() || konfirmasi.isEmpty()) {
            tampilkanStatusPassword("Semua field password harus diisi.", false);
            return;
        }
        if (!penumpang.getPassword().equals(lama)) {
            tampilkanStatusPassword("Password lama tidak sesuai.", false);
            return;
        }
        if (baru.length() < 6) {
            tampilkanStatusPassword("Password baru minimal 6 karakter.", false);
            return;
        }
        if (!baru.equals(konfirmasi)) {
            tampilkanStatusPassword("Konfirmasi password tidak cocok.", false);
            return;
        }

        boolean berhasil = userDAO.updatePassword(penumpang, baru);
        if (berhasil) {
            penumpang.setPassword(baru);
            pfPasswordLama.clear();
            pfPasswordBaru.clear();
            pfPasswordKonfirmasi.clear();
            tampilkanStatusPassword("✓ Password berhasil diubah.", true);
        } else {
            tampilkanStatusPassword("Gagal mengubah password. Coba lagi.", false);
        }
    }

    private void tampilkanStatusData(String pesan, boolean sukses) {
        lblStatusData.setText(pesan);
        lblStatusData.getStyleClass().removeAll("success-label", "error-label");
        lblStatusData.getStyleClass().add(sukses ? "success-label" : "error-label");
        lblStatusData.setVisible(true);
        lblStatusData.setManaged(true);
    }

    private void tampilkanStatusPassword(String pesan, boolean sukses) {
        lblStatusPassword.setText(pesan);
        lblStatusPassword.getStyleClass().removeAll("success-label", "error-label");
        lblStatusPassword.getStyleClass().add(sukses ? "success-label" : "error-label");
        lblStatusPassword.setVisible(true);
        lblStatusPassword.setManaged(true);
    }

    @FXML private void handleNavBeranda()   { SceneManager.switchScene("HomePenumpang.fxml"); }
    @FXML private void handleNavTiketSaya() { SceneManager.switchScene("TiketSaya.fxml"); }

    @FXML
    private void handleLogout() {
        Alert k = new Alert(Alert.AlertType.CONFIRMATION);
        k.setHeaderText(null);
        k.setContentText("Yakin ingin logout?");
        Optional<ButtonType> h = k.showAndWait();
        if (h.isPresent() && h.get() == ButtonType.OK) {
            PenumpangSession.reset();
            SessionManager.getInstance().logout();
            SceneManager.switchScene("login.fxml");
        }
    }
}
