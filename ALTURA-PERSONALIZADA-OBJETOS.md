# Altura personalizada dos objetos

A alteração foi aplicada sobre o `ScapeRunGame.zip` enviado nesta conversa.

## Como a altura funciona

A altura é relativa à altura de uma parede:

- `0.25`: 25% da altura de uma parede.
- `0.50`: metade da altura de uma parede.
- `1.00`: mesma altura de uma parede.
- `1.20`: 20% mais alto que uma parede.

O intervalo permitido é de `0.05` até `2.00`.

## Definir a altura ao criar o objeto

```java
MapaJogo mapa = new MapaJogo();
mapa.adicionar(TipoObjeto.MESA, 9, 7, 0.55);
mapa.adicionar(TipoObjeto.ESTANTE, 10, 7, 1.20);
mapa.adicionar(TipoObjeto.CAMA, 11, 7, 0.30);
```

## Alterar a altura de um objeto que já existe

```java
ObjetoMapa mesa = mapa.objetoEm(9, 7);

if (mesa != null) {
    mesa.setAlturaRelativa(0.75);
}
```

A `JogoView` já consulta `getAlturaRelativa()` durante o desenho de cada frame.
Portanto, ao alterar a altura no Model, a próxima renderização usa o novo valor.

## Onde cada responsabilidade fica no MVC

- **Model — `ObjetoMapa`:** armazena e valida a altura individual.
- **Model — `TipoObjeto`:** define a altura padrão de cada tipo.
- **Model — `MapaJogo`:** oferece métodos para adicionar objetos com altura padrão ou personalizada.
- **View — `JogoView`:** converte a altura relativa em altura de pixels na projeção 3D.

A altura visual não altera a colisão: o objeto continua ocupando a célula inteira do mapa.
