import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;

/* WhatsMyApp é um aplicativo simples de envio de mensagens que utilizada protocolo TCP e aprensenta as seguintes funcionalidades:
 *  -Marcação de mensagem recebida pelo servidor
 *  -Marcação de mensagem recebida no destinatário
 *  -Marcação de mensagem lida
 *  -Opção de apagar mensagem (para todos)
 *  
 * Nesta versão inicial será realizado para funcionar apenas para dois clientes (utilizando um servidor central) 
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
	 * 		1: confirmação de recebimento pelo cliente 
	 * 		2: mensagem de pedido de apagar mensagem 
	 * 		3: apagar mensagem (seguido pela posicao a ser apagada) 
	 * 		4: sair da conversa 
	 * 		5: confirmação recebimento pelo servidor
	 * 		6: lido pelo cliente
	 * 		7: reconectar a conversa
	 * 		8: não faz nada
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
			int cont = 0; //caso o cliente saia da conversa, esse int indica a última mensagem recebida
			String nome = "";
			Cliente2.saiu = false; //um booleano para dizer se o cliente saiu ou não da conversa
			int posicaoUltimaMensagem = 0;

			while (true) {
				PrintStream saida = new PrintStream(socket.getOutputStream()); // cria uma saida para o server
				boolean podeApagar =  true; //caso ele digite uma posição não válida para apagar, impedirá de mandar a msg para o server
				clientMsg = in.nextLine();
				if (cont == 0) {
					nome = clientMsg; //a primeira mensagem que o cliente envia é sempre o nome
				}
				else if (!Cliente2.saiu && clientMsg.equals("quero apagar")) {  //caso esteja no chat e queira apagar uma mensagem que enviou
					System.out.println("Digite a posição da mensagem a ser apagada, por favor:");
					flag = "2"; // flag para apagar
					int posicao = in.nextInt(); //recebe a posição do vetor a ser apagada
					in.nextLine(); //java e seus detalhes kkkk
					if (MensagensCliente2.mensagens.size() <= posicao) {
						System.err.println("Você digitou uma posição inválida!");
						podeApagar = false;
					}
					else if (MensagensCliente2.mensagens.elementAt(posicao).charAt(1) == flagCliente.charAt(0)) { //quando o cliente escreve uma mensagem, a flag será y (apenas o que vai ser salvo localmente)
						podeApagar = true;
					}
					else { //caso a flag não seja y, a mensagem n era dele
						podeApagar = false;
						System.err.println("Você não pode apagar uma mensagem que você não enviou!");
					}
					clientMsg = Integer.toString(posicao);
				}
				else if (clientMsg.equals("quero sair")) { //caso o cliente queira sair
					flag = "4";
					System.out.println("Saiu da conversa!");
					posicaoUltimaMensagem = MensagensCliente2.mensagens.size(); //salva a posição da última mensagem que recebeu
					Cliente2.saiu = true;
				}
				else if (clientMsg.equals("quero me reconectar")) { //caso o cliente queira se reconectar
					System.out.println("Reconectado com sucesso!");
					saida.println("7"); //avisar ao server que o cliente deseja se reconectar
					flag = "8";
					for (int i = posicaoUltimaMensagem; i < MensagensCliente2.mensagens.size();i++) { //caso alguma mensagem foi enviada para o cliente enquanto ele tinha saido, neste momento elas serão impressas
						System.out.println(MensagensCliente2.mensagens.elementAt(i).substring(2, MensagensCliente2.mensagens.elementAt(i).length()));
						saida.println('1'); //para cada mensagem recebida, mandamos acks
						Thread.sleep(100);
						saida.println('6');
					}
					Cliente2.saiu = false;
				}
				else if (!Cliente2.saiu && cont != 0){ //caso o cliente não tenha saido e a mensagem não seja a primeira (nome), salvamos no vetor do cliente a mensagem
					MensagensCliente2.mensagens.add(flag + flagCliente + nome + ": "+clientMsg);
				}
				if (podeApagar) saida.println(flag + flagCliente + clientMsg); //caso não esteja tentando apagar uma posição não válida, mensagem é enviada ao server (qualquer tipo de mensagem)
				flag = "0";
				cont = 1;
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
			int port = 8422; //Porta do cliente: primeiro cliente a se conectar terá porta 8421, segundo cliente, 8422
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
				else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '3') { //recebe do server que deve apagar uma mensagem (pode ser o próprio pedido do cliente)
					int posicaoSerRemovida = Integer.parseInt(serverMsg.substring(1, serverMsg.length())); //pega a mensagem sem a flag (índice a remover)
					MensagensCliente2.mensagens.removeElementAt(posicaoSerRemovida); //remove a mensagem daquela posição do array
					for (int i = 0; i < 100; i++) //"limpa" o console
						System.out.println(); 
					for (int i = 0; i < MensagensCliente2.mensagens.size(); i++) {  //imprime as mensagens novamente
						System.out.println(MensagensCliente2.mensagens.elementAt(i).substring(2, MensagensCliente2.mensagens.elementAt(i).length())); //imprime sem as flags, obviamente
					}
				} else {
					if (serverMsg != null && serverMsg.length() > 0 && !serverMsg.equals("Conectado, por favor digite seu nome:")
							&& !serverMsg.equals("Pronto, comece a mandar suas mensagens!")) { //caso não seja essas mensagens do server, será uma mensagem do outro cliente
						MensagensCliente2.mensagens.addElement(serverMsg); //então adicionamos a mensagem ao vetor
						PrintStream ack = new PrintStream(socketSaida.getOutputStream());
						ack.println('1'); //mandamos acks
						Thread.sleep(100);
						ack.println('6');
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