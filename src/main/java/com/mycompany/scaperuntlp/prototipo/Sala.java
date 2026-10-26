package com.mycompany.scaperuntlp.prototipo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class Sala extends Application {

    private final Set<KeyCode> teclas = new HashSet<>();

    private Eixos jogador;
    private Renderizar renderizador;

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(800, 600);

        jogador = new Eixos();
        renderizador = new Renderizar(canvas);

        StackPane root = new StackPane(canvas);
        Scene cena = new Scene(root, 800, 600);

        cena.setOnKeyPressed(event ->
                teclas.add(event.getCode())
        );

        cena.setOnKeyReleased(event ->
                teclas.remove(event.getCode())
        );

        stage.setTitle("Jogo de Terror - Protótipo");
        stage.setScene(cena);
        stage.setResizable(false);
        stage.show();

        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        new AnimationTimer() {

            private long tempoAnterior = 0;

            @Override
            public void handle(long agora) {

                if (tempoAnterior == 0) {
                    tempoAnterior = agora;
                    renderizador.desenhar(jogador);
                    return;
                }

                double dt = (agora - tempoAnterior) / 1_000_000_000.0;
                tempoAnterior = agora;

                dt = Math.min(dt, 0.05);

                atualizar(dt);
                renderizador.desenhar(jogador);
            }

        }.start();
    }

    private void atualizar(double dt) {

        double direcao = 0;

        if (teclas.contains(KeyCode.W)) direcao += 1;
        if (teclas.contains(KeyCode.S)) direcao -= 1;

        if (teclas.contains(KeyCode.LEFT)) {
            jogador.angulo -= jogador.velocidadeRotacao * dt;
        }

        if (teclas.contains(KeyCode.RIGHT)) {
            jogador.angulo += jogador.velocidadeRotacao * dt;
        }

        double dx = Math.cos(jogador.angulo)
                * direcao * jogador.velocidade * dt;

        double dy = Math.sin(jogador.angulo)
                * direcao * jogador.velocidade * dt;

        double margem = 0.20;

        if (!renderizador.parede(
                jogador.x + dx + Math.signum(dx) * margem,
                jogador.y)) {
            jogador.x += dx;
        }

        if (!renderizador.parede(
                jogador.x,
                jogador.y + dy + Math.signum(dy) * margem)) {
            jogador.y += dy;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}