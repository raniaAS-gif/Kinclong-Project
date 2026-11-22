package com.kinclong.controller.kasirContent.penerimaanCucian;

import com.kinclong.dao.PelangganDAO;
import com.kinclong.model.Pelanggan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TambahPelangganController {

    @FXML
    private Button btnBatalkan;

    @FXML
    private Button btnLanjut;

    @FXML
    private TextField textAlamat;

    @FXML
    private TextField textNamaPelanggan;

    @FXML
    private TextField textTelepon;

    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private Pelanggan pelangganBaru;

    public Pelanggan getPelangganBaru() {
        return pelangganBaru;
    }

    @FXML
    void klikBatalkan(ActionEvent event) {
        tutupPopup();
    }

    @FXML
    void klikLanjut(ActionEvent event) {
        String nama = textNamaPelanggan.getText().trim();
        String telp = textTelepon.getText().trim();
        String alamat = textAlamat.getText().trim();

        if (nama.isEmpty() || telp.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Data Belum Lengkap");
            alert.setHeaderText(null);
            alert.setContentText("Nama Pelanggan dan Nomor Telepon wajib diisi!");
            alert.showAndWait();
            return;
        }

        Pelanggan p = new Pelanggan(0, nama, telp, alamat);
        if (pelangganDAO.insert(p)) {
            this.pelangganBaru = p;
            tutupPopup();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Gagal Menyimpan");
            alert.setHeaderText(null);
            alert.setContentText("Gagal menambahkan data pelanggan ke database.");
            alert.showAndWait();
        }
    }

    private void tutupPopup() {
        Stage stage = (Stage) textNamaPelanggan.getScene().getWindow();
        stage.close();
    }

}
