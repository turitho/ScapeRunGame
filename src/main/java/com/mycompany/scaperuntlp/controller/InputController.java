package com.mycompany.scaperuntlp.controller;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Centraliza a leitura do teclado e transforma E em uma ação por pressionamento. */
public class InputController {
    private final Set<KeyCode> pressionadas = new HashSet<>();
    private boolean interagir;

    public void conectar(Scene cena) {
        cena.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            // Evita repetir a interação enquanto E permanece pressionado.
            boolean primeiraPressao = pressionadas.add(evento.getCode());
            if (evento.getCode() == KeyCode.E && primeiraPressao) {
                interagir = true;
            }
        });

        cena.addEventHandler(KeyEvent.KEY_RELEASED, evento ->
                pressionadas.remove(evento.getCode()));
    }

    public boolean pressionada(KeyCode tecla) {
        return pressionadas.contains(tecla);
    }

    public boolean consumirInteracao() {
        boolean solicitada = interagir;
        interagir = false;
        return solicitada;
    }
}
