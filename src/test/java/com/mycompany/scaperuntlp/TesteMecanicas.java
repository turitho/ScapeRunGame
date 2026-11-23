package com.mycompany.scaperuntlp;

import com.mycompany.scaperuntlp.controller.InputController;
import com.mycompany.scaperuntlp.controller.JogadorController;
import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.model.TipoObjeto;
import com.mycompany.scaperuntlp.view.JogoView;

/** Testes simples de execução direta; não dependem de JUnit. */
public class TesteMecanicas {

    private static void exigir(boolean condicao, String descricao) {
        if (!condicao) throw new AssertionError(descricao);
    }

    private static boolean proximo(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }

    public static void main(String[] args) {
        MapaJogo mapa = new MapaJogo();
        Jogador jogador = new Jogador();
        InputController input = new InputController();
        JogadorController controles = new JogadorController(jogador, mapa, input);

        exigir(mapa.getObjetos().size() == 13, "Mapa conserva os 13 móveis");
        exigir(mapa.livreParaCirculo(3.5, 3.5, 0.2), "Spawn liberado");
        exigir(!mapa.livreParaCirculo(0.95, 3.5, 0.2), "Parede bloqueia");
        exigir(!mapa.livreParaCirculo(6.5, 6.5, 0.2), "Mesa bloqueia");
        exigir(!mapa.livreParaCirculo(12.5, 6.2, 0.2), "Cama longa bloqueia");

        // A altura foi reduzida para proporções plausíveis.
        exigir(mapa.objetoEm(6, 6).getAlturaRelativa() < 0.40, "Mesa é baixa");
        exigir(mapa.objetoEm(12, 5).getAlturaRelativa() < 0.30, "Cama é baixa");
        exigir(mapa.objetoEm(16, 20).getAlturaRelativa() < 0.90, "Armário menor");

        jogador.posicionar(16.5, 21.5, -Math.PI / 2);
        ObjetoMapa armario = mapa.objetoEm(16, 20);
        exigir(controles.getAlvoInteracao() == armario, "Armário à frente");

        controles.interagir();
        exigir(armario.isAberto() && !armario.isOcupado(), "Primeiro E abre");
        controles.fecharArmario();
        exigir(!armario.isAberto(), "F fecha");
        controles.interagir();
        controles.interagir();
        exigir(jogador.isEscondido() && armario.isOcupado(), "Segundo E entra");
        exigir(proximo(jogador.getX(), 16.5) && proximo(jogador.getY(), 20.5),
                "Personagem foi fisicamente para o centro do armário");
        exigir(proximo(jogador.getAngulo(), Math.PI / 2), "Olha para a porta");
        // A área do armário é sólida, mas o jogador escondido pode ocupá-la.
        exigir(!mapa.livreParaCirculo(jogador.getX(), jogador.getY(), 0.2),
                "Posição interna pertence ao armário sólido");
        controles.moverComColisao(1, 0);
        exigir(proximo(jogador.getX(), 16.5), "Movimento desabilitado dentro");
        controles.fecharArmario();
        exigir(armario.isOcupado(), "F não fecha armário ocupado");
        controles.interagir();
        exigir(!jogador.isEscondido() && armario.isAberto(), "E sai do armário");
        exigir(proximo(jogador.getX(), 16.5) && proximo(jogador.getY(), 21.5),
                "Retorna ao local real de entrada");
        exigir(proximo(jogador.getAngulo(), -Math.PI / 2), "Restaura a direção");
        exigir(mapa.livreParaCirculo(jogador.getX(), jogador.getY(), 0.2),
                "Saída ocorre em posição válida");

        // Fora da porta não é possível entrar pelo fundo do armário.
        jogador.posicionar(16.5, 19.2, Math.PI / 2);
        exigir(controles.getAlvoInteracao() != armario,
                "Interação do armário somente pelo lado da porta");

        ObjetoMapa cama = mapa.objetoEm(31, 27);
        exigir(cama.getRotacao() == 90, "Cama girada");
        exigir(cama.getLarguraNoMundo() > cama.getProfundidadeNoMundo(),
                "Rotação troca as dimensões horizontais");
        ObjetoMapa estante = mapa.objetoEm(11, 15);
        estante.setAlturaRelativa(0.68);
        exigir(proximo(estante.getAlturaRelativa(), 0.68), "Altura ajustável");

        // Colisão contínua com subpassos.
        jogador.posicionar(5.30, 6.50, 0);
        controles.moverComColisao(3, 0);
        exigir(jogador.getX() < 6.0, "Movimento rápido não atravessa mesa");

        // Exercita a rasterização de móveis e de esconderijo (não testa a GPU).
        JogoView view = new JogoView();
        view.desenhar(jogador, mapa, armario, "Teste");
        jogador.posicionar(16.5, 21.5, -Math.PI / 2);
        jogador.entrarNoArmario(armario, jogador.getX(), jogador.getY());
        view.desenhar(jogador, mapa, armario, "Dentro do armário");
        jogador.sairDoArmario();
        view.desenhar(jogador, mapa, armario, "Fora do armário");

        System.out.println("OK: colisões, móveis menores, armário real e renderização.");
    }
}
