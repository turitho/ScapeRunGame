module com.mycompany.scaperuntlp {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.scaperuntlp to javafx.fxml;
    exports com.mycompany.scaperuntlp;
}
