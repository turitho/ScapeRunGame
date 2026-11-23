# ScapeRunGame — armários e renderização corrigidos

**Base:** `ScapeRunGame-Mecanicas-Melhoradas.zip`, criada na conversa de 23/11/2026.  
**Organização:** foram preservados os pacotes existentes de Model, View e Controller; não houve refatoração da arquitetura MVC.

## Executar

Abra a pasta `ScapeRunGame` no NetBeans como projeto Maven e execute o projeto (classe principal `com.mycompany.scaperuntlp.prototipo.Sala`).

```bash
mvn clean javafx:run
```

## Controles

| Tecla | Ação |
|---|---|
| W / S ou ↑ / ↓ | Andar para frente / trás |
| A / D ou ← / → | Mover lateralmente para esquerda / direita |
| W+A, W+D, S+A, S+D (ou setas equivalentes) | Mover na diagonal, mantendo a velocidade |
| Movimento do mouse | Girar a câmera horizontalmente e olhar para cima / baixo |
| Clique esquerdo | Capturar o mouse |
| Esc | Liberar o mouse |
| E | Abrir armário, entrar, sair ou examinar um móvel |
| F | Fechar armário aberto enquanto estiver fora |

O teclado não gira a câmera. A direção e a inclinação são controladas pelo mouse. O HUD mostra somente os controles e a interação disponível. **Não existem mais indicadores, consumo ou recuperação de energia, nem inventário ou coleta de itens.** Estantes, mesas e camas podem ser examinadas, mas não entregam itens e não recuperam energia.

## Armários: posição real do personagem

O `Model/Jogador.java` agora realmente move as coordenadas do jogador ao entrar:

1. Aproximar-se **pela frente** do armário; E abre as portas.
2. E novamente guarda a posição e o ângulo de fora, marca o armário como ocupado, **move o jogador para o centro interno** e o orienta para a porta.
3. A `JogoView` renderiza o interior com as portas quase fechadas e uma fresta central, como geometria do mundo. Não são mais duas faixas sobrepostas artificialmente à tela.
4. Enquanto escondido, o movimento fica desativado para evitar atravessar os painéis físicos.
5. E verifica a posição salva de saída, desocupa o móvel e retorna para o mesmo lugar e orientação anterior.
6. F fecha o armário quando ele está aberto e vazio.

O armário continua sendo sólido para colisões durante o jogo. Entrar nele é uma ação explícita, não uma caminhada através da parede. A posição de dentro é uma exceção controlada que só é usada enquanto o jogador está escondido.

## Móveis: correção dos buracos e alturas

`view/JogoView.java` foi atualizado para projetar peças de geometria em três dimensões: retângulos sólidos com **laterais, tampa e base**. Anteriormente, cada peça projetava somente faces verticais, produzindo buracos e distorções ao ver uma mesa ou cama de cima. Agora o cálculo encontra também o ponto em que o raio encontra a superfície horizontal. Um **buffer de profundidade por coluna e linha** determina qual superfície fica visível. O tampo de uma mesa e a parte superior de uma cama são superfícies fechadas; os vãos entre pernas de mesa ou prateleiras da estante continuam vazios de propósito.

A câmera está a `0.64` unidade acima do piso; o teto das paredes está em `1.00`. As alturas foram reduzidas:

| Tipo | Exemplo de altura no mapa |
|---|---:|
| Mesa | 0.31 a 0.36 |
| Estante | 0.68 a 0.78 |
| Armário | 0.82 a 0.87 |
| Cama | 0.21 a 0.24 |

A cabeceira da cama é um pouco mais alta que o colchão. É possível ajustar cada instância individualmente com `setAlturaRelativa`, `setDimensoes` e `setRotacao`.

## Colisões

Mantido o teste de colisão entre o círculo do jogador e os retângulos de paredes e móveis, com subpassos para movimentos rápidos e deslizamento nos eixos X/Y. Ao sair do armário a posição que foi guardada antes da entrada é verificada contra a colisão, evitando sair dentro de outro objeto.

## MVC

- **Model:** `Jogador`, `ObjetoMapa`, `TipoObjeto`, `MapaJogo` — posições, estado dos armários, dimensões e colisões.
- **Controller:** `InputController`, `JogadorController`, `JogoController` — teclado, movimento, ações e game loop.
- **View:** `JogoView` — imagem por pixel, superfícies dos móveis, cena 3D e HUD.

As classes legadas `prototipo/Renderizar.java` e `model/Item.java` continuam no repositório como arquivos antigos/vazios, mas não participam da partida atual.

## Validação

Os testes `src/test/java/com/mycompany/scaperuntlp/TesteMecanicas.java` e `TesteCameraEMovimento.java` verificam movimentos, diagonais, ângulo e inclinação, além de armários, posição interna real, saída, colisão, dimensões e chamada à View. Testado nesta sessão com Java 11, usando **classes auxiliares de JavaFX simuladas** para compilar e executar as regras e a rasterização. Também foram gerados quadros de teste (mesa, cama, armário fechado, aberto e visto de dentro) para revisar o resultado visual.

O ambiente desta análise **não possui Maven/JavaFX reais**, então é necessário fazer um teste de compilação gráfica no NetBeans. O resultado simulado não garante que não haverá diferenças no JavaFX real.
