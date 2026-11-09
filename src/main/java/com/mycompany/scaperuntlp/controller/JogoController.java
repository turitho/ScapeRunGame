package com.mycompany.scaperuntlp.controller;
import com.mycompany.scaperuntlp.model.*; import com.mycompany.scaperuntlp.view.JogoView; import javafx.animation.AnimationTimer; import javafx.scene.Scene;
public class JogoController {
 private final InputController input=new InputController(); private final Jogador jogador=new Jogador(); private final MapaJogo mapa=new MapaJogo(); private final JogadorController jogadorController=new JogadorController(jogador,mapa,input); private final JogoView view;
 public JogoController(JogoView view){this.view=view;}
 public void iniciar(Scene cena){input.conectar(cena);new AnimationTimer(){long anterior=0;public void handle(long agora){if(anterior==0){anterior=agora;return;}double dt=Math.min((agora-anterior)/1e9,.05);anterior=agora;if(input.consumirInteracao())jogadorController.interagir();jogadorController.atualizar(dt);view.desenhar(jogador,mapa);}}.start();}
}
