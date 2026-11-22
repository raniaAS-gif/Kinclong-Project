package com.kinclong.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SidebarKasirController implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @FXML
    private ImageView iconDashboardKasir;

    @FXML
    private ImageView iconPembayaran;

    @FXML
    private ImageView iconPenerimaanCucian;

    @FXML
    private ImageView iconUpdateStatus;

    @FXML
    private Label labelDashboardKasir;

    @FXML
    private Label labelPembayaran;

    @FXML
    private Label labelPenerimaanCucian;

    @FXML
    private Label labelUpdateStatus;

    @FXML
    private VBox sidebarDashboardKasir;

    @FXML
    private ImageView sidebarLogout;

    @FXML
    private VBox sidebarPembayaran;

    @FXML
    private VBox sidebarPenerimaanCucian;

    @FXML
    private VBox sidebarUpdateStatus;

    private final String styleActive = "-fx-background-color: #5B96D3; -fx-background-radius: 10; -fx-cursor: hand;";
    private final String styleDefault = "-fx-background-color: transparent; -fx-background-radius: 10; -fx-cursor: hand;";

    private final String styleHover = "-fx-background-color: #EBF3FA; -fx-background-radius: 10; -fx-cursor: hand;";
    private VBox currentActiveMenu;

    private final Color colorActivate = Color.WHITE;
    private final Color colorDefault = Color.web("#5B96D3");

    private final Image imgDashDefault = new Image(getClass().getResourceAsStream("CKinclong-Project\\src\\main\\resources\\assets\\icons8-dashboard-32.png"));
    private final Image imgDashActivate = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-dashboard-32 (1).png"));

    private final Image imgCucianDefault = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-washing-machine-48.png"));
    private final Image imgCucianActivate = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-washing-machine-48 (1).png"));

    private final Image imgStatusDefault = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-future-48.png"));
    private final Image imgStatusActivate = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-future-48 (1).png"));

    private final Image imgBayarDefault = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-wallet-48 (1).png"));
    private final Image imgBayarActivate = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-wallet-48.png"));

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupHoverEffect(sidebarDashboardKasir);
        setupHoverEffect(sidebarPenerimaanCucian);
        setupHoverEffect(sidebarUpdateStatus);
        setupHoverEffect(sidebarPembayaran);

        setActiveMenu(sidebarDashboardKasir, labelDashboardKasir, iconDashboardKasir, imgDashActivate);
    }

    private void setupHoverEffect(VBox menuBox) {
        menuBox.setOnMouseEntered(event -> {
            if (menuBox != currentActiveMenu) {
                menuBox.setStyle(styleHover);
            }
        });

        menuBox.setOnMouseExited(event -> {
            if (menuBox != currentActiveMenu) {
                menuBox.setStyle(styleDefault);
            }
        });
    }

    private void setActiveMenu(VBox activeBox, Label activeLabel, ImageView activeIcon, Image activeImage) {
        resetAllMenus();
    
        currentActiveMenu = activeBox;

        activeBox.setStyle(styleActive);
        activeLabel.setTextFill(colorActivate);
        activeIcon.setImage(activeImage);
    }

    @FXML
    void klikDashboardKasir(MouseEvent event) {
        setActiveMenu(sidebarDashboardKasir, labelDashboardKasir, iconDashboardKasir, imgDashActivate);
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("Kinclong-Project\\src\\main\\resources\\com\\kinclong\\dashboardKasir.fxml");
        }
    }

    @FXML
    void klikPenerimaanCucian(MouseEvent event) {
        setActiveMenu(sidebarPenerimaanCucian, labelPenerimaanCucian, iconPenerimaanCucian, imgCucianActivate);
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("Kinclong-Project\\src\\main\\resources\\com\\kinclong\\dashboardKasir.fxml");
        }
    }

    @FXML
    void klikUpdateStatus(MouseEvent event) {
        setActiveMenu(sidebarUpdateStatus, labelUpdateStatus, iconUpdateStatus, imgStatusActivate);
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("Kinclong-Project\\src\\main\\resources\\com\\kinclong\\dashboardKasir.fxml");
        }
    }

    @FXML
    void klikPembayaran(MouseEvent event) {
        setActiveMenu(sidebarPembayaran, labelPembayaran, iconPembayaran, imgBayarActivate);
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("Kinclong-Project\\src\\main\\resources\\com\\kinclong\\dashboardKasir.fxml");
        }
    }

    @FXML 
    void klikLogout(MouseEvent event) {
        kembaliKeLogin(event);
    }

    private void kembaliKeLogin(MouseEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/kinclong/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetAllMenus() {
        sidebarDashboardKasir.setStyle(styleDefault);
        labelDashboardKasir.setTextFill(colorDefault);
        iconDashboardKasir.setImage(imgDashDefault);

        sidebarPenerimaanCucian.setStyle(styleDefault);
        labelPenerimaanCucian.setTextFill(colorDefault);
        iconPenerimaanCucian.setImage(imgCucianDefault);

        sidebarUpdateStatus.setStyle(styleDefault);
        labelUpdateStatus.setTextFill(colorDefault);
        iconUpdateStatus.setImage(imgStatusDefault);

        sidebarPembayaran.setStyle(styleDefault);
        labelPembayaran.setTextFill(colorDefault);
        iconPembayaran.setImage(imgBayarDefault);
    }
}
