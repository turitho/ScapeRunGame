package com.mycompany.scaperuntlp.model;
public class ObjetoMapa {
 private final TipoObjeto tipo; private final int x,y; private boolean aberto=false;
 public ObjetoMapa(TipoObjeto tipo,int x,int y){this.tipo=tipo;this.x=x;this.y=y;}
 public TipoObjeto getTipo(){return tipo;} public int getX(){return x;} public int getY(){return y;}
 public boolean isAberto(){return aberto;} public void alternarAberto(){if(tipo==TipoObjeto.ARMARIO) aberto=!aberto;}
 public boolean isSolido(){return tipo!=TipoObjeto.ARMARIO || !aberto;}
}
