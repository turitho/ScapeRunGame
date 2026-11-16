
package com.mycompany.scaperuntlp.controller;

import com.mycompany.scaperuntlp.model.*;
import java.util.*;

public class InimigoController {

    private final Inimigo inimigo;
    private final Jogador jogador;
    private final MapaJogo mapa;

    private final double[][] patrulha = {
        {8.5, 3.5},
        {15.5, 3.5},
        {15.5, 8.5},
        {8.5, 8.5}
    };

    private int pontoAtual = 0;
    private double ultimaX;
    private double ultimaY;

    public InimigoController(Inimigo inimigo, Jogador jogador,
            MapaJogo mapa) {
        this.inimigo = inimigo;
        this.jogador = jogador;
        this.mapa = mapa;
    }

    public void atualizar(double dt) {

        if (jogador.isEscondido()) {
            if (inimigo.getEstado() == Inimigo.Estado.PERSEGUINDO) {
                inimigo.setEstado(Inimigo.Estado.PROCURANDO);
                inimigo.setDestino(ultimaX, ultimaY);
            }
            moverAteDestino(dt);
            return;
        }

        double distancia = Math.hypot(
                jogador.getX() - inimigo.getX(),
                jogador.getY() - inimigo.getY()
        );

        if (inimigo.getEstado() == Inimigo.Estado.PATRULHANDO
                && distancia <= inimigo.getRaioDeteccao()) {

            inimigo.setEstado(Inimigo.Estado.PERSEGUINDO);
        }

        if (inimigo.getEstado() == Inimigo.Estado.PERSEGUINDO) {

            if (distancia > inimigo.getRaioFuga()) {
                inimigo.setEstado(Inimigo.Estado.PROCURANDO);
                inimigo.setDestino(ultimaX, ultimaY);
            } else {
                ultimaX = jogador.getX();
                ultimaY = jogador.getY();
                inimigo.setDestino(ultimaX, ultimaY);
            }
        }

        if (inimigo.getEstado() == Inimigo.Estado.PROCURANDO) {
            moverAteDestino(dt);

            if (chegouAoDestino()) {
                inimigo.setEstado(Inimigo.Estado.PATRULHANDO);
            }
            return;
        }

        if (inimigo.getEstado() == Inimigo.Estado.PATRULHANDO) {
            if (chegouAoDestino()) {
                pontoAtual = (pontoAtual + 1) % patrulha.length;
                inimigo.setDestino(
                        patrulha[pontoAtual][0],
                        patrulha[pontoAtual][1]
                );
            }
        }

        moverAteDestino(dt);
    }

    private boolean chegouAoDestino() {
        return Math.hypot(
                inimigo.getDestinoX() - inimigo.getX(),
                inimigo.getDestinoY() - inimigo.getY()
        ) < 0.25;
    }

    private void moverAteDestino(double dt) {

        double dx = inimigo.getDestinoX() - inimigo.getX();
        double dy = inimigo.getDestinoY() - inimigo.getY();

        double distancia = Math.hypot(dx, dy);

        if (distancia < 0.05) {
            return;
        }

        double passo = Math.min(
                inimigo.getVelocidade() * dt,
                distancia
        );

        double novoX = inimigo.getX() + dx / distancia * passo;
        double novoY = inimigo.getY() + dy / distancia * passo;

        if (!mapa.bloqueado(novoX, inimigo.getY())) {
            inimigo.mover(novoX - inimigo.getX(), 0);
        }

        if (!mapa.bloqueado(inimigo.getX(), novoY)) {
            inimigo.mover(0, novoY - inimigo.getY());
        }
    }
}
