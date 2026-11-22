package com.kinclong.controller.kasirContent;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import com.kinclong.controller.MainLayoutController;
import com.kinclong.controller.SidebarSwitching;
import com.kinclong.dao.TransaksiDAO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class DashboardKasirController implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    private Label lblPemasukanHariIni;
    @FXML
    private Label lblPemasukanKemarin;
    @FXML
    private Label lblTransaksiKemarin;
    @FXML
    private Label lblPemasukanMingguIni;
    @FXML
    private Label lblTransaksiMingguIni;
    @FXML
    private Label lblPemasukanBulanIni;
    @FXML
    private Label lblTransaksiBulanIni;
    @FXML
    private Label lblCucianAktif;
    @FXML
    private Label lblSiapDiambil;
    @FXML
    private Button btnTambahCucian;
    @FXML
    private Button btnBayar;

    private TransaksiDAO transaksiDAO;
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transaksiDAO = new TransaksiDAO();
        rupiahFormat.setMaximumFractionDigits(0);
        loadDashboardData();
    }

    public void loadDashboardData() {
        try {
            // 1. Ambil data pemasukan
            double hariIni = transaksiDAO.getPemasukanHarian(0);
            double kemarin = transaksiDAO.getPemasukanHarian(1);
            double mingguIni = transaksiDAO.getPemasukanMingguIni();
            double bulanIni = transaksiDAO.getOmzetBulanIni(); // Method bawaan TransaksiDAO

            // 2. Ambil jumlah transaksi
            int trxKemarin = transaksiDAO.getJumlahTransaksiHarian(1);
            int trxMingguIni = transaksiDAO.getJumlahTransaksiMingguIni();
            int trxBulanIni = transaksiDAO.getJumlahTransaksiBulanIni();

            // 3. Ambil jumlah status cucian menggunakan method bawaan TransaksiDAO
            int cucianAktif = transaksiDAO.getCucianAktif("").size();
            int siapDiambil = transaksiDAO.getTagihanSiapBayar("").size();

            // 4. Tampilkan ke UI
            lblPemasukanHariIni.setText(formatRupiah(hariIni));
            lblPemasukanKemarin.setText(formatRupiah(kemarin));
            lblTransaksiKemarin.setText(trxKemarin + " Transaksi");

            lblPemasukanMingguIni.setText(formatRupiah(mingguIni));
            lblTransaksiMingguIni.setText(trxMingguIni + " Transaksi");

            lblPemasukanBulanIni.setText(formatRupiah(bulanIni));
            lblTransaksiBulanIni.setText(trxBulanIni + " Transaksi");

            lblCucianAktif.setText(String.valueOf(cucianAktif));
            lblSiapDiambil.setText(String.valueOf(siapDiambil));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatRupiah(double nominal) {
        return rupiahFormat.format(nominal).replace("Rp", "Rp. ");
    }

    @FXML
    private void handleTambahCucian(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/kasirContent/penerimaanCucian/PenerimaanCucian.fxml");
        }
    }

    @FXML
    private void handleBayar(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/kasirContent/pembayaran/CariPembayaran.fxml");
        }
    }

    private void pindahHalaman(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("File FXML tidak ditemukan: " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);

            // Cari parent BorderPane terdekat dari sidebar utama
            Node current = lblPemasukanHariIni;
            while (current != null && !(current instanceof BorderPane)) {
                current = current.getParent();
            }
            if (current instanceof BorderPane) {
                ((BorderPane) current).setCenter(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}