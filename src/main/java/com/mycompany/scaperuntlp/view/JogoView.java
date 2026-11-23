package com.mycompany.scaperuntlp.view;

import com.mycompany.scaperuntlp.model.Jogador;
import com.mycompany.scaperuntlp.model.MapaJogo;
import com.mycompany.scaperuntlp.model.ObjetoMapa;
import com.mycompany.scaperuntlp.model.TipoObjeto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * View em primeira pessoa com projeção 3D e teste de profundidade.
 * Paredes e móveis são compostos por quadriláteros; os triângulos próximos
 * à câmera são recortados no plano de visão antes da projeção.
 * Isso evita faces gigantes/triângulos esticados ao olhar para baixo perto
 * de um móvel, sem remover o movimento vertical da câmera.
 */
public class JogoView extends StackPane {

    private static final int LARGURA_TELA = 960;
    private static final int ALTURA_TELA = 640;
    private static final double FOV = Math.toRadians(65);
    private static final double TAN_MEIO_FOV = Math.tan(FOV / 2.0);

    // F3 alterna entre 960x640 (alta), 768x512 (media) e 576x384 (rapida).
    // O HUD e a janela permanecem sempre em 960x640.
    private static final int[] QUALIDADES = {768, 960, 576};
    private static final String[] NOMES_QUALIDADE = {"MEDIA", "ALTA", "RAPIDA"};
    private int indiceQualidade;
    private int larguraRender;
    private int alturaRender;
    private double focal;
    private static final double OLHOS_Z = 0.64;
    private static final double DISTANCIA_MAXIMA = 40.0;
    // Faces atrás deste plano não são projetadas: evita divisão por zero
    // e polígonos que se prolongam até o infinito perto do observador.
    private static final double PLANO_PROXIMO = 0.12;

    // Cores dos móveis em RGB, sem transparência.
    private static final int MADEIRA = 0x765037;
    private static final int MADEIRA_CLARA = 0xA4764F;
    private static final int MADEIRA_ESCURO = 0x4D3325;
    private static final int MADEIRA_INTERIOR = 0x3C302B;
    private static final int VERDE = 0x566B53;
    private static final int COLCHAO = 0x96B5C7;
    private static final int COBERTOR = 0x577D9C;
    private static final int TRAVESSEIRO = 0xD9D6C9;

    private final Canvas canvas = new Canvas(LARGURA_TELA, ALTURA_TELA);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private WritableImage quadro;
    private PixelWriter pixelWriter;
    private int[] pixels;
    private float[] profundidades;
    private double fpsAtual;

    // Objetos imutaveis entre alteracoes do mapa: evitamos recriar suas caixas.
    private MapaJogo mapaCacheObjetos;
    private long versaoCacheObjetos = Long.MIN_VALUE;
    private List<Peca> pecasCache;

    // As faces da parede sao estaticas e so precisam ser montadas uma vez.
    private MapaJogo mapaCacheParedes;
    private List<FaceParede> facesParedesCache;

    // Um unico conjunto de buffers para recorte, reusado em cada triangulo.
    private final Vertice[] entradaRecorte = new Vertice[3];
    private final Vertice[] saidaRecorte = new Vertice[4];

    public JogoView() {
        getChildren().add(canvas);
        setStyle("-fx-background-color: black;");
        ajustarQualidade(0);
    }

    /** Alterna a resolucao interna sem alterar a resolucao da janela. */
    public void alternarQualidade() {
        ajustarQualidade((indiceQualidade + 1) % QUALIDADES.length);
    }

    public void atualizarFps(double fps) {
        this.fpsAtual = fps;
    }

    private void ajustarQualidade(int indice) {
        indiceQualidade = indice;
        larguraRender = QUALIDADES[indice];
        alturaRender = larguraRender * ALTURA_TELA / LARGURA_TELA;
        focal = (larguraRender / 2.0) / TAN_MEIO_FOV;
        quadro = new WritableImage(larguraRender, alturaRender);
        pixelWriter = quadro.getPixelWriter();
        pixels = new int[larguraRender * alturaRender];
        profundidades = new float[larguraRender * alturaRender];
    }

    public void desenhar(Jogador jogador, MapaJogo mapa) {
        desenhar(jogador, mapa, null, "");
    }

