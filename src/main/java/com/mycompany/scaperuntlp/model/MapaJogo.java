package com.mycompany.scaperuntlp.model;
import java.util.*;
public class MapaJogo {
 public static final int LARGURA=40, ALTURA=40;
 private final int[][] paredes=new int[ALTURA][LARGURA];
 private final List<ObjetoMapa> objetos=new ArrayList<>();
 public MapaJogo(){
  for(int y=0;y<ALTURA;y++) for(int x=0;x<LARGURA;x++) paredes[y][x]=(x==0||y==0||x==LARGURA-1||y==ALTURA-1)?1:0;
  // Divisórias formando salas e corredores, com passagens abertas.
  for(int x=5;x<35;x++) if(x!=12&&x!=25) paredes[10][x]=1;
  for(int y=4;y<36;y++) if(y!=17&&y!=29) paredes[y][20]=1;
  for(int x=2;x<18;x++) if(x!=8) paredes[25][x]=1;
  for(int y=12;y<25;y++) if(y!=19) paredes[y][8]=1;
  adicionar(TipoObjeto.MESA,6,6); adicionar(TipoObjeto.MESA,14,6); adicionar(TipoObjeto.MESA,29,7);
  adicionar(TipoObjeto.ESTANTE,4,14); adicionar(TipoObjeto.ESTANTE,11,15); adicionar(TipoObjeto.ESTANTE,30,15); adicionar(TipoObjeto.ESTANTE,34,30);
  adicionar(TipoObjeto.ARMARIO,16,20); adicionar(TipoObjeto.ARMARIO,27,22); adicionar(TipoObjeto.ARMARIO,35,8);
 }
 private void adicionar(TipoObjeto t,int x,int y){objetos.add(new ObjetoMapa(t,x,y));}
 public boolean parede(double x,double y){int mx=(int)x,my=(int)y;return mx<0||my<0||mx>=LARGURA||my>=ALTURA||paredes[my][mx]==1;}
 public ObjetoMapa objetoEm(int x,int y){for(ObjetoMapa o:objetos)if(o.getX()==x&&o.getY()==y)return o;return null;}
 public ObjetoMapa objetoEm(double x,double y){return objetoEm((int)x,(int)y);}
 public boolean bloqueado(double x,double y){if(parede(x,y))return true;ObjetoMapa o=objetoEm(x,y);return o!=null&&o.isSolido();}
 public List<ObjetoMapa> getObjetos(){return Collections.unmodifiableList(objetos);}
}
