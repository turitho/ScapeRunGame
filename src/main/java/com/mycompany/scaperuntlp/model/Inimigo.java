
package com.mycompany.scaperuntlp.model;

public class Inimigo {

    public enum Estado {
        PATRULHANDO,
        PERSEGUINDO,
        PROCURANDO
    }

    private double x = 12.5;
    private double y = 3.5;

    private final double velocidade = 2.0;
    private final double raioDeteccao = 7.0;
    private final double raioFuga = 11.0;

    private double destinoX = 12.5;
    private double destinoY = 3.5;

    private Estado estado = Estado.PATRULHANDO;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelocidade() {
        return velocidade;
    }

    public double getRaioDeteccao() {
        return raioDeteccao;
    }

    public double getRaioFuga() {
        return raioFuga;
    }

    public double getDestinoX() {
        return destinoX;
    }

    public double getDestinoY() {
        return destinoY;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setDestino(double x, double y) {
        destinoX = x;
        destinoY = y;
    }

    public void mover(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void definirPosicao(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