    public void desenhar(Jogador jogador, MapaJogo mapa,
            ObjetoMapa alvo, String mensagem) {
        // Uma transformação 3D única é usada por paredes, móveis, chão
        // e movimento vertical do mouse: sem deslocamento artificial do horizonte.
        Camera camera = new Camera(jogador);
        preencherFundo(camera);
        renderizarParedes(mapa, camera);
        for (Peca peca : obterPecas(mapa)) {
            renderizarCaixa(peca, camera);
        }

        pixelWriter.setPixels(0, 0, larguraRender, alturaRender,
                PixelFormat.getIntArgbPreInstance(), pixels, 0, larguraRender);
        gc.drawImage(quadro, 0, 0, LARGURA_TELA, ALTURA_TELA);
        desenharHud(jogador, alvo, mensagem);
    }

    /**
     * O chão é um plano em z=0. Cada pixel usa o mesmo raio 3D da câmera.
     * Assim, olhando para baixo o chão continua no lugar, mesmo perto
     * de uma parede ou de um móvel. O céu não tem profundidade.
     */
    private void preencherFundo(Camera camera) {
        // O Z-buffer recebe 0 para a area do ceu. O chao armazena 1/distancia.
        Arrays.fill(profundidades, 0);
        final double inversoFocal = 1.0 / focal;
        final double uInicial = (0.5 - larguraRender / 2.0) * inversoFocal;
        final double passoU = inversoFocal;

        for (int linha = 0; linha < alturaRender; linha++) {
            double v = (alturaRender / 2.0 - linha - 0.5) * inversoFocal;
            double direcaoZ = camera.senPitch + v * camera.cosPitch;
            double frente = camera.cosPitch - v * camera.senPitch;
            int inicioLinha = linha * larguraRender;
            boolean olhaParaChao = direcaoZ < -0.000001;
            double distanciaPlano = olhaParaChao ? -OLHOS_Z / direcaoZ : 0;

            if (!olhaParaChao || distanciaPlano > DISTANCIA_MAXIMA * 5) {
                int variacao = 11 * linha / alturaRender;
                Arrays.fill(pixels, inicioLinha, inicioLinha + larguraRender,
                        rgb(23 + variacao, 28 + variacao, 39 + variacao));
                continue;
            }

            // A projecao do chao varia linearmente por coluna. Em vez de
            // repetir trigonometria e sombreamento para cada pixel,
            // calculamos o incremento e as cores uma vez por linha.
            double mundoX = camera.x + distanciaPlano
                    * (frente * camera.cosYaw - uInicial * camera.senYaw);
            double mundoY = camera.y + distanciaPlano
                    * (frente * camera.senYaw + uInicial * camera.cosYaw);
            double passoX = -passoU * distanciaPlano * camera.senYaw;
            double passoY = passoU * distanciaPlano * camera.cosYaw;
            double escurecimento = Math.max(0.52, 1.0 - distanciaPlano * 0.009);
            int corPar = sombrear(0x554A41, escurecimento);
            int corImpar = sombrear(0x584D44, escurecimento);
            int corJunta = sombrear(0x4A423B, escurecimento);
            float profundidadeChao = (float) (1.0 / distanciaPlano);

            for (int coluna = 0; coluna < larguraRender; coluna++) {
                int x = (int) Math.floor(mundoX);
                int y = (int) Math.floor(mundoY);
                double fracaoX = mundoX - x;
                double fracaoY = mundoY - y;
                boolean junta = fracaoX < 0.018 || fracaoY < 0.018;
                int indice = inicioLinha + coluna;
                pixels[indice] = junta ? corJunta : (((x + y) & 1) == 0
                        ? corPar : corImpar);
                profundidades[indice] = profundidadeChao;
                mundoX += passoX;
                mundoY += passoY;
            }
        }
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Desenha apenas as faces externas das paredes, armazenadas em cache. */
    private void renderizarParedes(MapaJogo mapa, Camera camera) {
        if (facesParedesCache == null || mapaCacheParedes != mapa) {
            mapaCacheParedes = mapa;
            facesParedesCache = construirFacesParedes(mapa);
        }
        double distanciaMaximaQuadrada = (DISTANCIA_MAXIMA + 1.5)
                * (DISTANCIA_MAXIMA + 1.5);
        for (FaceParede p : facesParedesCache) {
            double dx = p.centroX - camera.x;
            double dy = p.centroY - camera.y;
            if (dx * dx + dy * dy > distanciaMaximaQuadrada) continue;

            // Visao para o lado oposto da face: ela nao pode ser vista.
            if (p.lado == 0 && camera.x > p.x) continue;
            if (p.lado == 1 && camera.x < p.x + 1) continue;
            if (p.lado == 2 && camera.y > p.y) continue;
            if (p.lado == 3 && camera.y < p.y + 1) continue;

            // Descarte de faces muito fora do campo de visao horizontal.
            // A margem preserva corretamente paredes proxima da camera.
            double frente = dx * camera.cosYaw + dy * camera.senYaw;
            double lateral = -dx * camera.senYaw + dy * camera.cosYaw;
            if (frente < -1.5 || Math.abs(lateral) > Math.max(0, frente)
                    * TAN_MEIO_FOV + 2.0) continue;

            int x = p.x;
            int y = p.y;
            switch (p.lado) {
                case 0:
                    face(camera, x, y, 0, x, y + 1, 0,
                            x, y + 1, 1, x, y, 1, 0x89898F, 0.92);
                    break;
                case 1:
                    face(camera, x + 1, y + 1, 0, x + 1, y, 0,
                            x + 1, y, 1, x + 1, y + 1, 1, 0x89898F, 0.92);
                    break;
                case 2:
                    face(camera, x + 1, y, 0, x, y, 0,
                            x, y, 1, x + 1, y, 1, 0x85858C, 0.80);
                    break;
                case 3:
                    face(camera, x, y + 1, 0, x + 1, y + 1, 0,
                            x + 1, y + 1, 1, x, y + 1, 1, 0x85858C, 0.80);
                    break;
                default: break;
            }
        }
    }

    private List<FaceParede> construirFacesParedes(MapaJogo mapa) {
        List<FaceParede> faces = new ArrayList<>();
        for (int y = 0; y < MapaJogo.ALTURA; y++) {
            for (int x = 0; x < MapaJogo.LARGURA; x++) {
                if (!mapa.celulaParede(x, y)) continue;
                if (!mapa.celulaParede(x - 1, y)) faces.add(new FaceParede(x, y, 0));
                if (!mapa.celulaParede(x + 1, y)) faces.add(new FaceParede(x, y, 1));
                if (!mapa.celulaParede(x, y - 1)) faces.add(new FaceParede(x, y, 2));
                if (!mapa.celulaParede(x, y + 1)) faces.add(new FaceParede(x, y, 3));
            }
        }
        return faces;
    }

    private static final class FaceParede {
        final int x, y, lado;
        final double centroX, centroY;

        FaceParede(int x, int y, int lado) {
            this.x = x;
            this.y = y;
            this.lado = lado;
            centroX = x + 0.5;
            centroY = y + 0.5;
        }
    }

    /** Renderiza as seis faces de cada caixa com seu volume real. */
    private void renderizarCaixa(Peca p, Camera camera) {
        double x0 = p.minX, x1 = p.maxX;
        double y0 = p.minY, y1 = p.maxY;
        double z0 = p.zMin, z1 = p.zMax;
        double centroX = (x0 + x1) * 0.5 - camera.x;
        double centroY = (y0 + y1) * 0.5 - camera.y;
        if (centroX * centroX + centroY * centroY >
                (DISTANCIA_MAXIMA + 2) * (DISTANCIA_MAXIMA + 2)) return;

        face(camera, x0, y0, z0, x1, y0, z0,
                x1, y0, z1, x0, y0, z1, p.cor, 0.80);
        face(camera, x1, y1, z0, x0, y1, z0,
                x0, y1, z1, x1, y1, z1, p.cor, 0.80);
        face(camera, x0, y1, z0, x0, y0, z0,
                x0, y0, z1, x0, y1, z1, p.cor, 0.90);
        face(camera, x1, y0, z0, x1, y1, z0,
                x1, y1, z1, x1, y0, z1, p.cor, 0.90);
        face(camera, x0, y0, z1, x1, y0, z1,
                x1, y1, z1, x0, y1, z1, p.cor, 1.10);
        face(camera, x0, y1, z0, x1, y1, z0,
                x1, y0, z0, x0, y0, z0, p.cor, 0.58);
    }

    private void face(Camera camera,
            double ax, double ay, double az,
            double bx, double by, double bz,
            double cx, double cy, double cz,
            double dx, double dy, double dz,
            int cor, double orientacao) {
        Vertice a = camera.transformar(ax, ay, az);
        Vertice b = camera.transformar(bx, by, bz);
        Vertice c = camera.transformar(cx, cy, cz);
        Vertice d = camera.transformar(dx, dy, dz);
        // Uma face deve manter a mesma iluminação em seus dois triângulos;
        // calcular o brilho por triângulo criaria uma diagonal artificial.
        double mediaZ = Math.max(PLANO_PROXIMO,
                (a.z + b.z + c.z + d.z) / 4.0);
        int corSombreada = sombrear(cor, orientacao * brilho(mediaZ));
        desenharTrianguloRecortado(a, b, c, corSombreada);
        desenharTrianguloRecortado(a, c, d, corSombreada);
    }

    /**
     * Recorta o triângulo no plano próximo antes de projetar os vértices.
     * Sem esse recorte, um vértice atrás da câmera pode produzir um triângulo
     * gigante quando olhamos para baixo perto de um objeto.
     */
    private void desenharTrianguloRecortado(Vertice a, Vertice b,
            Vertice c, int cor) {
        entradaRecorte[0] = a;
        entradaRecorte[1] = b;
        entradaRecorte[2] = c;
        Vertice[] entrada = entradaRecorte;
        Vertice[] recortados = saidaRecorte;
        int contagem = 0;
        Vertice anterior = entrada[2];
        boolean anteriorVisivel = anterior.z >= PLANO_PROXIMO;
        for (Vertice atual : entrada) {
            boolean atualVisivel = atual.z >= PLANO_PROXIMO;
            if (atualVisivel != anteriorVisivel) {
                double t = (PLANO_PROXIMO - anterior.z) / (atual.z - anterior.z);
                recortados[contagem++] = new Vertice(
                        anterior.x + (atual.x - anterior.x) * t,
                        anterior.y + (atual.y - anterior.y) * t,
                        PLANO_PROXIMO);
            }
            if (atualVisivel) recortados[contagem++] = atual;
            anterior = atual;
            anteriorVisivel = atualVisivel;
        }
        if (contagem < 3) return;

        for (int i = 1; i < contagem - 1; i++) {
            rasterizarTriangulo(recortados[0], recortados[i],
                    recortados[i + 1], cor);
        }
    }

    /** Usa 1/z interpolado para o teste de profundidade por pixel. */
    private void rasterizarTriangulo(Vertice a, Vertice b, Vertice c, int cor) {
        double ax = larguraRender / 2.0 + focal * a.x / a.z;
        double ay = alturaRender / 2.0 - focal * a.y / a.z;
        double bx = larguraRender / 2.0 + focal * b.x / b.z;
        double by = alturaRender / 2.0 - focal * b.y / b.z;
        double cx = larguraRender / 2.0 + focal * c.x / c.z;
        double cy = alturaRender / 2.0 - focal * c.y / c.z;
        double area = (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
        if (Math.abs(area) < 1e-9) return;

        int esquerda = Math.max(0, (int) Math.floor(Math.min(ax, Math.min(bx, cx))));
        int direita = Math.min(larguraRender - 1, (int) Math.ceil(Math.max(ax, Math.max(bx, cx))));
        int topo = Math.max(0, (int) Math.floor(Math.min(ay, Math.min(by, cy))));
        int base = Math.min(alturaRender - 1, (int) Math.ceil(Math.max(ay, Math.max(by, cy))));
        if (direita < esquerda || base < topo) return;

        double invA = 1.0 / a.z, invB = 1.0 / b.z, invC = 1.0 / c.z;
        double invArea = 1.0 / area;
        double passoAX = (by - cy) * invArea;
        double passoAY = (cx - bx) * invArea;
        double passoBX = (cy - ay) * invArea;
        double passoBY = (ax - cx) * invArea;
        double pxInicial = esquerda + 0.5;
        double pyInicial = topo + 0.5;
        double pesoARow = ((bx - pxInicial) * (cy - pyInicial)
                - (by - pyInicial) * (cx - pxInicial)) * invArea;
        double pesoBRow = ((cx - pxInicial) * (ay - pyInicial)
                - (cy - pyInicial) * (ax - pxInicial)) * invArea;

        // Os pesos baricentricos sao funcoes lineares: em vez de dividir e
        // multiplicar varias vezes por pixel, incrementamos os pesos em X/Y.
        for (int y = topo; y <= base; y++) {
            int linha = y * larguraRender;
            double wA = pesoARow;
            double wB = pesoBRow;
            for (int x = esquerda; x <= direita; x++) {
                double wC = 1.0 - wA - wB;
                if (wA >= -1e-9 && wB >= -1e-9 && wC >= -1e-9) {
                    float profundidade = (float) (wA * invA + wB * invB + wC * invC);
                    int indice = linha + x;
                    if (profundidade > profundidades[indice] + 1e-6f) {
                        profundidades[indice] = profundidade;
                        pixels[indice] = cor;
                    }
                }
                wA += passoAX;
                wB += passoBX;
            }
            pesoARow += passoAY;
            pesoBRow += passoBY;
        }
    }

    private static final class Camera {
        final double x, y, cosYaw, senYaw, cosPitch, senPitch;

        Camera(Jogador jogador) {
            x = jogador.getX();
            y = jogador.getY();
            cosYaw = Math.cos(jogador.getAngulo());
            senYaw = Math.sin(jogador.getAngulo());
            cosPitch = Math.cos(jogador.getInclinacaoCamera());
            senPitch = Math.sin(jogador.getInclinacaoCamera());
        }

        Vertice transformar(double x, double y, double z) {
            double dx = x - this.x;
            double dy = y - this.y;
            double dz = z - OLHOS_Z;
            double frente = dx * cosYaw + dy * senYaw;
            return new Vertice(-dx * senYaw + dy * cosYaw,
                    -frente * senPitch + dz * cosPitch,
                    frente * cosPitch + dz * senPitch);
        }
    }

    private static final class Vertice {
        final double x, y, z;
        Vertice(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static double brilho(double distancia) {
        return Math.max(0.30, Math.min(1, 1.0 / (1.0 + distancia * 0.085)));
    }

    private static int sombrear(int cor, double fator) {
        int r = Math.min(255, Math.max(0, (int) (((cor >>> 16) & 255) * fator)));
        int g = Math.min(255, Math.max(0, (int) (((cor >>> 8) & 255) * fator)));
        int b = Math.min(255, Math.max(0, (int) ((cor & 255) * fator)));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /** Recalcula a geometria apenas quando um objeto ou seu estado muda. */
    private List<Peca> obterPecas(MapaJogo mapa) {
        long versao = 1469598103934665603L;
        for (ObjetoMapa objeto : mapa.getObjetos()) {
            versao = 31 * versao + objeto.getTipo().ordinal();
            versao = 31 * versao + objeto.getX();
            versao = 31 * versao + objeto.getY();
            versao = 31 * versao + objeto.getRotacao();
            versao = 31 * versao + Double.doubleToLongBits(objeto.getLargura());
            versao = 31 * versao + Double.doubleToLongBits(objeto.getProfundidade());
            versao = 31 * versao + Double.doubleToLongBits(objeto.getAlturaRelativa());
            versao = 31 * versao + (objeto.isAberto() ? 1 : 0);
            versao = 31 * versao + (objeto.isOcupado() ? 1 : 0);
        }
        if (pecasCache == null || mapaCacheObjetos != mapa || versaoCacheObjetos != versao) {
            pecasCache = construirPecas(mapa);
            mapaCacheObjetos = mapa;
            versaoCacheObjetos = versao;
        }
        return pecasCache;
    }

    /** Constrói caixas sólidas fechadas: topo, base e laterais são visíveis. */
    private List<Peca> construirPecas(MapaJogo mapa) {
        List<Peca> pecas = new ArrayList<>();
        for (ObjetoMapa o : mapa.getObjetos()) {
            double w = o.getLargura();
            double d = o.getProfundidade();
            double h = o.getAlturaRelativa();
            double borda = Math.min(0.055, Math.min(w, d) * 0.14);

            switch (o.getTipo()) {
                case MESA:
                    // Tampo fechado e quatro pés sólidos. O vão abaixo é real.
                    adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, d / 2,
                            h * 0.82, h, MADEIRA_CLARA);
                    for (int sx = -1; sx <= 1; sx += 2) {
                        for (int sy = -1; sy <= 1; sy += 2) {
                            double x = sx < 0 ? -w / 2 : w / 2 - borda;
                            double y = sy < 0 ? -d / 2 : d / 2 - borda;
                            adicionarPeca(pecas, o, x, y, x + borda, y + borda,
                                    0, h * 0.82, MADEIRA);
                        }
                    }
                    break;
                case ESTANTE:
                    adicionarPeca(pecas, o, -w / 2, -d / 2, -w / 2 + borda, d / 2,
                            0, h, MADEIRA_ESCURO);
                    adicionarPeca(pecas, o, w / 2 - borda, -d / 2, w / 2, d / 2,
                            0, h, MADEIRA_ESCURO);
                    adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, -d / 2 + borda,
                            0, h, VERDE);
                    adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, d / 2,
                            h - borda, h, MADEIRA);
                    for (int i = 0; i < 3; i++) {
                        double z = h * (0.18 + i * 0.25);
                        adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, d / 2,
                                z, z + borda * 0.65, MADEIRA_CLARA);
                    }
                    break;
                case ARMARIO:
                    construirArmario(pecas, o, w, d, h, borda);
                    break;
                case CAMA:
                    // Estrado e colchão com superfície superior fechada.
                    adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, d / 2,
                            h * 0.19, h * 0.38, MADEIRA);
                    adicionarPeca(pecas, o, -w / 2 + 0.015, -d / 2 + 0.015,
                            w / 2 - 0.015, d / 2 - 0.015,
                            h * 0.38, h * 0.82, COLCHAO);
                    adicionarPeca(pecas, o, -w / 2 + 0.022, d * 0.06,
                            w / 2 - 0.022, d / 2 - 0.025,
                            h * 0.82, h * 0.86, COBERTOR);
                    adicionarPeca(pecas, o, -w / 2 + 0.09, -d / 2 + 0.08,
                            w / 2 - 0.09, -d / 2 + d * 0.25,
                            h * 0.82, h, TRAVESSEIRO);
                    adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, -d / 2 + borda,
                            0, h * 1.38, MADEIRA_ESCURO);
                    break;
                default:
                    break;
            }
        }
        return pecas;
    }

    private void construirArmario(List<Peca> pecas, ObjetoMapa o,
            double w, double d, double h, double borda) {
        double frente = d / 2;
        // Piso, teto e paredes laterais de verdade; vistos também de dentro.
        adicionarPeca(pecas, o, -w / 2, -d / 2, -w / 2 + borda, d / 2,
                0, h, MADEIRA);
        adicionarPeca(pecas, o, w / 2 - borda, -d / 2, w / 2, d / 2,
                0, h, MADEIRA);
        adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, -d / 2 + borda,
                0, h, MADEIRA_INTERIOR);
        adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, d / 2,
                0, borda, MADEIRA_ESCURO);
        adicionarPeca(pecas, o, -w / 2, -d / 2, w / 2, d / 2,
                h - borda, h, MADEIRA_ESCURO);

        if (o.isOcupado()) {
            // Portas quase fechadas: o personagem está realmente no centro
            // do armário e observa o cenário pela abertura central.
            double fresta = 0.075;
            adicionarPeca(pecas, o, -w / 2, frente - borda / 3,
                    -fresta / 2, frente + borda / 3,
                    0, h - borda / 2, MADEIRA_CLARA);
            adicionarPeca(pecas, o, fresta / 2, frente - borda / 3,
                    w / 2, frente + borda / 3,
                    0, h - borda / 2, MADEIRA_CLARA);
        } else if (o.isAberto()) {
            // A porta gira para fora, ao lado do vão livre.
            adicionarPeca(pecas, o, -w / 2, frente - borda / 2,
                    -w / 2 + borda / 2, frente + w * 0.48,
                    0, h - borda / 2, MADEIRA_CLARA);
            adicionarPeca(pecas, o, w / 2 - borda / 2, frente - borda / 2,
                    w / 2, frente + w * 0.48,
                    0, h - borda / 2, MADEIRA_CLARA);
        } else {
            adicionarPeca(pecas, o, -w / 2, frente - borda / 3,
                    -0.005, frente + borda / 3,
                    0, h - borda / 2, MADEIRA_CLARA);
            adicionarPeca(pecas, o, 0.005, frente - borda / 3,
                    w / 2, frente + borda / 3,
                    0, h - borda / 2, MADEIRA_CLARA);
        }
    }

    /** Transformação local para o mapa; conserva rotações de 0,90,180,270. */
    private void adicionarPeca(List<Peca> pecas, ObjetoMapa obj,
            double x0, double y0, double x1, double y1,
            double z0, double z1, int cor) {
        int rot = obj.getRotacao() / 90;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (double x : new double[]{x0, x1}) {
            for (double y : new double[]{y0, y1}) {
                double xx;
                double yy;
                switch (rot) {
                    case 1: xx = -y; yy = x; break;
                    case 2: xx = -x; yy = -y; break;
                    case 3: xx = y; yy = -x; break;
                    default: xx = x; yy = y; break;
                }
                xx += obj.getCentroX();
                yy += obj.getCentroY();
                minX = Math.min(minX, xx);
                minY = Math.min(minY, yy);
                maxX = Math.max(maxX, xx);
                maxY = Math.max(maxY, yy);
            }
        }
        pecas.add(new Peca(minX, minY, maxX, maxY, z0, z1, cor));
    }

    private void desenharHud(Jogador jogador, ObjetoMapa alvo, String mensagem) {
        gc.setFill(Color.rgb(10, 13, 19, 0.60));
        gc.fillRect(0, 0, LARGURA_TELA, 43);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 15));
        gc.fillText("WASD/setas mover   Mouse olhar   E interagir   F fechar   F3 qualidade   Esc soltar", 12, 26);

        if (jogador.isEscondido()) {
            textoCentral("DENTRO DO ARMÁRIO — E para sair", ALTURA_TELA - 38);
        } else if (alvo != null) {
            String instrucao;
            switch (alvo.getTipo()) {
                case ARMARIO:
                    instrucao = alvo.isAberto()
                            ? "E entrar no armário    F fechar" : "E abrir armário";
                    break;
                case ESTANTE:
                    instrucao = "E examinar estante";
                    break;
                case CAMA:
                    instrucao = "E examinar cama";
                    break;
                default:
                    instrucao = "E examinar mesa";
                    break;
            }
            textoCentral(instrucao, ALTURA_TELA - 78);
        }

        if (mensagem != null && !mensagem.isEmpty() && !jogador.isEscondido()) {
            textoCentral(mensagem, ALTURA_TELA - 37);
        }
        gc.setFill(Color.rgb(230, 233, 238, 0.8));
        gc.setFont(Font.font("Arial", 13));
        gc.fillText("F3: qualidade " + NOMES_QUALIDADE[indiceQualidade]
                + "   FPS: " + (int) Math.round(fpsAtual),
                LARGURA_TELA - 234, 60);
        gc.setFill(Color.rgb(240, 239, 228));
        gc.fillOval(LARGURA_TELA / 2.0 - 2, ALTURA_TELA / 2.0 - 2, 4, 4);
    }

    private void textoCentral(String texto, double y) {
        gc.setFont(Font.font("Arial", 16));
        double larguraTexto = texto.length() * 8.0;
        gc.setFill(Color.rgb(11, 13, 16, 0.78));
        gc.fillRoundRect((LARGURA_TELA - larguraTexto) / 2 - 12, y - 23,
                larguraTexto + 24, 33, 9, 9);
        gc.setFill(Color.rgb(235, 228, 200));
        gc.fillText(texto, (LARGURA_TELA - larguraTexto) / 2, y);
    }

    private static final class Peca {
        final double minX, minY, maxX, maxY, zMin, zMax;
        final int cor;

        Peca(double minX, double minY, double maxX, double maxY,
                double zMin, double zMax, int cor) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.zMin = zMin;
            this.zMax = zMax;
            this.cor = cor;
        }
    }

}
