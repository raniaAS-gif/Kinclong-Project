package com.kinclong.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.kinclong.dao.KaryawanDAO;
import com.kinclong.model.Karyawan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController implements Initializable{

    @FXML
    private Button buttonAdmin;

    @FXML
    private Button buttonKasir;

    @FXML
    private Button buttonMasuk;

    @FXML
    private Label labelEror;

    @FXML
    private PasswordField textPassword;

    @FXML
    private TextField textUsername;

    private String selectedRole = "ADMIN";
    private final KaryawanDAO karyawanDAO = new KaryawanDAO();

    private final String roleActiveStyle = "-fx-background-color: #5B96D3; -fx-text-fill: #FAFCFE; -fx-background-radius: 25; -fx-cursor: hand;";
    private final String roleInactiveStyle = "-fx-background-color: #FAFCFE; -fx-text-fill: #5B96D3; -fx-border-color: #5B96D3; -fx-background-radius: 25; -fx-border-radius: 25; -fx-cursor: hand;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateRoleButtons();
        sembunyikanEror(null);
    }

    @FXML
    void klikMasuk(ActionEvent event) {
        String username = textUsername.getText().trim();
        String password = textPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            tampilkanError("Username dan password wajib diisi!");
            return;
        }

        try {
            Karyawan karyawan = karyawanDAO.login(username, password);

            if (karyawan == null) {
                tampilkanError("Username atau password salah!");
                return;
            }

            if (!selectedRole.equalsIgnoreCase(karyawan.getJabatan())) {
                tampilkanError("Akun ini bukan akun " + selectedRole + "!");
                return;
            }

            // 1. Kirim objek 'karyawan' (bukan 'username')
            pindahKeMainLayout(event, karyawan);

        } catch (SQLException e) {
            e.printStackTrace();
            tampilkanError("Gagal terhubung ke database.");
        } catch (Exception e) {
            tampilkanError(e.getMessage());
        }
    }

    @FXML
    void klikRoleAdmin(ActionEvent event) {
        selectedRole = "ADMIN";
        updateRoleButtons();
        sembunyikanEror(null);
    }

    @FXML
    void klikRoleKasir(ActionEvent event) {
        selectedRole = "KASIR";
        updateRoleButtons();
        sembunyikanEror(null);
    }

    private void updateRoleButtons() {
        if ("ADMIN".equals(selectedRole)) {
            buttonAdmin.setStyle(roleActiveStyle);
            buttonKasir.setStyle(roleInactiveStyle);
        } else {
            buttonKasir.setStyle(roleActiveStyle);
            buttonAdmin.setStyle(roleInactiveStyle);
        }
    }

    private void tampilkanError(String pesan) {
        labelEror.setText(pesan);
        labelEror.setVisible(true); 
    }

    @FXML
    void sembunyikanEror(KeyEvent event) {
        if (labelEror != null) {
            labelEror.setVisible(false);
        }
    }

    private void pindahKeMainLayout(ActionEvent event, Karyawan karyawan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kinclong/MainLayout.fxml"));
            Parent root = loader.load();

            MainLayoutController mainController = loader.getController();
            mainController.initUser(karyawan);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanError("Gagal memuat halaman utama.");
        }
    }

}
