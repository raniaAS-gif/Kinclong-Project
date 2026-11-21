package com.kinclong.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {

    @FXML
    private ScrollPane mainContentArea;

    public void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof SidebarSwitching pageController) {
                pageController.setMainLayoutController(this);
            }

            mainContentArea.setContent(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private BorderPane rootLayout;

}
