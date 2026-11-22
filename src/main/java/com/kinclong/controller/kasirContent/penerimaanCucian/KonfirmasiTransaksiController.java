package com.kinclong.controller.kasirContent.penerimaanCucian;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.TransaksiDAO;
import com.kinclong.model.DetailTransaksi;
import com.kinclong.model.Transaksi;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class KonfirmasiTransaksiController implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @FXML
    private Button btnBatal;

    @FXML
    private Button btnKembali;

    @FXML
    private Button btnSimpanTransaksi;

    @FXML 
    private TableView<DetailTransaksi> tabelKonfirmasi;

    @FXML 
    private TableColumn<DetailTransaksi, Integer> colNo;

    @FXML 
    private TableColumn<DetailTransaksi, String> colLayanan;

    @FXML
    private TableColumn<DetailTransaksi, Double> colJumlah;

    @FXML
    private TableColumn<DetailTransaksi, Double> colSubtotal;

    @FXML
    private Label lblKasir;

    @FXML
    private Label lblNomorNota;

    @FXML
    private Label lblPelanggan;

    @FXML
    private Label lblStatusCucian;

    @FXML
    private Label lblTanggalMasuk;

    @FXML
    private Label lblTelepon;

    @FXML
    private Label lblTotalTagihan;

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private Transaksi transaksiAktif;

    private final NumberFormat rupiahFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));
    private final DateTimeFormatter formatTanggal =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
        this.transaksiAktif = mainLayoutController.getTransaksiAktif();
        tampilkanRingkasan();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNo.setCellValueFactory(new PropertyValueFactory<>("nomorUrut"));
        colLayanan.setCellValueFactory(cell -> {
            DetailTransaksi d = cell.getValue();
            String teks = d.getNamaLayanan();
            if (d.getKeterangan() != null && !d.getKeterangan().isBlank()) {
                teks += " (" + d.getKeterangan() + ")";
            }
            return new SimpleStringProperty(teks);
        });
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
    }

    private void tampilkanRingkasan() {
        if (transaksiAktif == null) return;

        int previewNota = transaksiDAO.getNextIdTransaksi();
        lblNomorNota.setText("Nota #" + previewNota);

        if (transaksiAktif.getPelanggan() != null) {
            lblPelanggan.setText(transaksiAktif.getPelanggan().getNama() + ", "
                    + transaksiAktif.getPelanggan().getNomorTelepon());
        }

        lblTanggalMasuk.setText(transaksiAktif.getTanggalMasuk().format(formatTanggal));
        lblStatusCucian.setText("Belum");

        if (transaksiAktif.getKasir() != null) {
            lblKasir.setText(transaksiAktif.getKasir().getNamaKaryawan());
        }

        tabelKonfirmasi.setItems(FXCollections.observableArrayList(transaksiAktif.getListDetail()));
        lblTotalTagihan.setText(rupiahFormat.format(transaksiAktif.getTotalBayar()));
    }

    @FXML
    void klikKembali(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/PenerimaanCucianLayanan.fxml");
        }
    }

    @FXML
    void klikBatal(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Batalkan Transaksi");
        confirm.setHeaderText(null);
        confirm.setContentText("Yakin ingin membatalkan transaksi ini?");

        Optional<ButtonType> hasil = confirm.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK && mainLayoutController != null) {
            mainLayoutController.setTransaksiAktif(null);
            mainLayoutController.loadPage("/com/kinclong/PenerimaanCucian.fxml");
        }
    }

    @FXML
    void klikSimpanTransaksi(ActionEvent event) {
        if (transaksiAktif == null) return;

        if (transaksiDAO.simpanTransaksiBaru(transaksiAktif)) {
            // Tampilkan ringkasan nota yang berhasil dicetak/disimpan
            StringBuilder struk = new StringBuilder();
            struk.append("No. Nota   : #").append(transaksiAktif.getIdTransaksi()).append("\n");
            struk.append("Pelanggan  : ").append(transaksiAktif.getNamaPelanggan()).append("\n");
            struk.append("Tanggal    : ").append(transaksiAktif.getTanggalMasuk().format(formatTanggal)).append("\n");
            struk.append("Status     : BELUM\n");
            struk.append("Total      : Rp ").append(rupiahFormat.format(transaksiAktif.getTotalBayar())).append("\n\n");
            struk.append("Rincian Layanan:\n");
            for (DetailTransaksi d : transaksiAktif.getListDetail()) {
                struk.append("- ").append(d.getNamaLayanan())
                     .append(" (").append(d.getJumlah()).append(" ").append(d.getJenisSatuan()).append(") : Rp ")
                     .append(rupiahFormat.format(d.getTotalHarga())).append("\n");
            }

            Alert notaAlert = new Alert(Alert.AlertType.INFORMATION);
            notaAlert.setTitle("Transaksi Berhasil Disimpan");
            notaAlert.setHeaderText("Nota Transaksi #" + transaksiAktif.getIdTransaksi());
            notaAlert.setContentText(struk.toString());
            notaAlert.showAndWait();

            if (mainLayoutController != null) {
                mainLayoutController.setTransaksiAktif(null);
                mainLayoutController.loadPage("/com/kinclong/PenerimaanCucian.fxml");
            }
        } else {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Gagal Menyimpan");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Terjadi kesalahan saat menyimpan transaksi ke database.");
            errorAlert.showAndWait();
        }
    }

}
