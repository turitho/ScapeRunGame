package com.mycompany.scaperuntlp.controller;
import java.util.*; import javafx.scene.Scene; import javafx.scene.input.*;
public class InputController {
 private final Set<KeyCode> pressionadas=new HashSet<>(); private boolean interagir;
 public void conectar(Scene cena){cena.addEventHandler(KeyEvent.KEY_PRESSED,e->{pressionadas.add(e.getCode());if(e.getCode()==KeyCode.E)interagir=true;});cena.addEventHandler(KeyEvent.KEY_RELEASED,e->pressionadas.remove(e.getCode()));}
 public boolean pressionada(KeyCode tecla){return pressionadas.contains(tecla);} public boolean consumirInteracao(){boolean r=interagir;interagir=false;return r;}
}
