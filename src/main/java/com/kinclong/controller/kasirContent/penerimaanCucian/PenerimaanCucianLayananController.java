package com.kinclong.controller.kasirContent.penerimaanCucian;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.LayananDAO;
import com.kinclong.model.DetailTransaksi;
import com.kinclong.model.Layanan;
import com.kinclong.model.Transaksi;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class PenerimaanCucianLayananController implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @FXML private Button btnKembali;
    @FXML private Button btnLanjut;
    @FXML private Button btnTambah;

    @FXML private TableView<DetailTransaksi> tableKeranjang;
    @FXML private TableColumn<DetailTransaksi, Integer> coloumNo;
    @FXML private TableColumn<DetailTransaksi, String> coloumLayanan;
    @FXML private TableColumn<DetailTransaksi, Double> coloumJumlah;
    @FXML private TableColumn<DetailTransaksi, Double> coloumSubtotal;
    @FXML private TableColumn<DetailTransaksi, DetailTransaksi> coloumHapus;

    @FXML private TableView<Layanan> tableLayanan;
    @FXML private TableColumn<Layanan, String> coloumNamaLayanan;
    @FXML private TableColumn<Layanan, String> coloumSatuan;
    @FXML private TableColumn<Layanan, Double> coloumHarga;
    @FXML private TableColumn<Layanan, Layanan> coloumPilih;

    @FXML private HBox labelTambah;
    @FXML private Label labelTotalHarga;
    @FXML private TextField textKeterangan;
    @FXML private TextField textKuantitas;

    private final LayananDAO layananDAO = new LayananDAO();
    private final ObservableList<Layanan> daftarLayanan = FXCollections.observableArrayList();
    private final ObservableList<DetailTransaksi> daftarKeranjang = FXCollections.observableArrayList();

    private Layanan layananTerpilih = null;
    private Transaksi transaksiAktif;

    private final NumberFormat angkaFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
        this.transaksiAktif = mainLayoutController.getTransaksiAktif();
        if (this.transaksiAktif == null) {
            this.transaksiAktif = new Transaksi();
            mainLayoutController.setTransaksiAktif(this.transaksiAktif);
        }

        daftarKeranjang.setAll(transaksiAktif.getListDetail());
        perbaruiTotalHarga();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTabelLayanan();
        setupTabelKeranjang();
        daftarLayanan.setAll(layananDAO.getAll());
        tableLayanan.setItems(daftarLayanan);
        tableKeranjang.setItems(daftarKeranjang);
    }

    private void setupTabelLayanan() {
        coloumNamaLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        coloumSatuan.setCellValueFactory(new PropertyValueFactory<>("jenisSatuan"));

        // Tetap bertipe Double (mengambil getHargaPerUnit() dari Layanan)
        coloumHarga.setCellValueFactory(new PropertyValueFactory<>("hargaPerUnit"));
        coloumHarga.setCellFactory(col -> createFormattedDoubleCell(true));

        coloumPilih.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue()));
        coloumPilih.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setStyle("-fx-cursor: hand;");
                checkBox.setOnAction(e -> {
                    Layanan item = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        layananTerpilih = item;
                    } else if (layananTerpilih == item) {
                        layananTerpilih = null;
                    }
                    tableLayanan.refresh();
                });
            }

            @Override
            protected void updateItem(Layanan item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(layananTerpilih != null
                            && layananTerpilih.getIdLayanan() == item.getIdLayanan());
                    setAlignment(Pos.CENTER);
                    setGraphic(checkBox);
                }
            }
        });
    }

    private void setupTabelKeranjang() {
        coloumNo.setCellValueFactory(new PropertyValueFactory<>("nomorUrut"));
        coloumLayanan.setCellValueFactory(cell -> {
            DetailTransaksi d = cell.getValue();
            String teks = d.getNamaLayanan();
            if (d.getKeterangan() != null && !d.getKeterangan().isBlank()) {
                teks += " (" + d.getKeterangan() + ")";
            }
            return new SimpleStringProperty(teks);
        });

        // Tetap bertipe Double (mengambil getJumlah() dan getTotalHarga() dari DetailTransaksi)
        coloumJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        coloumJumlah.setCellFactory(col -> createFormattedDoubleCell(false));

        coloumSubtotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        coloumSubtotal.setCellFactory(col -> createFormattedDoubleCell(true));

        coloumHapus.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue()));
        coloumHapus.setCellFactory(col -> new TableCell<>() {
            private final Button btnHapus = buatTombolHapus();

            {
                btnHapus.setOnAction(e -> {
                    DetailTransaksi item = getTableView().getItems().get(getIndex());
                    if (item != null && transaksiAktif != null) {
                        transaksiAktif.hapusDetailItem(item);
                        daftarKeranjang.setAll(transaksiAktif.getListDetail());
                        perbaruiTotalHarga();
                    }
                });
            }

            @Override
            protected void updateItem(DetailTransaksi item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    setGraphic(btnHapus);
                }
            }
        });
    }

    // CellFactory untuk kolom Double agar tampil rapi tanpa ".0"
    private <T> TableCell<T, Double> createFormattedDoubleCell(boolean gunakanPemisahRibuan) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else if (gunakanPemisahRibuan) {
                    setText(angkaFormat.format(value));
                } else {
                    setText(value == value.longValue()
                            ? String.valueOf(value.longValue())
                            : String.valueOf(value));
                }
            }
        };
    }

    private Button buatTombolHapus() {
        SVGPath ikonSampah = new SVGPath();
        ikonSampah.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        ikonSampah.setFill(Color.web("#EF4444"));
        ikonSampah.setScaleX(0.85);
        ikonSampah.setScaleY(0.85);

        Button btn = new Button();
        btn.setGraphic(ikonSampah);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        return btn;
    }

    @FXML
    void tambahKeranjang(ActionEvent event) {
        if (layananTerpilih == null) {
            tampilkanPeringatan("Pilih Layanan", "Silakan centang salah satu layanan terlebih dahulu.");
            return;
        }

        String qtyText = textKuantitas.getText().trim().replace(",", ".");
        double kuantitas;
        try {
            kuantitas = Double.parseDouble(qtyText);
            if (kuantitas <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tampilkanPeringatan("Kuantitas Tidak Valid", "Masukkan angka kuantitas yang benar (contoh: 2 atau 2.5).");
            return;
        }

        String keterangan = textKeterangan.getText() == null ? "" : textKeterangan.getText().trim();

        DetailTransaksi detailBaru = new DetailTransaksi(0, layananTerpilih, kuantitas, keterangan);
        transaksiAktif.tambahDetailItem(detailBaru);

        daftarKeranjang.setAll(transaksiAktif.getListDetail());
        perbaruiTotalHarga();

        layananTerpilih = null;
        tableLayanan.refresh();
        textKuantitas.clear();
        textKeterangan.clear();
    }

    private void perbaruiTotalHarga() {
        double total = transaksiAktif != null ? transaksiAktif.getTotalBayar() : 0.0;
        // Hanya mengubah angka pada label nominal (label "Total Harga: Rp" terpisah di FXML)
        labelTotalHarga.setText(angkaFormat.format(total));
    }

    @FXML
    void klikKembali(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/kasirContent/penerimaanCucian/PenerimaanCucian.fxml");
        }
    }

    @FXML
    void klikLanjut(ActionEvent event) {
        if (transaksiAktif == null || transaksiAktif.getListDetail().isEmpty()) {
            tampilkanPeringatan("Keranjang Kosong", "Tambahkan minimal satu layanan ke keranjang sebelum melanjutkan.");
            return;
        }

        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong//com/kinclong/kasirContent/penerimaanCucian/KonfirmasiTransaksi.fxml");
        }
    }

    private void tampilkanPeringatan(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }
}