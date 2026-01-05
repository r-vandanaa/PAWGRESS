module digitalpet {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    exports com.digitalpet;
    exports com.digitalpet.model;
    exports com.digitalpet.view;
    exports com.digitalpet.viewmodel;
    
    opens com.digitalpet.view to javafx.fxml;
    opens com.digitalpet.viewmodel to javafx.fxml;
}