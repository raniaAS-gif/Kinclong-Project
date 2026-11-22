package com.kinclong.controller.adminContent;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.kinclong.dao.KaryawanDAO;
import com.kinclong.dao.LayananDAO;
import com.kinclong.model.Admin;
import com.kinclong.model.Karyawan;
import com.kinclong.model.Kasir;
import com.kinclong.model.Layanan;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class KelolaMasterDataController {

    @FXML private TabPane tabPaneMasterData;
    @FXML private Tab tabPaketLayanan;
    @FXML private Tab tabDataKaryawan;

    // --- Komponen Paket Layanan ---
    @FXML private TextField txtIdLayanan;
    @FXML private TextField txtNamaLayanan;
    @FXML private TextField txtJenisSatuan;
    @FXML private TextField txtHargaPerUnit;
    @FXML private TextField txtCariLayanan;
    @FXML private TableView<Layanan> tblLayanan;
    @FXML private TableColumn<Layanan, String> colIdLayanan;
    @FXML private TableColumn<Layanan, String> colNamaLayanan;
    @FXML private TableColumn<Layanan, String> colSatuanLayanan;
    @FXML private TableColumn<Layanan, Double> colHargaLayanan;

    // --- Komponen Data Karyawan ---
    @FXML private TextField txtIdKaryawan;
    @FXML private TextField txtNamaKaryawan;
    @FXML private TextField txtNoTelpKaryawan;
    @FXML private MenuButton mbJabatan;
    @FXML private MenuItem miAdmin;
    @FXML private MenuItem miKasir;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtCariKaryawan;
    @FXML private TableView<Karyawan> tblKaryawan;
    @FXML private TableColumn<Karyawan, String> colIdKaryawan;
    @FXML private TableColumn<Karyawan, String> colNamaKaryawan;
    @FXML private TableColumn<Karyawan, String> colNoTelpKaryawan;
    @FXML private TableColumn<Karyawan, String> colJabatanKaryawan;
    @FXML private TableColumn<Karyawan, String> colUsernameKaryawan;

    private final LayananDAO layananDAO = new LayananDAO();
    private final KaryawanDAO karyawanDAO = new KaryawanDAO();
    private final ObservableList<Layanan> masterListLayanan = FXCollections.observableArrayList();
    private final ObservableList<Karyawan> masterListKaryawan = FXCollections.observableArrayList();

    private Layanan layananTerpilih = null;
    private Karyawan karyawanTerpilih = null;
    private String jabatanTerpilih = null;

    private final NumberFormat formatRupiah = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"));

    @FXML
    public void initialize() {
        formatRupiah.setMaximumFractionDigits(2);

        // Default awal saat diakses melalui sidebar: Kelola Layanan
        bukaTabLayanan();

        setupTabelLayanan();
        setupTabelKaryawan();

        if (miAdmin != null) {
            miAdmin.setOnAction(e -> setJabatan("ADMIN"));
        }
        if (miKasir != null) {
            miKasir.setOnAction(e -> setJabatan("KASIR"));
        }

        muatDataLayanan();
        muatDataKaryawan();
    }

    public void bukaTabLayanan() {
        tabPaneMasterData.getSelectionModel().select(tabPaketLayanan);
    }

    public void bukaTabKaryawan() {
        tabPaneMasterData.getSelectionModel().select(tabDataKaryawan);
    }

    // ================= BAGIAN PAKET LAYANAN =================
    private void setupTabelLayanan() {
        colIdLayanan.setCellValueFactory(c -> new SimpleStringProperty(String.format("L%03d", c.getValue().getIdLayanan())));
        colNamaLayanan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaLayanan()));
        colSatuanLayanan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJenisSatuan()));

        // Kolom Harga / Unit bertipe Double
        colHargaLayanan.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getHargaPerUnit()));
        colHargaLayanan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp " + formatRupiah.format(item));
                }
            }
        });
    }

    private void muatDataLayanan() {
        masterListLayanan.setAll(layananDAO.getAll());
        tblLayanan.setItems(masterListLayanan);
        generatePreviewIdLayanan();
    }

    private void generatePreviewIdLayanan() {
        int nextId = masterListLayanan.stream().mapToInt(Layanan::getIdLayanan).max().orElse(0) + 1;
        txtIdLayanan.setText(String.format("L%03d", nextId));
    }

    @FXML
    private void handlePilihBarisLayanan() {
        layananTerpilih = tblLayanan.getSelectionModel().getSelectedItem();
        if (layananTerpilih != null) {
            txtIdLayanan.setText(String.format("L%03d", layananTerpilih.getIdLayanan()));
            txtNamaLayanan.setText(layananTerpilih.getNamaLayanan());
            txtJenisSatuan.setText(layananTerpilih.getJenisSatuan());
            txtHargaPerUnit.setText(String.valueOf(layananTerpilih.getHargaPerUnit()));
        }
    }

    @FXML
    private void handleSimpanLayanan() {
        if (txtNamaLayanan.getText().isBlank() || txtJenisSatuan.getText().isBlank() || txtHargaPerUnit.getText().isBlank()) {
            tampilkanPesan(Alert.AlertType.WARNING, "Validasi", "Semua field layanan wajib diisi.");
            return;
        }
        try {
            double harga = Double.parseDouble(txtHargaPerUnit.getText().replace(",", ".").replaceAll("[^0-9.]", ""));
            Layanan baru = new Layanan(0, txtNamaLayanan.getText().trim(), txtJenisSatuan.getText().trim(), harga);
            if (layananDAO.insert(baru)) {
                tampilkanPesan(Alert.AlertType.INFORMATION, "Sukses", "Paket layanan berhasil ditambahkan.");
                handleBersihkanLayanan();
                muatDataLayanan();
            }
        } catch (NumberFormatException e) {
            tampilkanPesan(Alert.AlertType.ERROR, "Format Salah", "Harga per unit harus berupa angka (double).");
        }
    }

    @FXML
    private void handleUpdateLayanan() {
        if (layananTerpilih == null) {
            tampilkanPesan(Alert.AlertType.WARNING, "Peringatan", "Pilih data layanan pada tabel terlebih dahulu.");
            return;
        }
        try {
            double harga = Double.parseDouble(txtHargaPerUnit.getText().replace(",", ".").replaceAll("[^0-9.]", ""));
            layananTerpilih.setNamaLayanan(txtNamaLayanan.getText().trim());
            layananTerpilih.setJenisSatuan(txtJenisSatuan.getText().trim());
            layananTerpilih.setHargaPerUnit(harga);

            if (layananDAO.update(layananTerpilih)) {
                tampilkanPesan(Alert.AlertType.INFORMATION, "Sukses", "Data layanan berhasil diperbarui.");
                handleBersihkanLayanan();
                muatDataLayanan();
            }
        } catch (NumberFormatException e) {
            tampilkanPesan(Alert.AlertType.ERROR, "Format Salah", "Harga per unit harus berupa angka (double).");
        }
    }

    @FXML
    private void handleHapusLayanan() {
        if (layananTerpilih == null) {
            tampilkanPesan(Alert.AlertType.WARNING, "Peringatan", "Pilih layanan yang akan dihapus.");
            return;
        }
        if (layananDAO.delete(layananTerpilih.getIdLayanan())) {
            tampilkanPesan(Alert.AlertType.INFORMATION, "Sukses", "Layanan berhasil dihapus.");
            handleBersihkanLayanan();
            muatDataLayanan();
        } else {
            tampilkanPesan(Alert.AlertType.ERROR, "Gagal", "Layanan tidak dapat dihapus karena sudah terpakai pada transaksi.");
        }
    }

    @FXML
    private void handleBersihkanLayanan() {
        layananTerpilih = null;
        txtNamaLayanan.clear();
        txtJenisSatuan.clear();
        txtHargaPerUnit.clear();
        tblLayanan.getSelectionModel().clearSelection();
        generatePreviewIdLayanan();
    }

    @FXML
    private void handleCariLayanan() {
        String key = txtCariLayanan.getText().toLowerCase().trim();
        if (key.isEmpty()) {
            tblLayanan.setItems(masterListLayanan);
        } else {
            List<Layanan> filtered = masterListLayanan.stream()
                    .filter(l -> l.getNamaLayanan().toLowerCase().contains(key))
                    .collect(Collectors.toList());
            tblLayanan.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    // ================= BAGIAN DATA KARYAWAN =================
    private void setupTabelKaryawan() {
        colIdKaryawan.setCellValueFactory(c -> new SimpleStringProperty(String.format("K%03d", c.getValue().getIdKaryawan())));
        colNamaKaryawan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaKaryawan()));
        colNoTelpKaryawan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomorTelepon()));
        colJabatanKaryawan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJabatan()));
        colUsernameKaryawan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
    }

    private void muatDataKaryawan() {
        masterListKaryawan.setAll(karyawanDAO.getAll());
        tblKaryawan.setItems(masterListKaryawan);
        generatePreviewIdKaryawan();
    }

    private void generatePreviewIdKaryawan() {
        int nextId = masterListKaryawan.stream().mapToInt(Karyawan::getIdKaryawan).max().orElse(0) + 1;
        txtIdKaryawan.setText(String.format("K%03d", nextId));
    }

    @FXML
    private void handlePilihJabatan(ActionEvent event) {
        if (event.getSource() instanceof MenuItem item) {
            setJabatan(item.getText());
        }
    }

    private void setJabatan(String jabatan) {
        this.jabatanTerpilih = jabatan;
        mbJabatan.setText(jabatan);
    }

    @FXML
    private void handlePilihBarisKaryawan() {
        karyawanTerpilih = tblKaryawan.getSelectionModel().getSelectedItem();
        if (karyawanTerpilih != null) {
            txtIdKaryawan.setText(String.format("K%03d", karyawanTerpilih.getIdKaryawan()));
            txtNamaKaryawan.setText(karyawanTerpilih.getNamaKaryawan());
            txtNoTelpKaryawan.setText(karyawanTerpilih.getNomorTelepon());

            String jabatan = karyawanTerpilih.getJabatan();
            if (jabatan != null && !jabatan.isBlank()) {
                setJabatan(jabatan.toUpperCase());
            } else {
                jabatanTerpilih = null;
                mbJabatan.setText("Pilih Jabatan");
            }

            txtUsername.setText(karyawanTerpilih.getUsername());
            txtPassword.clear();
        }
    }

    @FXML
    private void handleSimpanKaryawan() {
        if (txtNamaKaryawan.getText().isBlank() || jabatanTerpilih == null
                || txtUsername.getText().isBlank() || txtPassword.getText().isBlank()) {
            tampilkanPesan(Alert.AlertType.WARNING, "Validasi", "Nama, Jabatan, Username, dan Password wajib diisi.");
            return;
        }

        Karyawan baru = "ADMIN".equalsIgnoreCase(jabatanTerpilih) ? new Admin() : new Kasir();
        baru.setNamaKaryawan(txtNamaKaryawan.getText().trim());
        baru.setNomorTelepon(txtNoTelpKaryawan.getText().trim());
        baru.setJabatan(jabatanTerpilih);
        baru.setUsername(txtUsername.getText().trim());

        if (karyawanDAO.insert(baru, txtPassword.getText())) {
            tampilkanPesan(Alert.AlertType.INFORMATION, "Sukses", "Data karyawan berhasil disimpan.");
            handleBersihkanKaryawan();
            muatDataKaryawan();
        } else {
            tampilkanPesan(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan data karyawan (username mungkin sudah digunakan).");
        }
    }

    @FXML
    private void handleUpdateKaryawan() {
        if (karyawanTerpilih == null) {
            tampilkanPesan(Alert.AlertType.WARNING, "Peringatan", "Pilih data karyawan pada tabel terlebih dahulu.");
            return;
        }
        if (txtNamaKaryawan.getText().isBlank() || jabatanTerpilih == null || txtUsername.getText().isBlank()) {
            tampilkanPesan(Alert.AlertType.WARNING, "Validasi", "Nama, Jabatan, dan Username wajib diisi.");
            return;
        }

        karyawanTerpilih.setNamaKaryawan(txtNamaKaryawan.getText().trim());
        karyawanTerpilih.setNomorTelepon(txtNoTelpKaryawan.getText().trim());
        karyawanTerpilih.setJabatan(jabatanTerpilih);
        karyawanTerpilih.setUsername(txtUsername.getText().trim());

        boolean berhasil = txtPassword.getText().isBlank()
                ? karyawanDAO.update(karyawanTerpilih)
                : karyawanDAO.update(karyawanTerpilih, txtPassword.getText());

        if (berhasil) {
            tampilkanPesan(Alert.AlertType.INFORMATION, "Sukses", "Data karyawan berhasil diperbarui.");
            handleBersihkanKaryawan();
            muatDataKaryawan();
        } else {
            tampilkanPesan(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui data karyawan.");
        }
    }

    @FXML
    private void handleBersihkanKaryawan() {
        karyawanTerpilih = null;
        jabatanTerpilih = null;
        txtNamaKaryawan.clear();
        txtNoTelpKaryawan.clear();
        mbJabatan.setText("Pilih Jabatan");
        txtUsername.clear();
        txtPassword.clear();
        tblKaryawan.getSelectionModel().clearSelection();
        generatePreviewIdKaryawan();
    }

    @FXML
    private void handleCariKaryawan() {
        String key = txtCariKaryawan.getText().trim();
        masterListKaryawan.setAll(karyawanDAO.cariKaryawan(key));
        tblKaryawan.setItems(masterListKaryawan);
    }

    private void tampilkanPesan(Alert.AlertType type, String judul, String isi) {
        Alert alert = new Alert(type);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(isi);
        alert.showAndWait();
    }
}