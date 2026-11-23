# Otimização de desempenho — ScapeRunGame

**Base:** ScapeRunGame-correcao-projecao-camera.zip, mantendo o MVC e os controles.

## Como executar e testar

Abra a pasta `ScapeRunGame` no NetBeans e execute normalmente.

- **F3:** alterna qualidade: MEDIA (768x512), ALTA (960x640), RAPIDA (576x384), MEDIA...
- A **janela continua sempre 960x640**. A imagem do jogo é ampliada; os textos/HUD permanecem em resolução integral.
- O indicador na parte superior direita mostra a qualidade e a estimativa de FPS do ciclo de jogo. Esta estimativa considera o ciclo de atualização, não é um medidor de FPS da GPU.
- Controles preservados: mouse horizontal/vertical; WASD e setas movimentam (incluindo diagonais); E e F interagem com armários; Esc libera o mouse.

## Otimizações realizadas no JogoView

1. **Resolução interna ajustável:** resolução média padrão com ~36% menos pixels que 960x640. Modo rápido com ~64% menos pixels. Para maior nitidez, use F3 para qualidade alta.
2. **Chão por varredura incremental:** os passos por linha e as cores sombreadas são calculados uma vez por linha, em vez de repetir as operações para cada pixel.
3. **Rasterização incremental:** os pesos baricêntricos dos triângulos passam a ser incrementados ao longo das linhas, reduzindo divisões e operações por pixel.
4. **Cache da geometria dos móveis:** peças são reutilizadas entre quadros e reconstruídas somente quando propriedades relevantes mudam (altura, largura, profundidade, orientação, porta aberta, ocupação, etc.).
5. **Cache das faces de parede:** os limites do mapa são examinados uma vez para gerar as faces. Por quadro são testadas apenas as faces armazenadas.
6. **Culling conservador:** paredes fora do campo de visão e faces vistas pelo lado oposto são descartadas antes de rasterizar.
7. **Reutilização de buffers de recorte:** menos alocações temporárias por triângulo.
8. **Evita raízes quadradas:** usa distâncias ao quadrado para excluir móveis e paredes fora de alcance.
9. **Hud simples para medição:** mostra a qualidade e uma estimativa de FPS para comparar os resultados no equipamento real.

## Resultados locais (não são FPS reais)

Teste em CPU usando substitutos JavaFX, 30 quadros por cenário, com aquecimento:

| Cenário | Antes 960x640 | Depois MEDIA 768x512 | Depois ALTA 960x640 |
| --- | ---: | ---: | ---: |
| Spawn | 7,41 ms | 1,62 ms | 2,92 ms |
| Mesa próxima | 7,16 ms | 2,30 ms | 3,20 ms |
| Mesa olhando para baixo | 7,98 ms | 1,89 ms | 3,13 ms |
| Armário próximo | 24,62 ms | 7,60 ms | 10,12 ms |

Os números servem como comparação de custo de CPU neste ambiente de teste, não como promessa de FPS no Linux/NetBeans. O JavaFX real, GPU, driver e hardware podem produzir resultados diferentes.

## Regressão visual e testes

A versão otimizada em qualidade ALTA foi comparada pixel a pixel com a original em quatro cenários (mesa, câmera inclinada, armário, dentro do armário) e os quadros coincidiram exatamente. Mais 32 posições aleatórias foram testadas; 29 coincidiram integralmente e três apresentaram apenas um pixel diferente em cada cena (arredondamento numérico).

`TesteOtimizacao.java` valida reutilização dos caches, reconstrução após abrir armário e alternância das três qualidades. Os testes anteriores de movimento, armário e colisão continuam incluídos.

## Outras otimizações pesquisadas (não implementadas)

- **JavaFX PixelBuffer:** permite compartilhar um `IntBuffer` com `WritableImage`, potencialmente diminuindo cópias de pixels. Requer uma avaliação prática no JavaFX 13 e no sistema operacional alvo antes de substituir o PixelWriter. https://openjfx.io/javadoc/13/javafx.graphics/javafx/scene/image/PixelBuffer.html
- **Java Flight Recorder (JFR):** ajuda a identificar onde a CPU realmente gasta tempo, além de alocações e pausas de GC. https://docs.oracle.com/en/java/javase/11/troubleshoot/troubleshoot-performance-issues-using-jfr.html
- **Descarte por setores/portais:** se o mapa crescer e ganhar muitos cômodos, desenhar apenas a região visível da câmera.
- **Pré-computar transformações ou agrupar faces por material:** pode ajudar caso sejam adicionadas centenas de móveis.
- **Pipeline 3D acelerado ou OpenGL:** se o jogo crescer substancialmente em número de polígonos, um motor gráfico com GPU pode oferecer mais margem, mas isso seria uma alteração arquitetural maior e não foi feito aqui.

## Como diagnosticar lentidão no computador

1. Pressione F3 e compare os FPS em MEDIA e RAPIDA, de preferência olhando para o mesmo móvel, parado e depois se movimentando.
2. Se RAPIDA ficar muito mais fluido, o gargalo provavelmente está relacionado à quantidade de pixels e ao rasterizador.
3. Se a diferença entre as qualidades for pequena, pode haver outros gargalos (JavaFX, GPU/driver, mouse/Robot, GC, janela/compositor).
4. Execute sem outros programas pesados e use JFR ou um profiler para identificar métodos que consomem mais CPU.
5. Veja os resultados em situações distintas: corredor vazio, próximo a mesa, armário aberto, dentro do armário e olhando para baixo.

**Nota:** não foi possível executar a interface gráfica real do JavaFX neste ambiente; as compilações e verificações locais usaram Java 11 com stubs JavaFX e testes do código de rendering. A versão final precisa ser testada pelo NetBeans no computador do usuário.
