module com.kinclong {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.kinclong to javafx.fxml;
    exports com.kinclong;
}