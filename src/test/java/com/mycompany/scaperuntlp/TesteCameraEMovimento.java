package com.mycompany.scaperuntlp;

import com.mycompany.scaperuntlp.controller.InputController;
import com.mycompany.scaperuntlp.controller.JogadorController;
import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import javafx.scene.input.KeyCode;
import java.util.EnumSet;
import java.util.Set;

/** Verifica movimento relativo à câmera, diagonais e inclinação vertical. */
public class TesteCameraEMovimento {

    private static final class InputSimulado extends InputController {
        private final Set<KeyCode> teclas = EnumSet.noneOf(KeyCode.class);

        void definir(KeyCode... pressionadas) {
            teclas.clear();
            for (KeyCode tecla : pressionadas) {
                teclas.add(tecla);
            }
        }

        @Override
        public boolean pressionada(KeyCode tecla) {
            return teclas.contains(tecla);
        }
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static boolean proximo(double a, double b) {
        return Math.abs(a - b) < 0.000001;
    }

    public static void main(String[] args) {
        MapaJogo mapa = new MapaJogo();
        Jogador jogador = new Jogador();
        InputSimulado input = new InputSimulado();
        JogadorController controle = new JogadorController(jogador, mapa, input);

        input.definir(KeyCode.W);
        controle.atualizar(0.1);
        exigir(proximo(jogador.getX(), 3.8) && proximo(jogador.getY(), 3.5),
                "W deve avançar na direção horizontal da câmera");

        jogador.posicionar(3.5, 3.5, 0);
        input.definir(KeyCode.A);
        controle.atualizar(0.1);
        exigir(proximo(jogador.getX(), 3.5) && proximo(jogador.getY(), 3.2),
                "A deve mover lateralmente sem girar");
        exigir(proximo(jogador.getAngulo(), 0), "Teclado não pode girar a câmera");

        jogador.posicionar(3.5, 3.5, 0);
        input.definir(KeyCode.W, KeyCode.D);
        controle.atualizar(0.1);
        double dx = jogador.getX() - 3.5;
        double dy = jogador.getY() - 3.5;
        exigir(proximo(Math.hypot(dx, dy), 0.3),
                "W+D deve ter velocidade normalizada, igual ao movimento reto");
        exigir(dx > 0 && dy > 0, "W+D deve andar diagonalmente");

        jogador.posicionar(3.5, 3.5, Math.PI / 2);
        input.definir(KeyCode.UP, KeyCode.RIGHT);
        controle.atualizar(0.1);
        exigir(jogador.getX() < 3.5 && jogador.getY() > 3.5,
                "Setas para cima e direita devem andar diagonalmente relativamente à câmera");
        exigir(proximo(jogador.getAngulo(), Math.PI / 2),
                "As setas não podem alterar a orientação");

        jogador.inclinar(Math.toRadians(90));
        exigir(proximo(jogador.getInclinacaoCamera(), Math.toRadians(50)),
                "Olhando para cima deve limitar a inclinação a 50 graus");
        jogador.inclinar(Math.toRadians(-180));
        exigir(proximo(jogador.getInclinacaoCamera(), Math.toRadians(-50)),
                "Olhando para baixo deve limitar a inclinação a -50 graus");

        System.out.println("OK: W/A/S/D, setas, diagonais normalizadas, yaw preservado e pitch limitado.");
    }
}
