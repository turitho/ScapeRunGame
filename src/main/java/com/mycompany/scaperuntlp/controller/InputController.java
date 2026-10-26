
package com.mycompany.scaperuntlp.controller;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class InputController {

    private final Set<KeyCode> teclasPressionadas = new HashSet<>();

    public void conectarTeclado(Scene cena) {

        cena.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            teclasPressionadas.add(evento.getCode());
        });

        cena.addEventHandler(KeyEvent.KEY_RELEASED, evento -> {
            teclasPressionadas.remove(evento.getCode());
        });

    }

    public boolean teclaPressionada(KeyCode tecla) {
        return teclasPressionadas.contains(tecla);
    }

}
