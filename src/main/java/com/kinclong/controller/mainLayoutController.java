package com.kinclong.controller;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

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
    private Transaksi transaksiAktif;

    public void initUser(Karyawan user) {
        this.currentUser = user;

        if (user instanceof Admin) {
            setSidebar("/com/kinclong/sidebarAdmin.fxml");
        } else if (user instanceof Kasir) {
            setSidebar("/com/kinclong/sidebarKasir.fxml");
        }

        if (user != null) {
            loadPage(user.tampilkanDashboard());
        }
    }

    public Karyawan getCurrentUser() {
        return currentUser;
    }

    public Transaksi getTransaksiAktif() {
        return transaksiAktif;
    }

    public void setTransaksiAktif(Transaksi transaksiAktif) {
        this.transaksiAktif = transaksiAktif;
    }

    private void setSidebar(String sidebarFxmlPath) {
        try {
            URL resource = getClass().getResource(sidebarFxmlPath);
            if (resource == null) {
                throw new IOException("Sidebar FXML tidak ditemukan: " + sidebarFxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
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
        loadPage(fxmlPath, null);
    }

    public void loadPage(String fxmlPath, Consumer<Object> onLoaded) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("Halaman FXML tidak ditemukan: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof SidebarSwitching pageController) {
                pageController.setMainLayoutController(this);
            }
            if (onLoaded != null) {
                onLoaded.accept(controller);
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