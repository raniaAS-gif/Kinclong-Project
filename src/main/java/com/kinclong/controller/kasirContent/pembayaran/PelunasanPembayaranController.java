package com.kinclong.controller.kasirContent.pembayaran;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.PembayaranDAO;
import com.kinclong.dao.TransaksiDAO;
import com.kinclong.model.DetailTransaksi;
import com.kinclong.model.Pembayaran;
import com.kinclong.model.Transaksi;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

public class PelunasanPembayaranController implements Initializable, SidebarSwitching {

    private static final String FXML_DIR = "/com/kinclong/kasirContent/pembayaran/";
    private MainLayoutController mainLayoutController;

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
        if (this.transaksiAktif == null && mainLayoutController.getTransaksiAktif() != null) {
            setDataTransaksi(mainLayoutController.getTransaksiAktif());
        }
    }

    private static final String STYLE_BTN_AKTIF =
            "-fx-background-color: #5b96d3; -fx-background-radius: 10; -fx-border-color: #5b96d3; -fx-border-radius: 10; -fx-text-fill: #fafcfe; -fx-cursor: hand;";
    private static final String STYLE_BTN_NONAKTIF =
            "-fx-background-color: #fafcfe; -fx-background-radius: 10; -fx-border-color: #5b96d3; -fx-border-radius: 10; -fx-text-fill: #5b96d3; -fx-cursor: hand;";

    @FXML
    private Label lblNoNota;
    @FXML
    private Label lblStatusBayar;
    @FXML
    private Label lblNamaPelanggan;
    @FXML
    private Label lblTelpPelanggan;
    @FXML
    private Label lblTanggalMasuk;
    @FXML
    private Label lblStatusCucian;
    @FXML
    private Label lblNamaKasir;
    @FXML
    private TableView<DetailTransaksi> tblDetailLayanan;
    @FXML
    private TableColumn<DetailTransaksi, String> colLayanan;
    @FXML
    private TableColumn<DetailTransaksi, String> colJumlah;
    @FXML
    private TableColumn<DetailTransaksi, String> colHargaUnit;
    @FXML
    private TableColumn<DetailTransaksi, String> colSubtotal;
    @FXML
    private Label lblTotalTagihan;

    @FXML
    private Button btnCash;
    @FXML
    private Button btnQris;
    @FXML
    private Button btnKartuKredit;
    @FXML
    private Label lblTanggalBayar;
    @FXML
    private StackPane paneMetodePembayaran;
    @FXML
    private Button btnProsesBayar;
    @FXML
    private Button btnKembali;

    private Transaksi transaksiAktif;
    private TransaksiDAO transaksiDAO;
    private PembayaranDAO pembayaranDAO;

    // Menyimpan metode aktif: "CASH", "QRIS", atau "TRANSFER" (untuk Kartu Kredit)
    private String metodeTerpilih = "CASH";

    private PembayaranTunaiController tunaiController;
    private PembayaranQRISController qrisController;
    private PembayaranKartuKreditController kartuKreditController;

    private final NumberFormat angkaFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));
    private final DateTimeFormatter tglIndoFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transaksiDAO = new TransaksiDAO();
        pembayaranDAO = new PembayaranDAO();
        angkaFormat.setMaximumFractionDigits(0);

        lblTanggalBayar.setText(LocalDate.now().format(tglIndoFormat));
        setupDetailTable();
    }

    private void setupDetailTable() {
        colLayanan.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNamaLayanan()));

        colJumlah.setCellValueFactory(cell -> {
            DetailTransaksi d = cell.getValue();
            return new SimpleStringProperty(d.getJumlah() + " " + d.getJenisSatuan());
        });

        colHargaUnit.setCellValueFactory(cell ->
                new SimpleStringProperty("Rp " + angkaFormat.format(cell.getValue().getHargaPerUnit())));

        colSubtotal.setCellValueFactory(cell ->
                new SimpleStringProperty("Rp " + angkaFormat.format(cell.getValue().getTotalHarga())));
    }

    public void setDataTransaksi(Transaksi transaksi) {
        this.transaksiAktif = transaksi;

        lblNoNota.setText("#" + transaksi.getIdTransaksi());
        lblStatusBayar.setText(transaksi.getStatusPembayaranText());
        lblNamaPelanggan.setText(transaksi.getNamaPelanggan() + ",");
        lblTelpPelanggan.setText(transaksi.getNomorTeleponPelanggan());
        lblTanggalMasuk.setText(transaksi.getTanggalMasuk().format(tglIndoFormat));
        lblStatusCucian.setText(transaksi.getStatusCucian());
        lblNamaKasir.setText(pembayaranDAO.getNamaKasirByTransaksi(transaksi.getIdTransaksi()));
        lblTotalTagihan.setText(angkaFormat.format(transaksi.getTotalBayar()));

        // Ambil rincian item layanan transaksi
        List<DetailTransaksi> details = transaksiDAO.getDetailByTransaksi(transaksi.getIdTransaksi());
        transaksi.setListDetail(details);
        tblDetailLayanan.setItems(FXCollections.observableArrayList(details));

        // Tampilkan metode pembayaran default: Cash
        tampilkanFormTunai();
    }

    @FXML
    private void handlePilihCash(ActionEvent event) {
        tampilkanFormTunai();
    }

    @FXML
    private void handlePilihQris(ActionEvent event) {
        tampilkanFormQris();
    }

    @FXML
    private void handlePilihKartuKredit(ActionEvent event) {
        tampilkanFormKartuKredit();
    }

    private void updateStyleTombolMetode(Button aktif) {
        btnCash.setStyle(aktif == btnCash ? STYLE_BTN_AKTIF : STYLE_BTN_NONAKTIF);
        btnQris.setStyle(aktif == btnQris ? STYLE_BTN_AKTIF : STYLE_BTN_NONAKTIF);
        btnKartuKredit.setStyle(aktif == btnKartuKredit ? STYLE_BTN_AKTIF : STYLE_BTN_NONAKTIF);
    }

    private void tampilkanFormTunai() {
        metodeTerpilih = "CASH";
        updateStyleTombolMetode(btnCash);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIR + "PembayaranTunai.fxml"));
            Parent view = loader.load();
            tunaiController = loader.getController();
            if (transaksiAktif != null) {
                tunaiController.setTotalTagihan(transaksiAktif.getTotalBayar());
            }
            paneMetodePembayaran.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanFormQris() {
        metodeTerpilih = "QRIS";
        updateStyleTombolMetode(btnQris);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIR + "PembayaranQRIS.fxml"));
            Parent view = loader.load();
            qrisController = loader.getController();
            if (transaksiAktif != null) {
                qrisController.setTotalTagihan(transaksiAktif.getTotalBayar());
            }
            paneMetodePembayaran.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanFormKartuKredit() {
        // Menggunakan "TRANSFER" agar sesuai dengan pengecekan non-tunai di PembayaranDAO
        metodeTerpilih = "TRANSFER";
        updateStyleTombolMetode(btnKartuKredit);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIR + "PembayaranKartuKredit.fxml"));
            Parent view = loader.load();
            kartuKreditController = loader.getController();
            if (transaksiAktif != null) {
                kartuKreditController.setTotalTagihan(transaksiAktif.getTotalBayar());
            }
            paneMetodePembayaran.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProsesPembayaran(ActionEvent event) {
        if (transaksiAktif == null) return;

        double totalTagihan = transaksiAktif.getTotalBayar();
        double nominalUang = totalTagihan;

        if ("CASH".equals(metodeTerpilih)) {
            nominalUang = tunaiController.getNominalDiterima();
            if (nominalUang < totalTagihan) {
                tampilkanAlert(Alert.AlertType.WARNING, "Nominal Kurang", "Uang tunai yang diterima kurang dari total tagihan.");
                return;
            }
        } else if ("QRIS".equals(metodeTerpilih)) {
            if (!qrisController.isSudahDiterima()) {
                tampilkanAlert(Alert.AlertType.WARNING, "Belum Dikonfirmasi", "Klik tombol 'Pembayaran Sudah Diterima' terlebih dahulu.");
                return;
            }
        } else if ("TRANSFER".equals(metodeTerpilih)) {
            if (!kartuKreditController.isInputValid()) {
                tampilkanAlert(Alert.AlertType.WARNING, "Data Kartu Belum Lengkap", "Isi 4 digit terakhir kartu dan kode persetujuan EDC.");
                return;
            }
        }

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setMetodeBayar(metodeTerpilih);
        pembayaran.setTanggalBayar(LocalDate.now());
        pembayaran.setNominalUang(nominalUang);

        boolean sukses = pembayaranDAO.prosesPelunasan(transaksiAktif, pembayaran);
        if (sukses) {
            double kembalian = pembayaran.hitungKembalian(totalTagihan);
            String infoKembalian = "CASH".equals(metodeTerpilih)
                    ? "\nKembalian: Rp " + angkaFormat.format(kembalian)
                    : "";

            tampilkanAlert(Alert.AlertType.INFORMATION, "Pelunasan Berhasil",
                    "Nota #" + transaksiAktif.getIdTransaksi() + " berhasil dilunasi." + infoKembalian);
            kembaliKeCariPembayaran();
        } else {
            tampilkanAlert(Alert.AlertType.ERROR, "Gagal Memproses", "Terjadi kesalahan saat menyimpan data pembayaran.");
        }
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        kembaliKeCariPembayaran();
    }

    private void kembaliKeCariPembayaran() {
        if (mainLayoutController != null) {
            mainLayoutController.setTransaksiAktif(null);
            mainLayoutController.loadPage(FXML_DIR + "CariPembayaran.fxml");
        }
    }

    private void tampilkanAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}