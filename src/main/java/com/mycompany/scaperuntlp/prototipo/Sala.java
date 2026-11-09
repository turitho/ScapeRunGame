package com.mycompany.scaperuntlp.prototipo;
import com.mycompany.scaperuntlp.controller.JogoController; import com.mycompany.scaperuntlp.view.JogoView; import javafx.application.Application; import javafx.scene.Scene; import javafx.stage.Stage;
public class Sala extends Application {
    
 @Override public void start(Stage stage){JogoView view=new JogoView();Scene cena=new Scene(view,960,640);JogoController controller=new JogoController(view);stage.setTitle("ScapeRun - Protótipo MVC 3D");stage.setScene(cena);stage.setResizable(false);stage.show();view.requestFocus();controller.iniciar(cena);}
 public static void main(String[] args){launch(args);}
}
