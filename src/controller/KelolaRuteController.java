package controller;

import dao.RuteDAO;
import dao.StasiunDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Rute;
import model.Stasiun;
import org.kordamp.ikonli.javafx.FontIcon;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class KelolaRuteController implements Initializable {

    @FXML private Label lblAdminName;
    @FXML private TextField tfSearch;

    @FXML private TableView<Rute> tblRute;
    @FXML private TableColumn<Rute, Integer> colNo;
    @FXML private TableColumn<Rute, String>  colNamaRute;
    @FXML private TableColumn<Rute, String>  colRute;
    @FXML private TableColumn<Rute, Double>  colJarak;
    @FXML private TableColumn<Rute, Integer> colEstimasi;
    @FXML private TableColumn<Rute, Void>    colAksi;

    // Modal fields
    @FXML private StackPane   modalOverlay;
    @FXML private Label       lblModalTitle;
    @FXML private TextField   tfNamaRute;
    @FXML private ComboBox<Stasiun> cbStasiunAsal;
    @FXML private ComboBox<Stasiun> cbStasiunTujuan;
    @FXML private TextField   tfJarakKm;
    @FXML private TextField   tfEstimasiMenit;
    @FXML private VBox        vboxStops;
    @FXML private Label       lblModalError;
    @FXML private Button      btnSimpan;

    private final RuteDAO    ruteDAO    = new RuteDAO();
    private final StasiunDAO stasiunDAO = new StasiunDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Rute>         daftarRute;
    private FilteredList<Rute>           daftarRuteTersaring;
    private ObservableList<Stasiun>      daftarStasiun;

    private Rute ruteDiedit;

    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());

        daftarStasiun = FXCollections.observableArrayList(stasiunDAO.findAll());
        setupComboStasiun(cbStasiunAsal);
        setupComboStasiun(cbStasiunTujuan);

        setupTabel();
        muatDataRute();
        setupPencarian();
    }

    private void setupComboStasiun(ComboBox<Stasiun> cb) {
        cb.setItems(daftarStasiun);
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Stasiun s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNamaStasiun() + " [" + s.getKodeStasiun() + "] — " + s.getKota());
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Stasiun s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNamaStasiun() + " [" + s.getKodeStasiun() + "]");
            }
        });
    }

    // =========================================================
    // TABEL
    // =========================================================

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(tblRute.getItems().indexOf(data.getValue()) + 1));

        colNamaRute.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaRute()));

        colRute.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getPreviewUrutan()));

        colJarak.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getJarakKm()));
        colJarak.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double jarak, boolean kosong) {
                super.updateItem(jarak, kosong);
                setText(kosong || jarak == null ? null : String.format("%.0f km", jarak));
            }
        });

        colEstimasi.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEstimasiMenit()));
        colEstimasi.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer menit, boolean kosong) {
                super.updateItem(menit, kosong);
                setText(kosong || menit == null ? null : formatEstimasi(menit));
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final FontIcon iconEdit  = new FontIcon("fas-edit");
            private final FontIcon iconHapus = new FontIcon("fas-trash-alt");
            private final HBox container     = new HBox(14, iconEdit, iconHapus);

            {
                iconEdit.getStyleClass().add("action-icon-edit");
                iconHapus.getStyleClass().add("action-icon-delete");
                container.setAlignment(Pos.CENTER_LEFT);
                iconEdit.setOnMouseClicked(e  -> handleEditRute(getTableRow().getItem()));
                iconHapus.setOnMouseClicked(e -> handleHapusRute(getTableRow().getItem()));
            }

            @Override protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : container);
            }
        });
    }

    private String formatEstimasi(int menit) {
        int jam = menit / 60;
        int sisa = menit % 60;
        return jam <= 0 ? sisa + " menit" : jam + " jam " + sisa + " menit";
    }

    private void muatDataRute() {
        daftarRute = FXCollections.observableArrayList(ruteDAO.findAll());
        daftarRuteTersaring = new FilteredList<>(daftarRute, r -> true);
        tblRute.setItems(daftarRuteTersaring);
    }

    private void setupPencarian() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> {
            String q = baru == null ? "" : baru.trim().toLowerCase();
            daftarRuteTersaring.setPredicate(rute ->
                q.isEmpty()
                || rute.getNamaRute().toLowerCase().contains(q)
                || rute.getStasiunAsal().toLowerCase().contains(q)
                || rute.getStasiunTujuan().toLowerCase().contains(q)
                || rute.getPreviewUrutan().toLowerCase().contains(q)
            );
        });
    }

    // =========================================================
    // CRUD HANDLERS
    // =========================================================

    @FXML
    private void handleTambahRute() {
        ruteDiedit = null;
        lblModalTitle.setText("Buat Rute Baru");
        btnSimpan.setText("Simpan");
        kosongkanForm();
        tampilkanModal();
    }

    private void handleEditRute(Rute rute) {
        if (rute == null) return;
        ruteDiedit = rute;
        lblModalTitle.setText("Edit Rute");
        btnSimpan.setText("Perbarui");

        tfNamaRute.setText(rute.getNamaRute());
        cbStasiunAsal.setValue(cariStasiunById(rute.getStasiunAsalObj()));
        cbStasiunTujuan.setValue(cariStasiunById(rute.getStasiunTujuanObj()));
        tfJarakKm.setText(String.valueOf(rute.getJarakKm()));
        tfEstimasiMenit.setText(String.valueOf(rute.getEstimasiMenit()));

        // Load existing stops ke form
        vboxStops.getChildren().clear();
        for (Stasiun stop : rute.getStasiunPemberhentian()) {
            tambahBarisStop(stop);
        }

        sembunyikanError();
        tampilkanModal();
    }

    private void handleHapusRute(Rute rute) {
        if (rute == null) return;

        ButtonType btnYa    = new ButtonType("Ya, Hapus", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTidak = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> konfirmasi = new Dialog<>();
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus rute \"" + rute.getNamaRute() + "\"?");
        konfirmasi.setContentText("Semua data pemberhentian rute ini akan ikut terhapus secara permanen.");
        konfirmasi.getDialogPane().getButtonTypes().addAll(btnYa, btnTidak);

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isEmpty() || hasil.get() != btnYa) return;

        boolean berhasil = adminController.hapusRute(rute.getId());
        if (berhasil) {
            daftarRute.remove(rute);
        } else {
            tampilkanAlertError("Gagal menghapus rute. Pastikan rute tidak terpakai di jadwal aktif.");
        }
    }

    @FXML
    private void handleSimpanRute() {
        String namaRute      = tfNamaRute.getText().trim();
        Stasiun stasiunAsal  = cbStasiunAsal.getValue();
        Stasiun stasiunTujuan = cbStasiunTujuan.getValue();
        String teksJarak     = tfJarakKm.getText().trim();
        String teksEstimasi  = tfEstimasiMenit.getText().trim();

        // --- Validasi field wajib ---
        if (namaRute.isEmpty()) {
            tampilkanError("Nama rute tidak boleh kosong.");
            return;
        }
        if (stasiunAsal == null) {
            tampilkanError("Pilih stasiun asal.");
            return;
        }
        if (stasiunTujuan == null) {
            tampilkanError("Pilih stasiun tujuan.");
            return;
        }
        if (stasiunAsal.getId() == stasiunTujuan.getId()) {
            tampilkanError("Stasiun asal dan tujuan tidak boleh sama.");
            return;
        }

        double jarakKm;
        try {
            jarakKm = Double.parseDouble(teksJarak);
            if (jarakKm <= 0) { tampilkanError("Jarak harus berupa angka positif."); return; }
        } catch (NumberFormatException e) {
            tampilkanError("Jarak harus berupa angka."); return;
        }

        int estimasiMenit;
        try {
            estimasiMenit = Integer.parseInt(teksEstimasi);
            if (estimasiMenit <= 0) { tampilkanError("Estimasi waktu harus berupa angka positif."); return; }
        } catch (NumberFormatException e) {
            tampilkanError("Estimasi waktu harus berupa angka (dalam menit)."); return;
        }

        // --- Kumpulkan stops dari vboxStops ---
        List<Stasiun> stops = kumpulkanStops();
        if (stops == null) return; // validasi gagal, error sudah ditampilkan

        // --- Validasi duplikat stop ---
        String pesanDuplikat = validasiDuplikatStop(stasiunAsal, stasiunTujuan, stops);
        if (pesanDuplikat != null) {
            tampilkanError(pesanDuplikat);
            return;
        }

        // --- Buat objek Rute ---
        Rute ruteBaru = new Rute(namaRute, stasiunAsal, stasiunTujuan, jarakKm, estimasiMenit);
        ruteBaru.setStasiunPemberhentian(stops);

        boolean berhasil;
        if (ruteDiedit == null) {
            berhasil = adminController.tambahRute(ruteBaru);
            if (berhasil) daftarRute.add(ruteBaru);
        } else {
            ruteBaru.setId(ruteDiedit.getId());
            berhasil = adminController.editRute(ruteBaru);
            if (berhasil) {
                int indeks = daftarRute.indexOf(ruteDiedit);
                if (indeks >= 0) daftarRute.set(indeks, ruteBaru);
            }
        }

        if (berhasil) {
            tutupModal();
        } else {
            tampilkanError("Gagal menyimpan data rute. Silakan coba lagi.");
        }
    }

    // =========================================================
    // DYNAMIC STOPS (Stasiun Pemberhentian)
    // =========================================================

    @FXML
    private void handleTambahStop() {
        tambahBarisStop(null);
    }

    /**
     * Menambah satu baris ComboBox pemberhentian ke vboxStops.
     * @param preselect Stasiun yang sudah dipilih sebelumnya (untuk mode Edit), atau null.
     */
    private void tambahBarisStop(Stasiun preselect) {
        int urutan = vboxStops.getChildren().size() + 1;

        // Badge urutan
        Label lblUrutan = new Label(String.valueOf(urutan));
        lblUrutan.getStyleClass().add("stop-index");

        // ComboBox pilih stasiun
        ComboBox<Stasiun> cbStop = new ComboBox<>(daftarStasiun);
        cbStop.getStyleClass().add("stop-combo");
        cbStop.setMaxWidth(Double.MAX_VALUE);
        cbStop.setPromptText("Pilih stasiun transit...");
        setupComboStasiun(cbStop);
        if (preselect != null) {
            cbStop.setValue(cariStasiunById(preselect));
        }

        // Tombol hapus baris
        Button btnHapus = new Button("×");
        btnHapus.getStyleClass().add("btn-remove-stop");

        HBox baris = new HBox(8, lblUrutan, cbStop, btnHapus);
        baris.setAlignment(Pos.CENTER_LEFT);
        baris.getStyleClass().add("transit-row");
        HBox.setHgrow(cbStop, javafx.scene.layout.Priority.ALWAYS);

        btnHapus.setOnAction(e -> {
            vboxStops.getChildren().remove(baris);
            renumberStops(); // update badge urutan setelah hapus
        });

        vboxStops.getChildren().add(baris);
    }

    /** Update semua badge nomor urutan setelah penghapusan baris. */
    private void renumberStops() {
        for (int i = 0; i < vboxStops.getChildren().size(); i++) {
            if (vboxStops.getChildren().get(i) instanceof HBox row) {
                if (!row.getChildren().isEmpty() && row.getChildren().get(0) instanceof Label lbl) {
                    lbl.setText(String.valueOf(i + 1));
                }
            }
        }
    }

    /**
     * Mengambil daftar Stasiun dari semua baris stop di vboxStops.
     * Mengembalikan null jika ada baris yang belum dipilih.
     */
    @SuppressWarnings("unchecked")
    private List<Stasiun> kumpulkanStops() {
        List<Stasiun> stops = new ArrayList<>();
        for (int i = 0; i < vboxStops.getChildren().size(); i++) {
            if (vboxStops.getChildren().get(i) instanceof HBox row) {
                // ComboBox ada di index 1 dalam HBox (setelah Label badge)
                if (row.getChildren().size() > 1 && row.getChildren().get(1) instanceof ComboBox<?> cb) {
                    Stasiun s = (Stasiun) cb.getValue();
                    if (s == null) {
                        tampilkanError("Pemberhentian ke-" + (i + 1) + " belum dipilih. Pilih stasiun atau hapus baris tersebut.");
                        return null;
                    }
                    stops.add(s);
                }
            }
        }
        return stops;
    }

    /**
     * Validasi: tidak boleh ada duplikat stasiun dalam satu rute
     * (asal, semua stops, tujuan harus unik).
     */
    private String validasiDuplikatStop(Stasiun asal, Stasiun tujuan, List<Stasiun> stops) {
        Set<Integer> idSet = new HashSet<>();
        idSet.add(asal.getId());
        for (Stasiun s : stops) {
            if (!idSet.add(s.getId())) {
                return "Stasiun \"" + s.getNamaStasiun() + "\" muncul lebih dari satu kali dalam rute ini.";
            }
        }
        if (!idSet.add(tujuan.getId())) {
            return "Stasiun tujuan \"" + tujuan.getNamaStasiun() + "\" sudah ada di urutan pemberhentian sebelumnya.";
        }
        return null;
    }

    // =========================================================
    // MODAL HELPERS
    // =========================================================

    @FXML
    private void handleTutupModal() { tutupModal(); }

    private void tampilkanModal() {
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void tutupModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        ruteDiedit = null;
        btnSimpan.setText("Simpan");
    }

    private void kosongkanForm() {
        tfNamaRute.clear();
        cbStasiunAsal.setValue(null);
        cbStasiunTujuan.setValue(null);
        tfJarakKm.clear();
        tfEstimasiMenit.clear();
        vboxStops.getChildren().clear();
        sembunyikanError();
    }

    // =========================================================
    // UTILS
    // =========================================================

    /** Cari Stasiun di daftarStasiun berdasarkan ID dari objek yang diberikan. */
    private Stasiun cariStasiunById(Stasiun ref) {
        if (ref == null) return null;
        return daftarStasiun.stream()
            .filter(s -> s.getId() == ref.getId())
            .findFirst().orElse(null);
    }

    private void tampilkanError(String pesan) {
        lblModalError.setText(pesan);
        lblModalError.setVisible(true);
        lblModalError.setManaged(true);
    }

    private void sembunyikanError() {
        lblModalError.setText("");
        lblModalError.setVisible(false);
        lblModalError.setManaged(false);
    }

    private void tampilkanAlertError(String pesan) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Terjadi Kesalahan");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    // =========================================================
    // NAVIGASI
    // =========================================================

    @FXML private void handleNavDashboard() { SceneManager.switchScene("HomeAdmin.fxml"); }
    @FXML private void handleNavKereta()    { SceneManager.switchScene("KelolaKereta.fxml"); }
    @FXML private void handleNavRute()      { /* sudah berada di halaman ini */ }
    @FXML private void handleNavStasiun()   { SceneManager.switchScene("KelolaStasiun.fxml"); }
    @FXML private void handleNavJadwal()    { SceneManager.switchScene("KelolaJadwal.fxml"); }
    @FXML private void handleNavPromo()     { SceneManager.switchScene("KelolaPromo.fxml"); }
    @FXML private void handleNavLaporan()   { SceneManager.switchScene("LaporanPenjualan.fxml"); }

    @FXML
    private void handleLogout() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Logout");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Yakin logout?");
        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            SceneManager.switchScene("login.fxml");
        }
    }
}