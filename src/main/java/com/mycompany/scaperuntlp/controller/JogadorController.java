package com.mycompany.scaperuntlp.controller;
import com.mycompany.scaperuntlp.model.*; import javafx.scene.input.KeyCode;
public class JogadorController {
 private final Jogador jogador; private final MapaJogo mapa; private final InputController input;
 public JogadorController(Jogador j,MapaJogo m,InputController i){jogador=j;mapa=m;input=i;}
 public void atualizar(double dt){if(jogador.isEscondido()){if(input.consumirInteracao())jogador.sairDoArmario();return;}
  if(input.pressionada(KeyCode.LEFT)||input.pressionada(KeyCode.A))jogador.girar(-jogador.getVelocidadeRotacao()*dt);
  if(input.pressionada(KeyCode.RIGHT)||input.pressionada(KeyCode.D))jogador.girar(jogador.getVelocidadeRotacao()*dt);
  double direcao=0;if(input.pressionada(KeyCode.W)||input.pressionada(KeyCode.UP))direcao++;if(input.pressionada(KeyCode.S)||input.pressionada(KeyCode.DOWN))direcao--;
  double dx=Math.cos(jogador.getAngulo())*direcao*jogador.getVelocidade()*dt,dy=Math.sin(jogador.getAngulo())*direcao*jogador.getVelocidade()*dt;
  double raio=.20; if(!mapa.bloqueado(jogador.getX()+dx+Math.signum(dx)*raio,jogador.getY()))jogador.mover(dx,0);
  if(!mapa.bloqueado(jogador.getX(),jogador.getY()+dy+Math.signum(dy)*raio))jogador.mover(0,dy);
 }
 public void interagir(){if(jogador.isEscondido()){jogador.sairDoArmario();return;}
  double px=jogador.getX(),py=jogador.getY(),a=jogador.getAngulo();
  for(double d=.5;d<=1.6;d+=.1){ObjetoMapa o=mapa.objetoEm(px+Math.cos(a)*d,py+Math.sin(a)*d);if(o!=null&&o.getTipo()==TipoObjeto.ARMARIO){o.alternarAberto();if(o.isAberto()){jogador.entrarNoArmario(o.getX()+.5-Math.cos(a)*.8,o.getY()+.5-Math.sin(a)*.8);}return;}}
 }
}
