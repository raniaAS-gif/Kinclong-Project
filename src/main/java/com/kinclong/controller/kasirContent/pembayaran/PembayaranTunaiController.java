package com.kinclong.controller.kasirContent.pembayaran;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import com.kinclong.model.Pembayaran;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PembayaranTunaiController implements Initializable {

    @FXML
    private TextField txtNominalDiterima;
    @FXML
    private Label lblKembalian;

    private double totalTagihan = 0.0;
    private final NumberFormat angkaFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        angkaFormat.setMaximumFractionDigits(0);

        // Hitung kembalian secara real-time saat nominal diketik
        txtNominalDiterima.textProperty().addListener((obs, oldVal, newVal) -> {
            String bersih = newVal.replaceAll("[^0-9]", "");
            if (!newVal.equals(bersih)) {
                txtNominalDiterima.setText(bersih);
                return;
            }
            hitungKembalianLive();
        });
    }

    public void setTotalTagihan(double totalTagihan) {
        this.totalTagihan = totalTagihan;
        hitungKembalianLive();
    }

    private void hitungKembalianLive() {
        double diterima = getNominalDiterima();
        Pembayaran p = new Pembayaran();
        p.setMetodeBayar("CASH");
        p.setNominalUang(diterima);

        double kembalian = p.hitungKembalian(totalTagihan);
        lblKembalian.setText(kembalian >= 0 ? angkaFormat.format(kembalian) : "0");
    }

    public double getNominalDiterima() {
        String teks = txtNominalDiterima.getText().trim();
        if (teks.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(teks);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}