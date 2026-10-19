package com.mycompany.scaperuntlp.controller;

import com.mycompany.scaperuntlp.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
