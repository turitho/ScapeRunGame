package com.mycompany.scaperuntlp.controller;

import com.mycompany.scaperuntlp.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}