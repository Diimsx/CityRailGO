package controller;

import dao.PromoDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.Promo;
import org.kordamp.ikonli.javafx.FontIcon;
import util.DateTimeUtil;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaPromoController implements Initializable {

    @FXML private Label lblAdminName;
    @FXML private TextField tfSearch;

    @FXML private TableView<Promo> tblPromo;
    @FXML private TableColumn<Promo, Integer> colNo;
    @FXML private TableColumn<Promo, String> colKodePromo;
    @FXML private TableColumn<Promo, String> colDeskripsi;
    @FXML private TableColumn<Promo, String> colDiskon;
    @FXML private TableColumn<Promo, String> colPeriode;
    @FXML private TableColumn<Promo, String> colStatus;
    @FXML private TableColumn<Promo, Void> colAksi;

    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitle;
    @FXML private TextField tfKodePromo;
    @FXML private TextArea taDeskripsi;
    @FXML private TextField tfDiskonPersen;
    @FXML private DatePicker dpTanggalMulai;
    @FXML private DatePicker dpTanggalBerakhir;
    @FXML private CheckBox chkAktif;
    @FXML private Label lblModalError;
    @FXML private Button btnSimpan;

    private final PromoDAO promoDAO = new PromoDAO();
    private final AdminController adminController = new AdminController();

    private ObservableList<Promo> daftarPromo;
    private FilteredList<Promo> daftarPromoTersaring;

    private Promo promoDiedit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAdminName.setText("Halo, " + SessionManager.getInstance().getCurrentUser().getNamaLengkap());

        setupTabel();
        muatDataPromo();
        setupPencarian();
    }

    private void setupTabel() {
        colNo.setCellValueFactory(data ->
                new SimpleObjectProperty<>(tblPromo.getItems().indexOf(data.getValue()) + 1));

        colKodePromo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getKodePromo()));

        colDeskripsi.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDeskripsi()));

        colDiskon.setCellValueFactory(data ->
                new SimpleStringProperty(formatDiskon(data.getValue().getDiskonPersen())));
        colDiskon.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String diskon, boolean kosong) {
                super.updateItem(diskon, kosong);
                if (kosong || diskon == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(diskon);
                    label.getStyleClass().add("diskon-value");
                    setGraphic(label);
                }
            }
        });

        colPeriode.setCellValueFactory(data -> {
            Promo promo = data.getValue();
            return new SimpleStringProperty(
                    DateTimeUtil.formatTanggal(promo.getTanggalMulai())
                            + " - " + DateTimeUtil.formatTanggal(promo.getTanggalBerakhir())
            );
        });

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(statusPromo(data.getValue())));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean kosong) {
                super.updateItem(status, kosong);
                if (kosong || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("badge-status");
                badge.getStyleClass().add(kelasBadgeStatus(status));
                setGraphic(badge);
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final FontIcon iconEdit = new FontIcon("fas-edit");
            private final FontIcon iconHapus = new FontIcon("fas-trash-alt");
            private final HBox container = new HBox(14, iconEdit, iconHapus);

            {
                iconEdit.getStyleClass().add("action-icon-edit");
                iconHapus.getStyleClass().add("action-icon-delete");
                container.setAlignment(Pos.CENTER_LEFT);

                iconEdit.setOnMouseClicked(e -> handleEditPromo(getTableRow().getItem()));
                iconHapus.setOnMouseClicked(e -> handleHapusPromo(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : container);
            }
        });
    }

    private String formatDiskon(double diskon) {
        if (diskon == Math.floor(diskon)) {
            return String.format("%.0f%%", diskon);
        }
        return String.format("%.1f%%", diskon);
    }

    private String statusPromo(Promo promo) {
        LocalDate today = LocalDate.now();
        if (!promo.isAktif()) {
            return "Nonaktif";
        }
        if (today.isBefore(promo.getTanggalMulai())) {
            return "Akan Datang";
        }
        if (today.isAfter(promo.getTanggalBerakhir())) {
            return "Kadaluarsa";
        }
        return "Berlaku";
    }

    private String kelasBadgeStatus(String status) {
        return switch (status) {
            case "Berlaku" -> "badge-berlaku";
            case "Akan Datang" -> "badge-akan-datang";
            case "Kadaluarsa" -> "badge-kadaluarsa";
            default -> "badge-nonaktif";
        };
    }

    private void muatDataPromo() {
        daftarPromo = FXCollections.observableArrayList(promoDAO.findAll());
        daftarPromoTersaring = new FilteredList<>(daftarPromo, p -> true);
        tblPromo.setItems(daftarPromoTersaring);
    }

    private void setupPencarian() {
        tfSearch.textProperty().addListener((obs, lama, baru) -> {
            String kueri = baru == null ? "" : baru.trim().toLowerCase();
            daftarPromoTersaring.setPredicate(promo ->
                    kueri.isEmpty()
                            || promo.getKodePromo().toLowerCase().contains(kueri)
                            || promo.getDeskripsi().toLowerCase().contains(kueri)
            );
        });
    }

    @FXML
    private void handleTambahPromo() {
        promoDiedit = null;
        lblModalTitle.setText("Tambah Promo");
        kosongkanForm();
        tampilkanModal();
    }

    private void handleEditPromo(Promo promo) {
        if (promo == null) {
            return;
        }
        promoDiedit = promo;
        lblModalTitle.setText("Edit Promo");

        tfKodePromo.setText(promo.getKodePromo());
        taDeskripsi.setText(promo.getDeskripsi());
        tfDiskonPersen.setText(formatAngkaDiskon(promo.getDiskonPersen()));
        dpTanggalMulai.setValue(promo.getTanggalMulai());
        dpTanggalBerakhir.setValue(promo.getTanggalBerakhir());
        chkAktif.setSelected(promo.isAktif());

        sembunyikanError();
        tampilkanModal();
    }

    private String formatAngkaDiskon(double diskon) {
        return diskon == Math.floor(diskon) ? String.valueOf((int) diskon) : String.valueOf(diskon);
    }

    private void handleHapusPromo(Promo promo) {
        if (promo == null) {
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Promo");
        konfirmasi.setHeaderText(null);
        konfirmasi.setContentText("Apakah Anda yakin ingin menghapus promo \"" + promo.getKodePromo() + "\"?");

        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            boolean berhasil = adminController.hapusPromo(promo.getId());
            if (berhasil) {
                daftarPromo.remove(promo);
            } else {
                tampilkanAlertError("Gagal menghapus promo. Pastikan promo tidak terpakai pada riwayat pembayaran.");
            }
        }
    }

    @FXML
    private void handleSimpanPromo() {
        String kodePromo = tfKodePromo.getText().trim().toUpperCase();
        String deskripsi = taDeskripsi.getText().trim();
        String teksDiskon = tfDiskonPersen.getText().trim();
        LocalDate tanggalMulai = dpTanggalMulai.getValue();
        LocalDate tanggalBerakhir = dpTanggalBerakhir.getValue();
        boolean aktif = chkAktif.isSelected();

        if (kodePromo.isEmpty() || deskripsi.isEmpty() || teksDiskon.isEmpty()
                || tanggalMulai == null || tanggalBerakhir == null) {
            tampilkanError("Semua field wajib diisi.");
            return;
        }

        double diskonPersen;
        try {
            diskonPersen = Double.parseDouble(teksDiskon);
            if (diskonPersen <= 0 || diskonPersen > 100) {
                tampilkanError("Diskon harus berupa angka antara 1 dan 100.");
                return;
            }
        } catch (NumberFormatException e) {
            tampilkanError("Diskon harus berupa angka.");
            return;
        }

        if (tanggalBerakhir.isBefore(tanggalMulai)) {
            tampilkanError("Tanggal berakhir tidak boleh sebelum tanggal mulai.");
            return;
        }

        Promo promoDenganKodeSama = promoDAO.findByKode(kodePromo);
        boolean kodeDipakaiPromoLain = promoDenganKodeSama != null
                && (promoDiedit == null || promoDenganKodeSama.getId() != promoDiedit.getId());
        if (kodeDipakaiPromoLain) {
            tampilkanError("Kode promo \"" + kodePromo + "\" sudah digunakan.");
            return;
        }

        Promo promoBaru = new Promo(kodePromo, deskripsi, diskonPersen, tanggalMulai, tanggalBerakhir);
        promoBaru.setAktif(aktif);

        boolean berhasil;
        if (promoDiedit == null) {
            berhasil = adminController.tambahPromo(promoBaru);
            if (berhasil) {
                daftarPromo.add(promoBaru);
            }
        } else {
            promoBaru.setId(promoDiedit.getId());
            berhasil = adminController.editPromo(promoBaru);
            if (berhasil) {
                int indeks = daftarPromo.indexOf(promoDiedit);
                daftarPromo.set(indeks, promoBaru);
            }
        }

        if (berhasil) {
            tutupModal();
        } else {
            tampilkanError("Gagal menyimpan promo. Silakan coba lagi.");
        }
    }

    @FXML
    private void handleTutupModal() {
        tutupModal();
    }

    private void tampilkanModal() {
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void tutupModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        promoDiedit = null;
    }

    private void kosongkanForm() {
        tfKodePromo.clear();
        taDeskripsi.clear();
        tfDiskonPersen.clear();
        dpTanggalMulai.setValue(null);
        dpTanggalBerakhir.setValue(null);
        chkAktif.setSelected(true);
        sembunyikanError();
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

    @FXML
    private void handleNavDashboard() {
        SceneManager.switchScene("HomeAdmin.fxml");
    }

    @FXML
    private void handleNavKereta() {
        SceneManager.switchScene("KelolaKereta.fxml");
    }

    @FXML
    private void handleNavRute() {
        SceneManager.switchScene("KelolaRute.fxml");
    }

    @FXML
    private void handleNavJadwal() {
        SceneManager.switchScene("KelolaJadwal.fxml");
    }

    @FXML
    private void handleNavPromo() {
    }

    @FXML
    private void handleNavLaporan() {
        SceneManager.switchScene("LaporanPenjualan.fxml");
    }

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