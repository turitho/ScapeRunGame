package com.mycompany.scaperuntlp.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.mycompany.scaperuntlp.App;

public class PrimaryController {

    @FXML
    private StackPane rootPane;

    @FXML
    private Label lblDifficulty;

    private String[] dificuldades = {"FÁCIL", "NORMAL", "DIFÍCIL", "EXTREMO"};
    private int indiceDificuldade = 1;

    @FXML
    private void handlePlay() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void handleDifficulty() {
        indiceDificuldade = (indiceDificuldade + 1) % dificuldades.length;
        lblDifficulty.setText("DIFICULDADE: " + dificuldades[indiceDificuldade]);
    }

    @FXML
    private void handleOptions() {
        mostrarModal("OPÇÕES", "Controles:\nWASD - Mover\nMouse - Olhar\nE - Interagir\nESC - Pausa");
    }

    @FXML
    private void handleCredits() {
        mostrarModal("CRÉDITOS", "Desenvolvido por:\nArthur Gomes de Oliveira\nLuiz Flávio Paz Ayala\nLuiz Felipe de Araujo Santos\nSamuel de Lima de Oliveira\nEstilo: Fuga e Sobrevivência");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void mostrarModal(String titulo, String mensagem) {
        VBox box = new VBox(15);
        box.setMaxSize(380, 230);
        box.setStyle("-fx-background-color: rgba(10, 2, 2, 0.95); -fx-border-color: #8b0000; -fx-border-width: 2px; -fx-padding: 20px; -fx-alignment: center;");

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-text-fill: #ff2222; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Label lblMsg = new Label(mensagem);
        lblMsg.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-family: 'Courier New';");
        lblMsg.setWrapText(true);

        Label btnVoltar = new Label("VOLTAR");
        btnVoltar.getStyleClass().add("granny-text-button");
        btnVoltar.setStyle("-fx-font-size: 20px;");
        btnVoltar.setOnMouseClicked(e -> rootPane.getChildren().remove(box));

        box.getChildren().addAll(lblTitle, lblMsg, btnVoltar);
        rootPane.getChildren().add(box);
    }
}