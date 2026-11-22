package com.kinclong.controller.kasirContent.pembayaran;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.PembayaranDAO;
import com.kinclong.dao.TransaksiDAO;
import com.kinclong.model.Kasir;
import com.kinclong.model.Transaksi;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class CariPembayaranController implements Initializable, SidebarSwitching {

    private static final String FXML_DIR = "/com/kinclong/kasirContent/pembayaran/";
    private MainLayoutController mainLayoutController;

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    private Label lblBelumLunas;
    @FXML
    private Label lblLunasHariIni;
    @FXML
    private Label lblTotalSisaTagihan;
    @FXML
    private TextField txtCari;
    @FXML
    private Button btnCari;
    @FXML
    private TableView<Transaksi> tblPembayaran;
    @FXML
    private TableColumn<Transaksi, String> colNoNota;
    @FXML
    private TableColumn<Transaksi, String> colPelanggan;
    @FXML
    private TableColumn<Transaksi, String> colTotalTagihan;
    @FXML
    private TableColumn<Transaksi, String> colStatusCucian;
    @FXML
    private TableColumn<Transaksi, Void> colAksi;

    private TransaksiDAO transaksiDAO;
    private PembayaranDAO pembayaranDAO;
    private final Kasir kasirValidator = new Kasir();
    private final NumberFormat angkaFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transaksiDAO = new TransaksiDAO();
        pembayaranDAO = new PembayaranDAO();
        angkaFormat.setMaximumFractionDigits(0);

        setupTableColumns();
        loadSummaryCards();
        loadTableData("");
    }

    private void setupTableColumns() {
        colNoNota.setCellValueFactory(cell ->
                new SimpleStringProperty("#" + cell.getValue().getIdTransaksi()));

        colPelanggan.setCellValueFactory(cell -> {
            Transaksi t = cell.getValue();
            return new SimpleStringProperty(t.getNamaPelanggan() + " (" + t.getNomorTeleponPelanggan() + ")");
        });

        colTotalTagihan.setCellValueFactory(cell ->
                new SimpleStringProperty("Rp " + angkaFormat.format(cell.getValue().getTotalBayar())));

        colStatusCucian.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatusCucian()));

        // Render tombol "Bayar" di kolom kosong paling kanan
        colAksi.setCellFactory(param -> new TableCell<>() {
            private final Button btnBayar = new Button("Bayar");

            {
                btnBayar.setStyle(
                        "-fx-background-color: #5B96D3; " +
                        "-fx-text-fill: #FAFCFE; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 6 22 6 22; " +
                        "-fx-cursor: hand;"
                );
                btnBayar.setOnAction(event -> {
                    Transaksi transaksi = getTableView().getItems().get(getIndex());
                    bukaHalamanPelunasan(transaksi);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnBayar);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadSummaryCards() {
        lblBelumLunas.setText(String.valueOf(pembayaranDAO.getJumlahBelumLunas()));
        lblLunasHariIni.setText(String.valueOf(pembayaranDAO.getJumlahLunasHariIni()));
        lblTotalSisaTagihan.setText(angkaFormat.format(pembayaranDAO.getTotalSisaTagihan()));
    }

    private void loadTableData(String keyword) {
        List<Transaksi> daftarTagihan = transaksiDAO.getTagihanSiapBayar(keyword);
        ObservableList<Transaksi> data = FXCollections.observableArrayList(daftarTagihan);
        tblPembayaran.setItems(data);
    }

    @FXML
    private void handleCari(ActionEvent event) {
        loadTableData(txtCari.getText().trim());
    }

    private void bukaHalamanPelunasan(Transaksi transaksi) {
        if (!kasirValidator.validasiProsesPembayaran(transaksi)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Belum Siap Bayar");
            alert.setHeaderText(null);
            alert.setContentText("Pembayaran hanya dapat diproses jika status cucian sudah 'SUDAH'.");
            alert.showAndWait();
            return;
        }

        if (mainLayoutController != null) {
            mainLayoutController.setTransaksiAktif(transaksi);
            mainLayoutController.loadPage(FXML_DIR + "PelunasanPembayaran.fxml", controller -> {
                if (controller instanceof PelunasanPembayaranController pelunasanCtrl) {
                    pelunasanCtrl.setDataTransaksi(transaksi);
                }
            });
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIR + "PelunasanPembayaran.fxml"));
            Parent pelunasanView = loader.load();

            // Kirim data transaksi terpilih ke PelunasanPembayaranController
            PelunasanPembayaranController controller = loader.getController();
            controller.setDataTransaksi(transaksi);

            // Ganti konten tengah pada BorderPane sidebar utama
            Node current = tblPembayaran;
            while (current != null && !(current instanceof BorderPane)) {
                current = current.getParent();
            }
            if (current instanceof BorderPane) {
                ((BorderPane) current).setCenter(pelunasanView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}