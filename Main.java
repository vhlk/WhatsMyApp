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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;


public class Main extends Application {
    public boolean saiu;
    public Vector<String> vectorMensagens = new Vector<>();
    public int portServer = 8888;// Porta do servidor
    public int portReceive = 8421;
    public String nome = "";
    public String address = "localhost";// host do servidor
    public String flagCliente = "a"; //string para indicar o cliente
    Scene initial, appScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {

            Socket socket = new Socket(address, portServer);
            PrintStream saida = new PrintStream(socket.getOutputStream());
            ReceiveThreadCliente1 rt = new ReceiveThreadCliente1(socket); //cria a thread de receber mensagens
            View chat = new View(); // criando chatView
            MensagensCliente1 vectorMensagens = new MensagensCliente1();
            rt.start(); //coloca a thread para rodar
            saiu = false; //um booleano para dizer se o cliente saiu ou n�o da conversa
            //
            Stage window = primaryStage;
            window.setTitle("WhatsMyApp");
            //Layout initial
            Label nameApp = new Label("WhatsMyApp");
            nameApp.setMaxSize(500,500);
            nameApp.setAlignment(Pos.TOP_CENTER);
            nameApp.setStyle("-fx-font-size:50px;-fx-text-fill: white;");
            nameApp.setPadding(new Insets(10,10,100,10));
            GridPane.setConstraints(nameApp,0,0);
            Button start = new Button("START APP");
            start.setMinWidth(350);
            GridPane.setConstraints(start,0,2);
            TextField yourName = new TextField();
            yourName.setMinWidth(300);
            GridPane.setConstraints(yourName,0,1);
            yourName.setPromptText("Digite seu nome aqui");
            start.setOnAction(event -> {
                nome = yourName.getCharacters().toString();
                if(!nome.equals("")){
                    window.setScene(appScene);
                    saida.println(nome);
                }
            });
            GridPane layoutOpen = new GridPane();
            layoutOpen.setStyle("-fx-background-color: #336699;");
            layoutOpen.getChildren().addAll(start,yourName,nameApp);
            layoutOpen.setAlignment(Pos.CENTER);
            initial = new Scene(layoutOpen, 670, 500);

            //Layout app
            BorderPane layoutApp = new BorderPane();

            //Criando Middle
            chat.getChat().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

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
                if (!message.equals("")) {
                    Label label = new Label(nome + ": " + message);
                    label.setStyle("-fx-text-fill: #800000;");
                    View.chat.getItems().add(label);
                    input.clear();
                    saida.println(0 + flagCliente + message);
                    MensagensCliente1.mensagens.add(0 + flagCliente + nome + ": "+ message);
                }
            });
            send.setMinWidth(500);
            GridPane.setConstraints(send, 0, 1);
            input.setMinWidth(500);
            GridPane.setConstraints(input, 0, 0);
            bottonLayout.getChildren().addAll(send, input);
            bottonLayout.setPadding(new Insets(10, 10, 10, 10));

            //Criando Top
            HBox topLayout = new HBox(2000);
            topLayout.setStyle("-fx-background-color: #336699;");
            Button deleteMesage = new Button("DELETE MESSAGE");
            deleteMesage.setOnAction(event -> {                                       //Delete Aqui
                boolean podeApagar =  true;
                int index = View.chat.getSelectionModel().getSelectedIndex();
                if (MensagensCliente1.mensagens.elementAt(index).charAt(1) == flagCliente.charAt(0)) { //quando o cliente escreve uma mensagem, a flag ser� y (apenas o que vai ser salvo localmente)
                    podeApagar = true;
                } else { //caso a flag n�o seja y, a mensagem n era dele
                    podeApagar = false;
                    System.err.println("Voc� n�o pode apagar uma mensagem que voc� n�o enviou!");
                }
                if(podeApagar){
                    View.chat.getItems().remove(index);
                    saida.println(2 + flagCliente + index);
                    MensagensCliente1.mensagens.remove(index);
                }
            });
            topLayout.getChildren().add(deleteMesage);
            topLayout.setAlignment(Pos.TOP_CENTER);
            topLayout.setPadding(new Insets(10, 10, 10, 10));

            //Adicionando ao layoutApp
            layoutApp.setBottom(bottonLayout);
            layoutApp.setLeft(leftLayout);
            layoutApp.setRight(rigthLayout);
            layoutApp.setTop(topLayout);
            layoutApp.setCenter(chat.getChat());

            //Executando Window
            appScene = new Scene(layoutApp, 670, 500);
            window.setScene(initial);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
