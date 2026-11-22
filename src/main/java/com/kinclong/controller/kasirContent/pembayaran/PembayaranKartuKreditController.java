package com.kinclong.controller.kasirContent.pembayaran;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class PembayaranKartuKreditController implements Initializable {

    @FXML
    private TextField txtNomorKartu;
    @FXML
    private TextField txtKodeEdc;
    @FXML
    private TextField txtNominalKartu;

    private final NumberFormat angkaFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        angkaFormat.setMaximumFractionDigits(0);

        // Batasi input nomor kartu maksimal 4 digit angka
        txtNomorKartu.textProperty().addListener((obs, oldVal, newVal) -> {
            String bersih = newVal.replaceAll("[^0-9]", "");
            if (bersih.length() > 4) {
                bersih = bersih.substring(0, 4);
            }
            if (!newVal.equals(bersih)) {
                txtNomorKartu.setText(bersih);
            }
        });
    }

    public void setTotalTagihan(double totalTagihan) {
        txtNominalKartu.setText("Rp " + angkaFormat.format(totalTagihan));
    }

    public boolean isInputValid() {
        return txtNomorKartu.getText().trim().length() == 4
                && !txtKodeEdc.getText().trim().isEmpty();
    }
}