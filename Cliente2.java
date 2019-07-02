import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;

/* WhatsMyApp � um aplicativo simples de envio de mensagens que utilizada protocolo TCP e aprensenta as seguintes funcionalidades:
 *  -Marca��o de mensagem recebida pelo servidor
 *  -Marca��o de mensagem recebida no destinat�rio
 *  -Marca��o de mensagem lida
 *  -Op��o de apagar mensagem (para todos)
 *  
 * Nesta vers�o inicial ser� realizado para funcionar apenas para dois clientes (utilizando um servidor central) 
 *  
 *  Trabalho realizado por:
 *  	Anderson Cesar de Carvalho Silva
 *		Johnny Herbert Muniz Nunes
 *		Jose Guilherme Nascimento Vieira da Silva
 *		Luana Porciuncula Barreto
 *		Samuel Oliveira de Miranda
 *		Victor Hugo de Lima Kunst
 */

public class Cliente2 {
	public static boolean saiu;
	/*
	 * Flags utilizadas: 
	 * 		1: confirma��o de recebimento pelo cliente 
	 * 		2: mensagem de pedido de apagar mensagem 
	 * 		3: apagar mensagem (seguido pela posicao a ser apagada) 
	 * 		4: sair da conversa 
	 * 		5: confirma��o recebimento pelo servidor
	 * 		6: lido pelo cliente
	 * 		7: reconectar a conversa
	 * 		8: n�o faz nada
	 */

