package com.mycompany.scaperuntlp.model;
public class Jogador {
 private double x=3.5,y=3.5,angulo=0; private final double velocidade=3.0,velocidadeRotacao=2.0;
 private boolean escondido=false; private double saidaX,saidaY;
 public double getX(){return x;} public double getY(){return y;} public double getAngulo(){return angulo;}
 public double getVelocidade(){return velocidade;} public double getVelocidadeRotacao(){return velocidadeRotacao;}
 public void mover(double dx,double dy){x+=dx;y+=dy;} public void girar(double d){angulo+=d;}
 public boolean isEscondido(){return escondido;}
 public void entrarNoArmario(double xProximo,double yProximo){saidaX=xProximo;saidaY=yProximo;escondido=true;}
 public void sairDoArmario(){x=saidaX;y=saidaY;escondido=false;}
}
