package com.mycompany.scaperuntlp.controller;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.robot.Robot;
import javafx.stage.Window;

/** Centraliza a leitura do teclado e do mouse, sem alterar as regras do jogo. */
public class InputController {

    private final Set<KeyCode> pressionadas = new HashSet<>();
    private final Set<KeyCode> acoesPendentes = new HashSet<>();

    private Scene cena;
    private Robot robo;
    private boolean mouseCapturado;
    private double movimentoMouseX;
    private double movimentoMouseY;

    public void conectar(Scene cena) {
        this.cena = cena;
        this.robo = new Robot();

        cena.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            KeyCode tecla = evento.getCode();
            if (pressionadas.add(tecla)) {
                acoesPendentes.add(tecla);
            }
            if (tecla == KeyCode.ESCAPE) {
                soltarMouse();
            }
        });

        cena.addEventHandler(KeyEvent.KEY_RELEASED, evento ->
                pressionadas.remove(evento.getCode()));

        // Clique esquerdo captura o mouse para olhar continuamente.
        cena.addEventHandler(MouseEvent.MOUSE_PRESSED, evento -> {
            if (evento.getButton() == MouseButton.PRIMARY) {
                capturarMouse();
            }
        });

        // MOUSE_DRAGGED cobre o deslocamento enquanto o clique ainda está pressionado.
        cena.addEventHandler(MouseEvent.MOUSE_MOVED, this::registrarMovimentoMouse);
        cena.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::registrarMovimentoMouse);

        if (cena.getWindow() != null) {
            cena.getWindow().focusedProperty().addListener((obs, antes, focado) -> {
                if (!focado) {
                    limpar();
                    soltarMouse();
                }
            });
        }
    }

    private void registrarMovimentoMouse(MouseEvent evento) {
        if (!mouseCapturado || cena.getWindow() == null) {
            return;
        }

        // O evento usa coordenadas de tela para comparar com a posição do Robot.
        double deltaX = evento.getScreenX() - centroTelaX();
        double deltaY = evento.getScreenY() - centroTelaY();

        // Ignora o evento artificial produzido quando o Robot volta ao centro.
        if (Math.abs(deltaX) < 0.8 && Math.abs(deltaY) < 0.8) {
            return;
        }

        movimentoMouseX += deltaX;
        movimentoMouseY += deltaY;
        centralizarMouse();
    }

    public void capturarMouse() {
        if (mouseCapturado || cena == null || cena.getWindow() == null) {
            return;
        }

        mouseCapturado = true;
        movimentoMouseX = 0;
        movimentoMouseY = 0;
        cena.setCursor(Cursor.NONE);
        centralizarMouse();
    }

    public void soltarMouse() {
        mouseCapturado = false;
        movimentoMouseX = 0;
        movimentoMouseY = 0;
        if (cena != null) {
            cena.setCursor(Cursor.DEFAULT);
        }
    }

    private void centralizarMouse() {
        if (robo != null && cena != null && cena.getWindow() != null) {
            robo.mouseMove(centroTelaX(), centroTelaY());
        }
    }

    private double centroTelaX() {
        Window janela = cena.getWindow();
        return janela.getX() + cena.getX() + cena.getWidth() / 2.0;
    }

    private double centroTelaY() {
        Window janela = cena.getWindow();
        return janela.getY() + cena.getY() + cena.getHeight() / 2.0;
    }

    /** Lê o deslocamento horizontal acumulado desde o quadro anterior. */
    public double consumirMovimentoMouseX() {
        double deltaX = movimentoMouseX;
        movimentoMouseX = 0;
        return deltaX;
    }

    /** Deslocamento vertical: positivo quando o mouse desce na tela. */
    public double consumirMovimentoMouseY() {
        double deltaY = movimentoMouseY;
        movimentoMouseY = 0;
        return deltaY;
    }

    public boolean isMouseCapturado() {
        return mouseCapturado;
    }

    public void limpar() {
        pressionadas.clear();
        acoesPendentes.clear();
        movimentoMouseX = 0;
        movimentoMouseY = 0;
    }

    public boolean pressionada(KeyCode tecla) {
        return pressionadas.contains(tecla);
    }

    public boolean consumirAcao(KeyCode tecla) {
        return acoesPendentes.remove(tecla);
    }

    public boolean consumirInteracao() {
        return consumirAcao(KeyCode.E);
    }
}
