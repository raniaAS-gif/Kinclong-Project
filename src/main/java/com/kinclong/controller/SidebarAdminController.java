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

public class SidebarAdminController implements Initializable, SidebarSwitching {

    private MainLayoutController mainLayoutController;

    @FXML
    private ImageView iconDashboardAdmin;

    @FXML
    private ImageView iconKelolaData;

    @FXML
    private ImageView iconPendapatan;

    @FXML
    private Label labelDashboardAdmin;

    @FXML
    private Label labelKelolaData;

    @FXML
    private Label labelPendapatan;
    
    @FXML
    private VBox sidebarDashboardAdmin;

    @FXML
    private VBox sidebarKelolaData;

    @FXML
    private ImageView sidebarLogoutAdmin;

    @FXML
    private VBox sidebarPendapatan;

    private final String styleActive = "-fx-background-color: #5B96D3; -fx-background-radius: 10; -fx-cursor: hand;";
    private final String styleDefault = "-fx-background-color: transparent; -fx-background-radius: 10; -fx-cursor: hand;";

    private final String styleHover = "-fx-background-color: #EBF3FA; -fx-background-radius: 10; -fx-cursor: hand;";
    private VBox currentActiveMenu;

    private final Color colorActivate = Color.WHITE;
    private final Color colorDefault = Color.web("#5B96D3");

    private final Image imgDashDefault = new Image(getClass().getResourceAsStream("CKinclong-Project\\src\\main\\resources\\assets\\icons8-dashboard-32.png"));
    private final Image imgDashActivate = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-dashboard-32 (1).png"));

    private final Image imgKelolaDefault = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-database-96.png"));
    private final Image imgKelolaActivate = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-database-96 (1).png"));

    private final Image imgPendapatanDefault = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-document-96.png"));
    private final Image imgPendapatanActivate = new Image(getClass().getResourceAsStream("Kinclong-Project\\src\\main\\resources\\assets\\icons8-document-96 (1).png"));

    @Override
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupHoverEffect(sidebarDashboardAdmin);
        setupHoverEffect(sidebarKelolaData);
        setupHoverEffect(sidebarPendapatan);

        setActiveMenu(sidebarDashboardAdmin, labelDashboardAdmin, iconDashboardAdmin, imgDashActivate);
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
    void klikDashboardAdmin(MouseEvent event) {
        setActiveMenu(sidebarDashboardAdmin, labelDashboardAdmin, iconDashboardAdmin, imgDashActivate);
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/DashboardAdmin.fxml");
        }
    }

    @FXML
    void klikKelolaData(MouseEvent event) {
        setActiveMenu(sidebarKelolaData, labelKelolaData, iconKelolaData, imgDashActivate);
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/DashboardAdmin.fxml");
        }
    }

    @FXML
    void klikPendapatan(MouseEvent event) {
        setActiveMenu(sidebarPendapatan, labelPendapatan, iconPendapatan, imgDashActivate);
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/com/kinclong/DashboardAdmin.fxml");
        }
    }

     @FXML
    void klikLogoutAdmin(MouseEvent event) {
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
        sidebarDashboardAdmin.setStyle(styleDefault);
        labelDashboardAdmin.setTextFill(colorDefault);
        iconDashboardAdmin.setImage(imgDashDefault);

        sidebarKelolaData.setStyle(styleDefault);
        labelKelolaData.setTextFill(colorDefault);
        iconKelolaData.setImage(imgDashDefault);

        sidebarPendapatan.setStyle(styleDefault);
        labelPendapatan.setTextFill(colorDefault);
        iconPendapatan.setImage(imgDashDefault);
    }
}