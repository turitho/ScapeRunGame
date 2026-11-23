# Correção da distorção da câmera — ScapeRunGame

## Problema observado

Ao olhar para baixo e se aproximar de móveis ou paredes, algumas faces eram desenhadas como triângulos grandes, parecendo ser puxadas para o centro ou para as bordas da tela.

O renderizador anterior alterava artificialmente a posição do horizonte conforme a inclinação da câmera e projetava algumas faces dividindo por distâncias muito pequenas. Perto dos móveis, isso podia gerar projeções exageradas.

## Alteração aplicada

Apenas a classe `src/main/java/com/mycompany/scaperuntlp/view/JogoView.java` foi modificada. A estrutura MVC, os objetos, a interação com armários e os controles permanecem iguais.

A correção utiliza:

1. **Transformação 3D única** para paredes, móveis e câmera (`yaw` e `pitch`), em vez de deslocar artificialmente a linha do horizonte.
2. **Plano de recorte próximo** de 0,12 unidade: faces parcialmente atrás/muito perto da câmera são recortadas *antes* da perspectiva, evitando vértices projetados ao infinito.
3. **Rasterização de triângulos com z-buffer** por pixel: a peça mais próxima cobre a mais distante sem criar faces esticadas ou alterar a colisão.
4. **Plano do chão com raios 3D**: o chão também acompanha corretamente a inclinação da câmera.
5. **Iluminação uniforme por face**: os dois triângulos de um tampo ou de uma parede não produzem uma divisão diagonal de cor.

## Como testar no NetBeans

1. Abra o projeto Maven pela pasta `ScapeRunGame`.
2. Execute o projeto normalmente.
3. Clique para capturar o mouse.
4. Aproxime-se de uma mesa, cama, estante e parede e olhe para baixo e para cima.
5. Verifique se as faces permanecem fechadas e se não aparecem triângulos puxados até o horizonte.
6. Teste W+A/W+D e o armário com E/F; estas mecânicas não foram alteradas.

## Verificações realizadas

O arquivo `JogoView.java` e as classes do Model foram compilados com Java 11 usando substitutos locais para as classes JavaFX. Foram gerados e inspecionados quadros em sete situações de câmera e proximidade, incluindo pitch de -50°. O projeto completo ainda precisa ser testado com a distribuição real de JavaFX no NetBeans.
