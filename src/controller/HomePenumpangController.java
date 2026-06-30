package controller;

import dao.JadwalDAO;
import dao.PromoDAO;
import dao.RuteDAO;
import dao.StasiunDAO;
import dao.TiketDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import model.Penumpang;
import model.Promo;
import model.Stasiun;
import util.AvatarManager;
import util.PenumpangSession;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class HomePenumpangController implements Initializable {

    @FXML private Label               lblNama;
    @FXML private FontIcon            topbarIcon;
    @FXML private ImageView           topbarAvatar;
    @FXML private Label               lblStatJadwal;
    @FXML private Label               lblStatRute;
    @FXML private Label               lblStatTiketSaya;
    @FXML private ComboBox<Stasiun>   cbAsal;
    @FXML private ComboBox<Stasiun>   cbTujuan;
    @FXML private DatePicker          dpTanggal;
    @FXML private Spinner<Integer>    spJumlah;
    @FXML private Label               lblError;
    @FXML private VBox                vboxPromo;
    @FXML private Label               lblNoPromo;

    private static final DateTimeFormatter FMT_EXP = DateTimeFormatter.ofPattern("dd MMM yyyy",
            new Locale("id", "ID"));

    private final StasiunDAO stasiunDAO = new StasiunDAO();
    private final JadwalDAO  jadwalDAO  = new JadwalDAO();
    private final RuteDAO    ruteDAO    = new RuteDAO();
    private final TiketDAO   tiketDAO   = new TiketDAO();
    private final PromoDAO   promoDAO   = new PromoDAO();

    // Warna palette for promo cards (cycling)
    private static final String[] PROMO_STYLES   = {"blue","green","orange","red","purple"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user = SessionManager.getInstance().getCurrentUser();
        lblNama.setText(user.getUsername());
        AvatarManager.loadAvatar(user.getUsername(), topbarAvatar, topbarIcon, 24);

        // Isi combo stasiun
        List<Stasiun> stasiuns = stasiunDAO.findAll();
        cbAsal.setItems(FXCollections.observableArrayList(stasiuns));
        cbTujuan.setItems(FXCollections.observableArrayList(stasiuns));

        javafx.util.Callback<ListView<Stasiun>, ListCell<Stasiun>> factory = lv -> new ListCell<>() {
            @Override protected void updateItem(Stasiun s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNamaStasiun() + " [" + s.getKodeStasiun() + "]");
            }
        };
        cbAsal.setCellFactory(factory);
        cbTujuan.setCellFactory(factory);
        cbAsal.setButtonCell(factory.call(null));
        cbTujuan.setButtonCell(factory.call(null));

        spJumlah.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1));
        spJumlah.setEditable(false);
        dpTanggal.setValue(LocalDate.now());

        muatStatistik();
        muatPromo();
    }

    // ===== Data loaders =====

    private void muatStatistik() {
        lblStatJadwal.setText(String.valueOf(jadwalDAO.findAll().stream()
                .filter(j -> "TERSEDIA".equals(j.getStatus())).count()));
        lblStatRute.setText(String.valueOf(ruteDAO.findAll().size()));

        var user = SessionManager.getInstance().getCurrentUser();
        if (user instanceof Penumpang p) {
            lblStatTiketSaya.setText(String.valueOf(tiketDAO.findByPenumpang(p).size()));
        } else {
            lblStatTiketSaya.setText("0");
        }
    }

    private void muatPromo() {
        List<Promo> promos = promoDAO.findAll().stream()
                .filter(Promo::isValid)
                .toList();

        if (promos.isEmpty()) {
            lblNoPromo.setVisible(true);
            lblNoPromo.setManaged(true);
            return;
        }

        vboxPromo.getChildren().clear();
        for (int i = 0; i < promos.size(); i++) {
            vboxPromo.getChildren().add(buatPromoCard(promos.get(i), PROMO_STYLES[i % PROMO_STYLES.length]));
        }
    }

    private VBox buatPromoCard(Promo promo, String warna) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("promo-item", "promo-item-" + warna);

        // Baris 1: kode + badge diskon
        HBox baris1 = new HBox(8);
        baris1.setAlignment(Pos.CENTER_LEFT);

        Label lblKode = new Label(promo.getKodePromo());
        lblKode.getStyleClass().addAll("promo-kode-" + warna);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label badge = new Label((int) promo.getDiskonPersen() + "% OFF");
        badge.getStyleClass().addAll("promo-badge", "promo-badge-diskon");

        baris1.getChildren().addAll(lblKode, spacer, badge);

        // Baris 2: deskripsi
        Label lblDesc = new Label(promo.getDeskripsi());
        lblDesc.getStyleClass().add("promo-desc");
        lblDesc.setWrapText(true);

        // Baris 3: tanggal berakhir
        Label lblExp = new Label("Berlaku s/d " + promo.getTanggalBerakhir().format(FMT_EXP));
        lblExp.getStyleClass().add("promo-exp");

        // Tombol salin kode
        Button btnSalin = new Button("Salin Kode");
        btnSalin.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-text-fill: #4D8AFF; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSalin.setOnAction(e -> {
            javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(promo.getKodePromo());
            cb.setContent(content);
            btnSalin.setText("✓ Tersalin!");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            pause.setOnFinished(ev -> btnSalin.setText("Salin Kode"));
            pause.play();
        });

        card.getChildren().addAll(baris1, lblDesc, lblExp, btnSalin);
        return card;
    }

    // ===== Search =====

    @FXML
    private void handleSwapStasiun() {
        Stasiun asal   = cbAsal.getValue();
        Stasiun tujuan = cbTujuan.getValue();
        cbAsal.setValue(tujuan);
        cbTujuan.setValue(asal);
    }

    @FXML
    private void handleCariJadwal() {
        Stasiun asal   = cbAsal.getValue();
        Stasiun tujuan = cbTujuan.getValue();
        LocalDate tgl  = dpTanggal.getValue();

        if (asal == null)                         { tampilkanError("Pilih stasiun asal."); return; }
        if (tujuan == null)                       { tampilkanError("Pilih stasiun tujuan."); return; }
        if (asal.getId() == tujuan.getId())       { tampilkanError("Asal dan tujuan tidak boleh sama."); return; }
        if (tgl == null)                          { tampilkanError("Pilih tanggal keberangkatan."); return; }
        if (tgl.isBefore(LocalDate.now()))        { tampilkanError("Tanggal tidak boleh di masa lalu."); return; }

        sembunyikanError();
        PenumpangSession.setStasiunAsal(asal.getNamaStasiun());
        PenumpangSession.setStasiunTujuan(tujuan.getNamaStasiun());
        PenumpangSession.setTanggalBerangkat(tgl);
        PenumpangSession.setJumlahPenumpang(spJumlah.getValue());

        SceneManager.switchScene("PilihJadwal.fxml");
    }

    // ===== Nav =====

    @FXML private void handleNavBeranda()   { /* sudah di sini */ }
    @FXML private void handleNavTiketSaya() { SceneManager.switchScene("TiketSaya.fxml"); }
    @FXML private void handleNavProfil()    { SceneManager.switchScene("Profil.fxml"); }

    @FXML
    private void handleLogout() {
        Alert k = new Alert(Alert.AlertType.CONFIRMATION);
        k.setTitle("Logout"); k.setHeaderText(null);
        k.setContentText("Yakin ingin logout?");
        Optional<ButtonType> h = k.showAndWait();
        if (h.isPresent() && h.get() == ButtonType.OK) {
            PenumpangSession.reset();
            SessionManager.getInstance().logout();
            SceneManager.switchScene("login.fxml");
        }
    }

    // ===== Helpers =====
    private void tampilkanError(String p) { lblError.setText(p); lblError.setVisible(true); lblError.setManaged(true); }
    private void sembunyikanError()       { lblError.setVisible(false); lblError.setManaged(false); }
}