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

public class Cliente1 {
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

		try {
			Socket socket = new Socket(address, port);
			char flag = '0';
			Scanner in = new Scanner(System.in);
			String clientMsg;
			Mensagens mensagens = new Mensagens();
			ReceiveThread rt = new ReceiveThread(socket);
			rt.start();
			int cont = 0;
			String nome = "";
			Cliente1.saiu = false;
			int posicaoUltimaMensagem = 0;

			while (true) {
				PrintStream saida = new PrintStream(socket.getOutputStream());
				boolean podeApagar =  true;
				clientMsg = in.nextLine();
				if (cont == 0) {
					nome = clientMsg;
				}
				else if (!Cliente1.saiu && clientMsg.equals("quero apagar")) {
					System.out.println("Digite a posi��o da mensagem a ser apagada, por favor:");
					flag = '2'; // flag para apagar
					int posicao = in.nextInt();
					in.nextLine();
					if (Mensagens.mensagens.elementAt(posicao).charAt(0) == 'y') {
						podeApagar = true;
					}
					else {
						podeApagar = false;
						System.err.println("Voc� n�o pode apagar uma mensagem que voc� n�o enviou!");
					}
					clientMsg = Integer.toString(posicao);
				}
				else if (clientMsg.equals("quero sair")) {
					flag = '4';
					System.out.println("Saiu da conversa!");
					posicaoUltimaMensagem = Mensagens.mensagens.size();
					Cliente1.saiu = true;
				}
				else if (clientMsg.equals("quero me reconectar")) {
					System.out.println("Reconectado com sucesso!");
					saida.println("7");
					flag = '8';
					for (int i = posicaoUltimaMensagem; i < Mensagens.mensagens.size();i++) {
						System.out.println(Mensagens.mensagens.elementAt(i).substring(1, Mensagens.mensagens.elementAt(i).length()));
						saida.println("1");
						saida.println("6");
					}
					Cliente1.saiu = false;
				}
				else if (!Cliente1.saiu && cont != 0){
					Mensagens.mensagens.add('y'+nome+": "+clientMsg);
				}
				if (podeApagar) saida.println(flag + clientMsg);
				flag = '0';
				cont = 1;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class ReceiveThread extends Thread {
	private Socket socketSaida;

	public ReceiveThread(Socket socket) {
		this.socketSaida = socket;
	}

	public void run() {

		try {
			String serverMsg;
			int port = 8422;// Porta do cliente
			String address = "localhost";// host do servidor

			BufferedReader input;
			Socket socket = new Socket(address, port);

			while (true) {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				serverMsg = input.readLine();
				if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '5') { // ack do server
					System.out.println("Mensagem recebida pelo servidor!");
				}
				else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '1') {
					System.out.println("Mensagem recebida!");
				}
				else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '6') {
					System.out.println("Mensagem lida!");
				}
				else if (serverMsg != null && serverMsg.length() > 0 && serverMsg.charAt(0) == '3') { // apagar
					int posicaoSerRemovida = Integer.parseInt(serverMsg.substring(1, serverMsg.length()));
					Mensagens.mensagens.removeElementAt(posicaoSerRemovida);
					for (int i = 0; i < 100; i++)
						System.out.println();
					for (int i = 0; i < Mensagens.mensagens.size(); i++) {
						System.out.println(Mensagens.mensagens.elementAt(i).substring(1, Mensagens.mensagens.elementAt(i).length()));
					}
				} else {
					if (serverMsg != null && serverMsg.length() > 0 && !serverMsg.equals("Conectado, por favor digite seu nome:")
							&& !serverMsg.equals("Pronto, comece a mandar suas mensagens!")) {
						Mensagens.mensagens.addElement(serverMsg);
						PrintStream ack = new PrintStream(socketSaida.getOutputStream());
						ack.println("1");
						ack.println("6");
						serverMsg = serverMsg.substring(1, serverMsg.length());
					}
					if (!Cliente1.saiu) System.out.println(serverMsg);
				}
			}

		} catch (Exception e) {
			System.err.println("Deu erro: "+e.getMessage());
		}

	}
}

class Mensagens {
	public static Vector<String> mensagens;

	public Mensagens() {
		this.mensagens = new Vector<String>();
	}
}