class ReceiveThreadCliente1 extends Thread {
    private Socket socketSaida;

    public ReceiveThreadCliente1(Socket socket) {
        this.socketSaida = socket;
    }

    public void run() {

        try {
            String serverMsg;
            int port = 8422; //Porta do cliente: primeiro cliente a se conectar ter� porta 8421, segundo cliente, 8422
            String address = "localhost";// host do servidor
            int indexEnviadas = 0;
            int indexRecebidas = 0;
            int indexLidas = 0;
            BufferedReader input;
            Socket socket = new Socket(address, port);
            while (true) {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream())); //recebe mensagens vindas do server
                serverMsg = input.readLine();
                if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '5') { //ack do server
                    System.out.println("ack recebido");
                    View.chat.getItems().get(indexEnviadas).setStyle("-fx-text-fill: #008B8B;");
                    indexEnviadas++;
                    System.out.println("Mensagem recebida pelo servidor!");
                }
                else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '1') { //mensagem do server avisando que o outro cliente recebeu a mensagem
                    View.chat.getItems().get(indexRecebidas).setStyle("-fx-text-fill: #00CED1;");
                    System.out.println("Mensagem recebida!");
                    indexRecebidas++;
                }
                else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '6') { //mensagem do server avisando que o outro cliente leu a mensagem
                    View.chat.getItems().get(indexLidas).setStyle("-fx-text-fill: #4682B4;");
                    System.out.println("Mensagem lida!");
                    indexLidas++;
                }
                else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '3') { //recebe do server que deve apagar uma mensagem (pode ser o pr�prio pedido do cliente)
                    int posicaoSerRemovida = Integer.parseInt(serverMsg.substring(1, serverMsg.length())); //pega a mensagem sem a flag (�ndice a remover)
                    MensagensCliente1.mensagens.removeElementAt(posicaoSerRemovida); //remove a mensagem daquela posi��o do array
                    View.chat.getItems().remove(posicaoSerRemovida);
                } else {
                    if (serverMsg != null && serverMsg.length() > 0 && !serverMsg.equals("Conectado, por favor digite seu nome:")
                            && !serverMsg.equals("Pronto, comece a mandar suas mensagens!")) { //caso n�o seja essas mensagens do server, ser� uma mensagem do outro cliente
                        MensagensCliente1.mensagens.addElement(serverMsg); //ent�o adicionamos a mensagem ao vetor
                        PrintStream ack = new PrintStream(socketSaida.getOutputStream());
                        ack.println("1"); //mandamos acks
                        ack.println("6");
                        Label label = new Label(serverMsg);
                        label.setStyle("-fx-text-fill: #008000;");
                        View.chat.getItems().add(label);
                        indexEnviadas++;
                        indexLidas++;
                        indexRecebidas++;
                    }


                }
            }

        } catch (Exception e) {
            System.err.println("Deu erro: "+e.getMessage());
        }

    }
}
class MensagensCliente1 { //server apenas como um local global para guardar mensagens
    public static Vector<String> mensagens;

    public MensagensCliente1() {
        this.mensagens = new Vector<String>();
    }
}
class View{
    public static ListView<Label> chat;
    public View(){
        this.chat = new ListView();
    }
    public ListView getChat(){
        return this.chat;
    }
}
