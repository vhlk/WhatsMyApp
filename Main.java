import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.*;


public class Main extends Application{
    Scene initial, appScene;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Stage window = primaryStage;
        window.setTitle("WhatsMyApp");

        //Layout initial
        Button start = new Button("START APP");
        start.setOnAction(event -> window.setScene(appScene));
        HBox layoutOpen = new HBox(1000);
        layoutOpen.setStyle("-fx-background-color: #336699;");
        layoutOpen.getChildren().add(start);
        layoutOpen.setAlignment(Pos.CENTER);
        initial = new Scene(layoutOpen,670,500);

        //Layout app
        BorderPane layoutApp = new BorderPane();

        //Criando Middle
        ListView chat = new ListView();
        chat.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        //Criando Lateral Esquerda
        VBox leftLayout = new VBox(1000);
        leftLayout.setAlignment(Pos.CENTER_LEFT);
        Label labelLeft = new Label("  WhatsMyApp  ");
        labelLeft.setStyle("-fx-font-size:20px;");
        labelLeft.setStyle("-fx-text-fill: white;");
        leftLayout.setStyle("-fx-background-color: #336699;");
        leftLayout.getChildren().add(labelLeft);

        //Criando Lateral Direita
        VBox rigthLayout = new VBox(1000);
        rigthLayout.setAlignment(Pos.CENTER_RIGHT);
        Label labelRigth = new Label("  WhatsMyApp  ");
        labelRigth.setStyle("-fx-font-size:20px;");
        labelRigth.setStyle("-fx-text-fill: white;");
        rigthLayout.setStyle("-fx-background-color: #336699;");
        rigthLayout.getChildren().add(labelRigth);

        //Criando Botton
        GridPane bottonLayout = new GridPane();
        bottonLayout.setAlignment(Pos.BOTTOM_CENTER);
        bottonLayout.setStyle("-fx-background-color: #336699;");
        TextField input = new TextField();
        Button send = new Button("SEND MESSAGE");
        send.setOnAction(event -> {                                            //Enviar Mensagem
            String message = input.getCharacters().toString();
            if(!message.equals("")){
                chat.getItems().add(message);
                input.clear();
            }
        });
        send.setMinWidth(500);
        GridPane.setConstraints(send,0,1);
        input.setMinWidth(500);
        GridPane.setConstraints(input,0,0);
        bottonLayout.getChildren().addAll(send,input);
        bottonLayout.setPadding(new Insets(10,10,10,10));

        //Criando Top
        HBox topLayout = new HBox(2000);
        topLayout.setStyle("-fx-background-color: #336699;");
        Button deleteMesage = new Button("DELETE MESSAGE");
        deleteMesage.setOnAction(event -> {                                      //Delete Aqui
            int index = chat.getSelectionModel().getSelectedIndex();
            chat.getItems().remove(index);
        });
        topLayout.getChildren().add(deleteMesage);
        topLayout.setAlignment(Pos.TOP_CENTER);
        topLayout.setPadding(new Insets(10,10,10,10));

        //Adicionando ao layoutApp
        layoutApp.setBottom(bottonLayout);
        layoutApp.setLeft(leftLayout);
        layoutApp.setRight(rigthLayout);
        layoutApp.setTop(topLayout);
        layoutApp.setCenter(chat);

        //Executando Window
        appScene = new Scene(layoutApp,670,500);
        window.setScene(initial);
        window.show();
    }
}
