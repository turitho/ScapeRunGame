# Controle de câmera livre e movimentação diagonal

Esta versão parte de `ScapeRunGame-controle-mouse.zip` e mantém as classes nas pastas MVC originais.

## Comandos

- **Clique esquerdo na janela:** capturar o mouse e esconder o cursor.
- **Mouse para esquerda/direita:** gira a câmera no eixo horizontal (yaw).
- **Mouse para cima/baixo:** inclina a câmera no eixo vertical (pitch), limitado a ±50°.
- **W / ↑:** andar para frente, na direção horizontal em que o jogador está olhando.
- **S / ↓:** andar para trás.
- **A / ←:** andar para a esquerda (strafe).
- **D / →:** andar para a direita (strafe).
- **W+D, W+A, S+D ou S+A:** movimento diagonal sem aumento de velocidade.
- **E:** interagir; **F:** fechar armário; **Esc:** liberar o cursor.

O teclado não gira mais a câmera. Só o mouse altera os ângulos de visão durante a exploração.

## Arquivos alterados

- `controller/InputController.java`: acumula deslocamentos X e Y do mouse.
- `controller/JogoController.java`: aplica sensibilidade horizontal e vertical aos ângulos do jogador.
- `model/Jogador.java`: adiciona inclinação vertical limitada; salva e restaura o ângulo ao entrar/sair do armário.
- `controller/JogadorController.java`: converte os comandos para dois eixos de deslocamento relativo à câmera e normaliza a diagonal; preserva colisões.
- `view/JogoView.java`: recalcula o horizonte, as paredes, os móveis e o fundo com o ângulo de inclinação. Atualiza o HUD.

## Sensibilidade

No `JogoController.java`, altere `SENSIBILIDADE_MOUSE_HORIZONTAL` e `SENSIBILIDADE_MOUSE_VERTICAL`. Menor número = movimento mais lento.

## Nota técnica

A inclinação é implementada por deslocamento da linha do horizonte (pitch em um renderizador raycasting de projeção vertical simplificada); não é um motor de polígonos 3D completo. Em certos ambientes Linux/Wayland a recentralização do cursor com `Robot` pode ser limitada. O comportamento precisa ser conferido no JavaFX real na máquina do usuário.
