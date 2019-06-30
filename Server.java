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
	 * 		6: lido pelo cliente
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
				ReceiveMsg t = new ReceiveMsg(socketIn, socketOut);
				t.start()
				System.out.println("Enviado");
				porta++;

			}

		} catch (BindException e) {
			System.out.println("Endereco em uso");
		} catch (Exception e) {
			System.out.println("Erro" + e);
		}

	}

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
				if (mensagem.charAt(0) == '6') {
					System.out.println("Mensagem lida pelo cliente!");
				}
				else if (mensagem.charAt(0) == '1') {
					System.out.println("Mensagem recebida pelo cliente!");
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
