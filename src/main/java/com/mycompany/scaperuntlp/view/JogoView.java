package com.mycompany.scaperuntlp.view;

import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.model.TipoObjeto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** View responsável pela apresentação 3D simplificada do mapa. */
public class JogoView extends StackPane {
    private static final double LARGURA_TELA = 960;
    private static final double ALTURA_TELA = 640;
    private static final double CAMPO_VISAO = Math.toRadians(60);
    private static final double DISTANCIA_MAXIMA = 35;
    private static final double PASSO_RAYCAST = 0.025;

    private final Canvas canvas = new Canvas(LARGURA_TELA, ALTURA_TELA);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    public JogoView() {
        getChildren().add(canvas);
        setStyle("-fx-background-color: black;");
    }

    public void desenhar(Jogador jogador, MapaJogo mapa) {
        double largura = canvas.getWidth();
        double altura = canvas.getHeight();
        double horizonte = altura / 2;

        if (jogador.isEscondido()) {
            desenharTelaEscondido(largura, altura);
            return;
        }

        desenharCeuEChao(largura, altura, horizonte);

        for (int coluna = 0; coluna < (int) largura; coluna++) {
            desenharColuna(coluna, largura, altura, horizonte, jogador, mapa);
        }

        desenharHud(largura, horizonte);
    }

    private void desenharTelaEscondido(double largura, double altura) {
        gc.setFill(Color.rgb(12, 10, 9));
        gc.fillRect(0, 0, largura, altura);
        gc.setFill(Color.BEIGE);
        gc.setFont(Font.font(22));
        gc.fillText("ESCONDIDO NO ARMÁRIO — pressione E para sair",
                largura / 2 - 230, altura / 2);
    }

    private void desenharCeuEChao(double largura, double altura, double horizonte) {
        gc.setFill(Color.rgb(28, 29, 36));
        gc.fillRect(0, 0, largura, horizonte);
        gc.setFill(Color.rgb(75, 67, 58));
        gc.fillRect(0, horizonte, largura, altura - horizonte);
    }

    /**
     * Desenha uma coluna da visão em primeira pessoa.
     *
     * A parede é desenhada primeiro, como fundo. Depois os móveis são
     * desenhados da maior distância para a menor. Assim, a parte do móvel
     * que não ocupa altura alguma fica realmente livre para mostrar o cenário
     * e os objetos que estão atrás dele.
     */
    private void desenharColuna(int coluna, double largura, double altura,
            double horizonte, Jogador jogador, MapaJogo mapa) {
        double camera = 2.0 * coluna / largura - 1;
        double anguloRaio = jogador.getAngulo()
                + Math.atan(camera * Math.tan(CAMPO_VISAO / 2));
        double raioX = Math.cos(anguloRaio);
        double raioY = Math.sin(anguloRaio);

        double distanciaParede = DISTANCIA_MAXIMA;
        boolean paredeEncontrada = false;
        List<AcertoObjeto> objetosEncontrados = new ArrayList<>();
        Set<String> celulasVisitadas = new HashSet<>();

        // Percorre o raio até encontrar uma parede ou atingir a distância máxima.
        for (double distancia = PASSO_RAYCAST;
                distancia < DISTANCIA_MAXIMA; distancia += PASSO_RAYCAST) {
            double x = jogador.getX() + raioX * distancia;
            double y = jogador.getY() + raioY * distancia;

            if (mapa.parede(x, y)) {
                distanciaParede = distancia;
                paredeEncontrada = true;
                break;
            }

            ObjetoMapa objeto = mapa.objetoEm(x, y);
            if (objeto != null) {
                String chaveCelula = objeto.getX() + ":" + objeto.getY();
                if (celulasVisitadas.add(chaveCelula)) {
                    objetosEncontrados.add(new AcertoObjeto(objeto, distancia));
                }
            }
        }

        // Corrige o efeito de distorção nas laterais da tela.
        double correcao = Math.cos(anguloRaio - jogador.getAngulo());
        double distanciaParedeCorrigida = Math.max(0.05, distanciaParede * correcao);

        // O cenário atrás dos móveis é desenhado antes deles.
        if (paredeEncontrada) {
            double alturaParede = altura / distanciaParedeCorrigida;
            double topoParede = horizonte - alturaParede / 2;
            double brilhoParede = calcularBrilho(distanciaParedeCorrigida);
            gc.setFill(Color.rgb(115, 118, 125).deriveColor(0, 1, brilhoParede, 1));
            gc.fillRect(coluna, topoParede, 1, alturaParede);
        }

        // Desenha primeiro os objetos distantes; os próximos ficam por cima.
        objetosEncontrados.sort(Comparator.comparingDouble(
                (AcertoObjeto acerto) -> acerto.distancia).reversed());

        for (AcertoObjeto acerto : objetosEncontrados) {
            double distanciaCorrigida = Math.max(0.05, acerto.distancia * correcao);
            double alturaParedeNaDistancia = altura / distanciaCorrigida;
            double alturaObjeto = alturaParedeNaDistancia
                    * acerto.objeto.getAlturaRelativa();
            double baseObjeto = horizonte + alturaParedeNaDistancia / 2;
            double topoObjeto = baseObjeto - alturaObjeto;
            double brilho = calcularBrilho(distanciaCorrigida);

            gc.setFill(corObjeto(acerto.objeto).deriveColor(0, 1, brilho, 1));
            gc.fillRect(coluna, topoObjeto, 1, alturaObjeto);
        }
    }

    private double calcularBrilho(double distancia) {
        return Math.max(0.18, Math.min(1, 1 / (1 + distancia * 0.10)));
    }

    /** Um móvel atingido pelo raio e a distância aproximada até ele. */
    private static class AcertoObjeto {
        private final ObjetoMapa objeto;
        private final double distancia;

        private AcertoObjeto(ObjetoMapa objeto, double distancia) {
            this.objeto = objeto;
            this.distancia = distancia;
        }
    }

    private void desenharHud(double largura, double horizonte) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("W/S: mover   A/D ou ←/→: girar   E: abrir, entrar ou sair do armário",
                18, 28);
        gc.setFill(Color.color(1, 1, 1, 0.8));
        gc.fillOval(largura / 2 - 2, horizonte - 2, 4, 4);
    }

    private Color corObjeto(ObjetoMapa objeto) {
        if (objeto.getTipo() == TipoObjeto.MESA) {
            return Color.SADDLEBROWN;
        }
        if (objeto.getTipo() == TipoObjeto.ESTANTE) {
            return Color.DARKGREEN;
        }
        if (objeto.getTipo() == TipoObjeto.ARMARIO) {
            return objeto.isAberto() ? Color.SANDYBROWN : Color.DARKRED;
        }
        if (objeto.getTipo() == TipoObjeto.CAMA) {
            return Color.LIGHTSTEELBLUE;
        }
        return Color.GRAY;
    }
}
