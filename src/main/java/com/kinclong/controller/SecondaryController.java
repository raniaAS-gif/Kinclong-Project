package com.kinclong.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import com.kinclong.App;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}