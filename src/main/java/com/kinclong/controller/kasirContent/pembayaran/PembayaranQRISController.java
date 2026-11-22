package com.kinclong.controller.kasirContent.pembayaran;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class PembayaranQRISController implements Initializable {

    @FXML
    private TextField txtNominalQris;
    @FXML
    private Button btnKonfirmasiQris;

    private boolean sudahDiterima = false;
    private final NumberFormat angkaFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        angkaFormat.setMaximumFractionDigits(0);
    }

    public void setTotalTagihan(double totalTagihan) {
        txtNominalQris.setText("Rp " + angkaFormat.format(totalTagihan));
    }

    @FXML
    private void handleKonfirmasiQris(ActionEvent event) {
        sudahDiterima = true;
        btnKonfirmasiQris.setText("✓ Pembayaran QRIS Diterima");
        btnKonfirmasiQris.setStyle("-fx-background-color: #27AE60; -fx-background-radius: 10; -fx-text-fill: #fafcfe;");
    }

    public boolean isSudahDiterima() {
        return sudahDiterima;
    }
}