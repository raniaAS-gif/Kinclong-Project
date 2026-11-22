package com.kinclong.controller;

import java.io.IOException;

import com.kinclong.model.Admin;
import com.kinclong.model.Karyawan;
import com.kinclong.model.Kasir;
import com.kinclong.model.Transaksi;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML
    private ScrollPane mainContentArea;

    @FXML
    private BorderPane rootLayout;

    private Karyawan currentUser;

    public void initUser(Karyawan user) {
        this.currentUser = user;

        if (user instanceof Admin) {
            setSidebar("/com/kinclong/sidebarAdmin.fxml");
        } else if (user instanceof Kasir) {
            setSidebar("/com/kinclong/sidebarKasir.fxml");
        }

        loadPage(user.tampilkanDashboard());
    }

    public Karyawan getCurrentUser() {
        return currentUser;
    }

    private Transaksi transaksiAktif;

    public Transaksi getTransaksiAktif() {
        return transaksiAktif;
    }

    public void setTransaksiAktif(Transaksi transaksiAktif) {
        this.transaksiAktif = transaksiAktif;
    }

    private void setSidebar(String sidebarFxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(sidebarFxmlPath));
            Parent sidebarNode = loader.load();

            Object controller = loader.getController();
            if (controller instanceof SidebarSwitching sidebarController) {
                sidebarController.setMainLayoutController(this);
            }

            rootLayout.setLeft(sidebarNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof SidebarSwitching pageController) {
                pageController.setMainLayoutController(this);
            }

            mainContentArea.setContent(page);

            if (rootLayout.getScene() != null) {
                Stage stage = (Stage) rootLayout.getScene().getWindow();
                if (!stage.isMaximized()) {
                    stage.sizeToScene();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