	public static void main(String args[]) throws IOException {
		int port = 8888;// Porta do servidor
		String address = "localhost";// host do servidor
		String flagCliente = "b"; //string para indicar o cliente

		try {
			Socket socket = new Socket(address, port);
			String flag = "0";
			Scanner in = new Scanner(System.in);
			String clientMsg;
			MensagensCliente2 mensagens = new MensagensCliente2(); //chama o construtor de mensagem, que guarda as mensagens do cliente
			ReceiveThreadCliente2 rt = new ReceiveThreadCliente2(socket); //cria a thread de receber mensagens
			rt.start(); //coloca a thread para rodar
			int cont = 0; //caso o cliente saia da conversa, esse int indica a �ltima mensagem recebida
			String nome = "";
			Cliente2.saiu = false; //um booleano para dizer se o cliente saiu ou n�o da conversa
			int posicaoUltimaMensagem = 0;

			while (true) {
				PrintStream saida = new PrintStream(socket.getOutputStream()); // cria uma saida para o server
				boolean podeApagar =  true; //caso ele digite uma posi��o n�o v�lida para apagar, impedir� de mandar a msg para o server
				clientMsg = in.nextLine();
				if (cont == 0) {
					nome = clientMsg; //a primeira mensagem que o cliente envia � sempre o nome
				}
				else if (!Cliente2.saiu && clientMsg.equals("quero apagar")) {  //caso esteja no chat e queira apagar uma mensagem que enviou
					System.out.println("Digite a posi��o da mensagem a ser apagada, por favor:");
					flag = "2"; // flag para apagar
					int posicao = in.nextInt(); //recebe a posi��o do vetor a ser apagada
					in.nextLine(); //java e seus detalhes kkkk
					if (MensagensCliente2.mensagens.size() <= posicao) {
						System.err.println("Voc� digitou uma posi��o inv�lida!");
						podeApagar = false;
					}
					else if (MensagensCliente2.mensagens.elementAt(posicao).charAt(1) == flagCliente.charAt(0)) { //quando o cliente escreve uma mensagem, a flag ser� y (apenas o que vai ser salvo localmente)
						podeApagar = true;
					}
					else { //caso a flag n�o seja y, a mensagem n era dele
						podeApagar = false;
						System.err.println("Voc� n�o pode apagar uma mensagem que voc� n�o enviou!");
					}
					clientMsg = Integer.toString(posicao);
				}
				else if (clientMsg.equals("quero sair")) { //caso o cliente queira sair
					flag = "4";
					System.out.println("Saiu da conversa!");
					posicaoUltimaMensagem = MensagensCliente2.mensagens.size(); //salva a posi��o da �ltima mensagem que recebeu
					Cliente2.saiu = true;
				}
				else if (clientMsg.equals("quero me reconectar")) { //caso o cliente queira se reconectar
					System.out.println("Reconectado com sucesso!");
					saida.println("7"); //avisar ao server que o cliente deseja se reconectar
					flag = "8";
					for (int i = posicaoUltimaMensagem; i < MensagensCliente2.mensagens.size();i++) { //caso alguma mensagem foi enviada para o cliente enquanto ele tinha saido, neste momento elas ser�o impressas
						System.out.println(MensagensCliente2.mensagens.elementAt(i).substring(2, MensagensCliente2.mensagens.elementAt(i).length()));
						saida.println('1'); //para cada mensagem recebida, mandamos acks
						saida.flush();
						Thread.sleep(100);
						saida.println('6');
						saida.flush();
					}
					Cliente2.saiu = false;
				}
				else if (!Cliente2.saiu && cont != 0){ //caso o cliente n�o tenha saido e a mensagem n�o seja a primeira (nome), salvamos no vetor do cliente a mensagem
					MensagensCliente2.mensagens.add(flag + flagCliente + nome + ": "+clientMsg);
				}
				if (podeApagar) saida.println(flag + flagCliente + clientMsg); //caso n�o esteja tentando apagar uma posi��o n�o v�lida, mensagem � enviada ao server (qualquer tipo de mensagem)
				flag = "0";
				cont = 1;
				saida.flush();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class ReceiveThreadCliente2 extends Thread {
	private Socket socketSaida;

	public ReceiveThreadCliente2(Socket socket) {
		this.socketSaida = socket;
	}

	public void run() {

		try {
			String serverMsg;
			int port = 8422; //Porta do cliente: primeiro cliente a se conectar ter� porta 8421, segundo cliente, 8422
			String address = "localhost";// host do servidor

			BufferedReader input;
			Socket socket = new Socket(address, port);

			while (true) {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream())); //recebe mensagens vindas do server
				serverMsg = input.readLine();
				if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '5') { //ack do server
					System.out.println("Mensagem recebida pelo servidor!");
				}
				else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '1') { //mensagem do server avisando que o outro cliente recebeu a mensagem
					System.out.println("Mensagem recebida!");
				}
				else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '6') { //mensagem do server avisando que o outro cliente leu a mensagem
					System.out.println("Mensagem lida!");
				}
				else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '3') { //recebe do server que deve apagar uma mensagem (pode ser o pr�prio pedido do cliente)
					int posicaoSerRemovida = Integer.parseInt(serverMsg.substring(1, serverMsg.length())); //pega a mensagem sem a flag (�ndice a remover)
					MensagensCliente2.mensagens.removeElementAt(posicaoSerRemovida); //remove a mensagem daquela posi��o do array
					for (int i = 0; i < 100; i++) //"limpa" o console
						System.out.println(); 
					for (int i = 0; i < MensagensCliente2.mensagens.size(); i++) {  //imprime as mensagens novamente
						System.out.println(MensagensCliente2.mensagens.elementAt(i).substring(2, MensagensCliente2.mensagens.elementAt(i).length())); //imprime sem as flags, obviamente
					}
				} else {
					if (serverMsg != null && serverMsg.length() > 0 && !serverMsg.equals("Conectado, por favor digite seu nome:")
							&& !serverMsg.equals("Pronto, comece a mandar suas mensagens!")) { //caso n�o seja essas mensagens do server, ser� uma mensagem do outro cliente
						MensagensCliente2.mensagens.addElement(serverMsg); //ent�o adicionamos a mensagem ao vetor
						PrintStream ack = new PrintStream(socketSaida.getOutputStream());
						ack.println('1'); //mandamos acks
						ack.flush();
						Thread.sleep(100);
						ack.println('6');
						ack.flush();
						serverMsg = serverMsg.substring(2, serverMsg.length()); //tiramos as flags
					}
					if (!Cliente2.saiu) System.out.println(serverMsg);
				}
			}

		} catch (Exception e) {
			System.err.println("Deu erro: "+e.getMessage());
		}

	}
}

class MensagensCliente2 { //server apenas como um local global para guardar mensagens
	public static Vector<String> mensagens;

	public MensagensCliente2() {
		this.mensagens = new Vector<String>();
	}
}