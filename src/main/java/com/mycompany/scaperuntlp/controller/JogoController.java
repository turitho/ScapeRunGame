package com.mycompany.scaperuntlp.controller;

import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.view.JogoView;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

/** Mantém o ciclo de jogo no Controller existente (sem alterar o MVC). */
public class JogoController {
    // Radianos por pixel. Ajuste os dois separadamente, se necessário.
    private static final double SENSIBILIDADE_MOUSE_HORIZONTAL = 0.0025;
    private static final double SENSIBILIDADE_MOUSE_VERTICAL = 0.0025;
    private final InputController input = new InputController();
    private final Jogador jogador = new Jogador();
    private final MapaJogo mapa = new MapaJogo();
    private final JogadorController jogadorController = new JogadorController(jogador, mapa, input);
    private final JogoView view;
    private String mensagem = "";
    private double tempoMensagem;
    // Medicao simples de FPS (ciclo de atualizacao) para diagnostico na tela.
    private long inicioContagemFps;
    private int quadrosNoIntervalo;

    public JogoController(JogoView view) {
        this.view = view;
    }

    public void iniciar(Scene cena) {
        input.conectar(cena);
        view.desenhar(jogador, mapa, null, "");
        new AnimationTimer() {
            private long anterior;

            @Override
            public void handle(long agora) {
                if (anterior == 0) {
                    anterior = agora;
                    return;
                }
                double dt = Math.min((agora - anterior) / 1_000_000_000.0, 0.05);
                anterior = agora;

                // O InputController lê o mouse; o Model guarda a direção da câmera.
                // Mesmo escondido, consumimos o deslocamento para não acumular giros.
                double deltaMouseX = input.consumirMovimentoMouseX();
                double deltaMouseY = input.consumirMovimentoMouseY();
                if (!jogador.isEscondido()) {
                    jogador.girar(deltaMouseX * SENSIBILIDADE_MOUSE_HORIZONTAL);
                    // No JavaFX, Y positivo aponta para baixo na tela.
                    // Assim, mover o mouse para cima aumenta o pitch.
                    jogador.inclinar(-deltaMouseY * SENSIBILIDADE_MOUSE_VERTICAL);
                }

                if (input.consumirInteracao()) jogadorController.interagir();
                if (input.consumirAcao(KeyCode.F)) jogadorController.fecharArmario();
                // F3 troca a qualidade interna sem alterar o tamanho da janela.
                if (input.consumirAcao(KeyCode.F3)) view.alternarQualidade();
                jogadorController.atualizar(dt);

                String novidade = jogadorController.consumirMensagem();
                if (!novidade.isEmpty()) {
                    mensagem = novidade;
                    tempoMensagem = 3.0;
                }
                tempoMensagem = Math.max(0, tempoMensagem - dt);
                // Atualiza o indicador em intervalos, evitando texto novo a cada frame.
                if (inicioContagemFps == 0) inicioContagemFps = agora;
                quadrosNoIntervalo++;
                double segundos = (agora - inicioContagemFps) / 1_000_000_000.0;
                if (segundos >= 0.6) {
                    view.atualizarFps(quadrosNoIntervalo / segundos);
                    quadrosNoIntervalo = 0;
                    inicioContagemFps = agora;
                }
                ObjetoMapa alvo = jogadorController.getAlvoInteracao();
                view.desenhar(jogador, mapa, alvo, tempoMensagem > 0 ? mensagem : "");
            }
        }.start();
    }
}
