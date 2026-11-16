package com.mycompany.scaperuntlp.controller;

import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.model.TipoObjeto;
import javafx.scene.input.KeyCode;

/** Controla movimentação, colisões e interações do jogador. */
public class JogadorController {
    private static final double DISTANCIA_INTERACAO = 1.45;
    private static final double RAIO_JOGADOR = 0.20;

    private final Jogador jogador;
    private final MapaJogo mapa;
    private final InputController input;

    public JogadorController(Jogador jogador, MapaJogo mapa, InputController input) {
        this.jogador = jogador;
        this.mapa = mapa;
        this.input = input;
    }

    public void atualizar(double dt) {
        if (jogador.isEscondido()) {
            return;
        }

        atualizarRotacao(dt);
        atualizarMovimento(dt);
    }

    private void atualizarRotacao(double dt) {
        if (input.pressionada(KeyCode.LEFT) || input.pressionada(KeyCode.A)) {
            jogador.girar(-jogador.getVelocidadeRotacao() * dt);
        }
        if (input.pressionada(KeyCode.RIGHT) || input.pressionada(KeyCode.D)) {
            jogador.girar(jogador.getVelocidadeRotacao() * dt);
        }
    }

    private void atualizarMovimento(double dt) {
        double direcao = 0;
        if (input.pressionada(KeyCode.W) || input.pressionada(KeyCode.UP)) {
            direcao++;
        }
        if (input.pressionada(KeyCode.S) || input.pressionada(KeyCode.DOWN)) {
            direcao--;
        }

        double dx = Math.cos(jogador.getAngulo()) * direcao * jogador.getVelocidade() * dt;
        double dy = Math.sin(jogador.getAngulo()) * direcao * jogador.getVelocidade() * dt;

        if (!mapa.bloqueado(jogador.getX() + dx + Math.signum(dx) * RAIO_JOGADOR,
                jogador.getY())) {
            jogador.mover(dx, 0);
        }
        if (!mapa.bloqueado(jogador.getX(),
                jogador.getY() + dy + Math.signum(dy) * RAIO_JOGADOR)) {
            jogador.mover(0, dy);
        }
    }

    /**
     * E abre um armário fechado; em um armário aberto, entra; escondido,
     * E é tratado como saída pelo próprio jogador.
     */
    public void interagir() {
        if (jogador.isEscondido()) {
            jogador.sairDoArmario();
            return;
        }

        ObjetoMapa alvo = localizarObjetoÀFrente();
        if (alvo == null || alvo.getTipo() != TipoObjeto.ARMARIO) {
            return;
        }

        if (!alvo.isAberto()) {
            alvo.abrir();
            return;
        }

        entrarNoArmario(alvo);
    }

    private ObjetoMapa localizarObjetoÀFrente() {
        double cos = Math.cos(jogador.getAngulo());
        double sin = Math.sin(jogador.getAngulo());

        // Amostras curtas para localizar o objeto que está diante do jogador.
        for (double distancia = 0.45; distancia <= DISTANCIA_INTERACAO; distancia += 0.05) {
            ObjetoMapa objeto = mapa.objetoEm(
                    jogador.getX() + cos * distancia,
                    jogador.getY() + sin * distancia);
            if (objeto != null) {
                return objeto;
            }
            if (mapa.parede(jogador.getX() + cos * distancia,
                    jogador.getY() + sin * distancia)) {
                return null;
            }
        }
        return null;
    }

    private void entrarNoArmario(ObjetoMapa armario) {
        double centroX = armario.getX() + 0.5;
        double centroY = armario.getY() + 0.5;
        double dx = jogador.getX() - centroX;
        double dy = jogador.getY() - centroY;
        double comprimento = Math.hypot(dx, dy);

        if (comprimento < 0.001) {
            dx = -Math.cos(jogador.getAngulo());
            dy = -Math.sin(jogador.getAngulo());
            comprimento = 1.0;
        }

        // Guarda uma posição segura do lado de fora para o momento da saída.
        double saidaX = centroX + (dx / comprimento) * 0.85;
        double saidaY = centroY + (dy / comprimento) * 0.85;
        jogador.entrarNoArmario(saidaX, saidaY);
    }
}
