# Correção da renderização da altura dos objetos

## Problema

Antes, cada raio parava no primeiro móvel encontrado. A View desenhava somente esse móvel e não continuava o desenho do cenário atrás dele. Por isso, a área acima de um móvel baixo parecia opaca, em vez de revelar o que estava atrás.

## Solução

A classe `view/JogoView.java` foi ajustada para:

1. Percorrer o raio até a parede, registrando os móveis encontrados no caminho.
2. Desenhar a parede como fundo da coluna.
3. Desenhar os móveis do mais distante para o mais próximo.
4. Limitar cada móvel à sua altura projetada (`getAlturaRelativa()`).

Assim, a parte que não é ocupada pelo móvel não recebe uma cor opaca do próprio móvel: ela mantém visível o cenário ou outro objeto que esteja atrás.

## Onde alterar

A lógica está no método `desenharColuna(...)` de `JogoView.java`. A altura de cada tipo continua sendo definida em `model/TipoObjeto.java` e consultada por `ObjetoMapa.getAlturaRelativa()`.

## Observação

A colisão continua sendo baseada na célula ocupada pelo móvel. Esta alteração afeta somente a renderização visual, não a área de colisão.
