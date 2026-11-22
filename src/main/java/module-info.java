module com.kinclong {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.kinclong to javafx.fxml;
    opens com.kinclong.controller to javafx.fxml;
    opens com.kinclong.model to javafx.base;

    exports com.kinclong;
}