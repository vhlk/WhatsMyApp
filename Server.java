import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class Server {

	/*
	 * Flags utilizadas: 
	 * 		1: confirmação de recebimento pelo cliente 
	 * 		2: mensagem de pedido de apagar mensagem 
	 * 		3: apagar mensagem (seguido pela posicao a ser apagada) 
	 * 		4: sair da conversa 
	 * 		5: confirmação recebimento pelo servidor
	 */

	public static Vector<String> mensagens = new Vector<String>();
	private Socket conexao;
	private static Vector CLIENTES;

	public Server(Socket socket) {
		this.conexao = socket;
	}

	public static void main(String[] args) {
		int port = 8888;
		int porta = 8421;
		try {
			ServerSocket tmpSocketIn = new ServerSocket(port);

			while (true) {
				System.out.println("Aguardando cliente");
				ServerSocket tmpSocketOut = new ServerSocket(porta);
				Socket socketIn = tmpSocketIn.accept();
				Socket socketOut = tmpSocketOut.accept();
				/*DataInputStream inputStream;
				DataOutputStream outputStream;
				String strData, ans;
				
				 * inputStream = new DataInputStream(socketIn.getInputStream()); strData =
				 * inputStream.readUTF(); strData = strData.trim();
				 * 
				 * System.out.println("Texto recebido do cliente: " + strData); if
				 * (strData.charAt(0) == 1) { //1 -> ack p/ cliente ans = "ack"; } else if
				 * (strData.charAt(0) == 2) { //2 -> apagar ans = "apagar" +
				 * strData.substring(1, strData.length()); } else if
				 * (strData.contains("InfraCom")) { ans =
				 * "IF678, a disciplina de InfraEstrutura de Comunicacao ministrada pelo professor Paulo Goncalves "
				 * ; } else if (strData.contains("Ola") || strData.contains("Oi")) { ans =
				 * "Ola, como esta voce?s"; } else if (strData.contains("Tudo bem?")) { ans =
				 * "Tudo, e com voce?"; } else if (strData.contains("Adriano")) { ans =
				 * "O melhor monitor!"; } else { ans = "Mensagem recebida!"; }
				 */
				ReceiveMsg t = new ReceiveMsg(socketIn, socketOut);
				t.start();
				PrintStream saida = new PrintStream(socketOut.getOutputStream());
				// outputStream = new DataOutputStream(socketOut.getOutputStream());
				System.out.println("Enviado");
				porta++;

			}

		} catch (BindException e) {
			System.out.println("Endereco em uso");
		} catch (Exception e) {
			System.out.println("Erro" + e);
		}

	}

	/*
	 * public void run(Socket socketClient) { try { BufferedReader entrada = new
	 * BufferedReader(new InputStreamReader(socketClient.getInputStream()));
	 * PrintStream saida = new PrintStream(this.conexao.getOutputStream());
	 * saida.println("Conectado"); mensagens.add(entrada.readLine());
	 * System.out.println(entrada.readLine()); System.out.println(mensagens.get(0));
	 * 
	 * 
	 * 
	 * DataInputStream inputStream; String strData; inputStream = new
	 * DataInputStream(socketClient.getInputStream()); strData =
	 * inputStream.readUTF(); strData = strData.trim(); mensagens.add(strData);
	 * System.out.println(mensagens.get(0)); System.out.println(strData);
	 * 
	 * } catch(Exception e) { System.out.println("Deu erro!"); } }
	 */

}

class ReceiveMsg extends Thread {
	public static Vector<String> mensagens;
	public static Vector<String> usuarios;

	private Socket conexaoEntrada;
	private Socket conexaoSaida;

	public ReceiveMsg(Socket conexaoEntrada, Socket conexaoSaida) {
		this.mensagens = new Vector<String>();
		this.usuarios = new Vector<String>();
		this.conexaoEntrada = conexaoEntrada;
		this.conexaoSaida = conexaoSaida;
	}

	public void run() {
		try {
			BufferedReader entrada = new BufferedReader(new InputStreamReader(this.conexaoEntrada.getInputStream()));
			PrintStream saida = new PrintStream(this.conexaoSaida.getOutputStream());

			saida.println("Conectado, por favor digite seu nome:");
			String nome = entrada.readLine();
			saida.println("Pronto, comece a mandar suas mensagens!");
			usuarios.add(nome);
			String mensagem = "";
			mensagem = entrada.readLine();
			int cont = 0;
			while (mensagem.charAt(0) != '4') {
				//ACKMensagemRecebidaServer ack = new ACKMensagemRecebidaServer(this.conexaoSaida);
				//ack.start();
				if (mensagem.charAt(0) == '1') {
					System.out.println("Mensagem Recebida pelo Cliente!");
				}
				else if (mensagem.charAt(0) == '2') {
					int posicaoRemover = Integer.parseInt(mensagem.substring(1, mensagem.length()));
					String removido = ReceiveMsg.mensagens.remove(posicaoRemover);
					System.out.println("Mensagem removida: " + removido);
					saida.println("3" + posicaoRemover);
					cont--;
				} else {
					PrintStream ack = new PrintStream(conexaoSaida.getOutputStream());
					ack.println("5");
					ReceiveMsg.mensagens.add(mensagem);
					System.out.println("Mensagem recebida: " + ReceiveMsg.mensagens.lastElement());
					saida.println(mensagem);
					cont++;
				}
				mensagem = entrada.readLine();
			}
			System.out.println("Sai da conversa");
		} catch (Exception e) {
			System.err.println("Deu erro: "+e.getMessage());
		}
	}

}

class ACKMensagemRecebidaServer extends Thread {
	private Socket saida;

	public ACKMensagemRecebidaServer(Socket saida) {
		this.saida = saida;
	}

	public void run() {

		try {
			// DataOutputStream outputStream;
			PrintStream ack;
			//int port = 8888;// Porta do servidor
			String address = "localhost";// host do servidor

			//Socket socket = new Socket(address, port);

			ack = new PrintStream(this.saida.getOutputStream());
			ack.println("5");
			ack.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
