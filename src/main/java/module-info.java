module com.mycompany.scaperuntlp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.mycompany.scaperuntlp to javafx.fxml;
    exports com.mycompany.scaperuntlp;

    opens com.mycompany.scaperuntlp.controller to javafx.fxml;
    exports com.mycompany.scaperuntlp.controller;
}