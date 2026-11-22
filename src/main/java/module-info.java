module com.kinclong {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.kinclong to javafx.fxml;
    opens com.kinclong.model to javafx.base;
    opens com.kinclong.controller.kasirContent to javafx.fxml;

    exports com.kinclong;
    exports com.kinclong.controller;
    exports com.kinclong.model;
    exports com.kinclong.dao;
    exports com.kinclong.controller.kasirContent;
}