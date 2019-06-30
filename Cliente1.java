import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

public class Cliente1 {

	/*
	 * Flags utilizadas: 
	 * 		1: confirmação de recebimento pelo cliente 
	 * 		2: mensagem de pedido de apagar mensagem 
	 * 		3: apagar mensagem (seguido pela posicao a ser apagada) 
	 * 		4: sair da conversa 
	 * 		5: confirmação recebimento pelo servidor
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

			while (true) {
				clientMsg = in.nextLine();
				if (clientMsg.equals("quero apagar")) {
					System.out.println("Digite a posição da mensagem a ser apagada, por favor:");
					flag = '2'; // flag para apagar
					int posicao = in.nextInt();
					in.nextLine();
					clientMsg = Integer.toString(posicao);
				}
				if (clientMsg.equals("quero sair")) {
					flag = '4';
					System.out.println("Saiu da conversa!");
				}
				PrintStream saida = new PrintStream(socket.getOutputStream());
				saida.println(flag + clientMsg);
				flag = '0';
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
			int port = 8421;// Porta do cliente
			String address = "localhost";// host do servidor

			BufferedReader input;
			Socket socket = new Socket(address, port);

			while (true) {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				serverMsg = input.readLine();
				if (serverMsg.charAt(0) == '5') { // ack do server
					System.out.println("Mensagem recebida pelo servidor!");
				}
				else if (serverMsg.charAt(0) == '3') { // apagar
					for (int i = 0; i < 100; i++)
						System.out.println();
					int posicaoSerRemovida = Integer.parseInt(serverMsg.substring(1, serverMsg.length()));
					Mensagens.mensagens.removeElementAt(posicaoSerRemovida);
					for (int i = 0; i < Mensagens.mensagens.size(); i++) {
						System.out.println(Mensagens.mensagens.elementAt(i));
					}
				} else {
					if (!serverMsg.equals("Conectado, por favor digite seu nome:")
							&& !serverMsg.equals("Pronto, comece a mandar suas mensagens!")) {
						Mensagens.mensagens.addElement(serverMsg);
						PrintStream ack = new PrintStream(socketSaida.getOutputStream());
						ack.println("1");
					}
					System.out.println(serverMsg);
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
