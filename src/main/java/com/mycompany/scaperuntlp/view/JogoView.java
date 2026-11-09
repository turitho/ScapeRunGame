package com.mycompany.scaperuntlp.view;
import com.mycompany.scaperuntlp.model.*; import javafx.scene.canvas.*; import javafx.scene.layout.*; import javafx.scene.paint.Color; import javafx.scene.text.*;
public class JogoView extends StackPane {
 private final Canvas canvas=new Canvas(960,640); private final GraphicsContext gc=canvas.getGraphicsContext2D();
 public JogoView(){getChildren().add(canvas);setStyle("-fx-background-color: black;");}
 public void desenhar(Jogador j,MapaJogo mapa){double w=canvas.getWidth(),h=canvas.getHeight(),horizonte=h/2;
  if(j.isEscondido()){gc.setFill(Color.rgb(12,10,9));gc.fillRect(0,0,w,h);gc.setFill(Color.BEIGE);gc.setFont(Font.font(22));gc.fillText("ESCONDIDO NO ARMÁRIO — pressione E para sair",w/2-230,h/2);return;}
  gc.setFill(Color.rgb(28,29,36));gc.fillRect(0,0,w,horizonte);gc.setFill(Color.rgb(75,67,58));gc.fillRect(0,horizonte,w,h-horizonte);
  double fov=Math.toRadians(60);
  for(int col=0;col<(int)w;col++){double cam=2.0*col/w-1,ang=j.getAngulo()+Math.atan(cam*Math.tan(fov/2)),rx=Math.cos(ang),ry=Math.sin(ang),dist=0;Color cor=Color.GRAY;
   while(dist<35){dist+=.025;double tx=j.getX()+rx*dist,ty=j.getY()+ry*dist;if(mapa.parede(tx,ty)){cor=Color.rgb(115,118,125);break;}ObjetoMapa o=mapa.objetoEm(tx,ty);if(o!=null&&o.isSolido()){cor=corObjeto(o);break;}}
   double corrigida=Math.max(.05,dist*Math.cos(ang-j.getAngulo())),ph=h/corrigida,brilho=Math.max(.18,Math.min(1,1/(1+corrigida*.10)));gc.setFill(cor.deriveColor(0,1,brilho,1));gc.fillRect(col,horizonte-ph/2,1,ph);
  }
  gc.setFill(Color.WHITE);gc.setFont(Font.font("Arial",16));gc.fillText("W/S: mover   A/D ou ←/→: girar   E: abrir/entrar no armário",18,28);
  gc.setFill(Color.color(1,1,1,.8));gc.fillOval(w/2-2,horizonte-2,4,4);
 }
 private Color corObjeto(ObjetoMapa o){switch(o.getTipo()){case MESA:return Color.SADDLEBROWN;case ESTANTE:return Color.DARKGREEN;case ARMARIO:return o.isAberto()?Color.SANDYBROWN:Color.DARKRED;default:return Color.GRAY;}}
}
