package com.mycompany.scaperuntlp.prototipo;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderizar {

    private final Canvas canvas;
    private final GraphicsContext gc;

    private final int[][] mapa = {
        {1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1}
    };

    public Renderizar(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public boolean parede(double x, double y) {

        int mapaX = (int) x;
        int mapaY = (int) y;

        if (mapaX < 0 || mapaY < 0
                || mapaX >= mapa[0].length
                || mapaY >= mapa.length) {
            return true;
        }

        return mapa[mapaY][mapaX] == 1;
    }

    public void desenhar(Eixos jogador) {

        double largura = canvas.getWidth();
        double altura = canvas.getHeight();
        double horizonte = altura / 2.0;

        // Teto
        gc.setFill(Color.rgb(35, 35, 42));
        gc.fillRect(0, 0, largura, horizonte);

        // Chão
        gc.setFill(Color.rgb(90, 80, 70));
        gc.fillRect(0, horizonte, largura, altura - horizonte);

        double fov = Math.toRadians(60);
        double meioFov = fov / 2.0;

        for (int coluna = 0; coluna < (int) largura; coluna++) {

            double cameraX = 2.0 * coluna / largura - 1.0;

            double anguloRaio = jogador.angulo
                    + Math.atan(cameraX * Math.tan(meioFov));

            double raioX = Math.cos(anguloRaio);
            double raioY = Math.sin(anguloRaio);

            double distancia = 0;
            double passo = 0.02;

            while (distancia < 20) {

                distancia += passo;

                double testeX = jogador.x + raioX * distancia;
                double testeY = jogador.y + raioY * distancia;

                if (parede(testeX, testeY)) {
                    break;
                }
            }

            double distanciaCorrigida = distancia
                    * Math.cos(anguloRaio - jogador.angulo);

            distanciaCorrigida = Math.max(0.05, distanciaCorrigida);

            double alturaParede = altura / distanciaCorrigida;
            double inicioParede = horizonte - alturaParede / 2.0;

            double brilho = 1.0 / (1.0 + distanciaCorrigida * 0.18);
            brilho = Math.max(0.15, Math.min(1.0, brilho));

            gc.setFill(Color.color(
                    0.65 * brilho,
                    0.65 * brilho,
                    0.68 * brilho
            ));

            gc.fillRect(
                    coluna,
                    inicioParede,
                    1,
                    alturaParede
            );
        }
    }
}