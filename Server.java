import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
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
	 * 		7: reconectar a conversa
	 * 		8: não faz nada
	 */

	public static Vector<String> mensagens = new Vector<String>();
	private Socket conexao;

	public Server(Socket socket) {
		this.conexao = socket;
	}

	public static void main(String[] args) {
		int port = 8888;
		int porta = 8421;
		Clientes clientes = new Clientes();
		MensagensServidor mensagens = new MensagensServidor();
		try {
			ServerSocket tmpSocketIn = new ServerSocket(port);

			while (true) {
				System.out.println("Aguardando cliente");
				ServerSocket tmpSocketOut = new ServerSocket(porta);
				Socket socketIn = tmpSocketIn.accept();
				Socket socketOut = tmpSocketOut.accept();
				ReceiveMsg t = new ReceiveMsg(socketIn, socketOut);
				t.start();
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
			nome = nome.substring(1, nome.length());
			saida.println("Pronto, comece a mandar suas mensagens!");
			for(int i = 0; i <  MensagensServidor.mensagens.size(); i++)
			{
				saida.println(MensagensServidor.mensagens.elementAt(i));
				Thread.sleep(1);
			}
			usuarios.add(nome);
			Clientes.clientes.add(saida);
			String mensagem = "";
			mensagem = entrada.readLine();
			int cont = 0;
			boolean saiu = false;
			while (true) {
				if (mensagem.charAt(0) == '8') {} //8: não faz nada
				else if (mensagem.charAt(0) == '7') {  //7: reconectar a conversa
					saiu = false;
					System.out.println("Cliente retornou!");
				}
				else if (mensagem.charAt(0) == '4') {  //4: sair da conversa 
					saiu = true;
					System.out.println("Cliente saiu!");
				}
				else if (!saiu && mensagem.charAt(0) == '6') {  //6: lido pelo cliente
					sendMessage(saida, nome, mensagem);
					System.out.println("Mensagem lida pelo cliente!");
				}
				else if (!saiu && mensagem.charAt(0) == '1') {  //1: confirmação de recebimento pelo cliente
					sendMessage(saida, nome, mensagem);
					System.out.println("Mensagem recebida pelo cliente!");
				}
				else if (!saiu && mensagem.charAt(0) == '2') {  //2: mensagem de pedido de apagar mensagem
					int posicaoRemover = Integer.parseInt(mensagem.substring(1, mensagem.length()));
					String removido = MensagensServidor.mensagens.remove(posicaoRemover);
					System.out.println("Mensagem removida: " + removido);
					Enumeration<PrintStream> e = Clientes.clientes.elements();
					for (int i = 0; i < 100; i++)
						System.out.println();
					for (int i = 0; i < MensagensServidor.mensagens.size(); i++) {
						System.out.println(MensagensServidor.mensagens.elementAt(i).substring(1, MensagensServidor.mensagens.elementAt(i).length()));
					}
					while (e.hasMoreElements()) {
						PrintStream clienteMandar = (PrintStream) e.nextElement();
						clienteMandar.println("3" + posicaoRemover);
					}
					cont--;
				} 
				else if (!saiu){
					PrintStream ack = new PrintStream(conexaoSaida.getOutputStream());
					ack.println("5");
					ReceiveMsg.mensagens.add(mensagem);
					MensagensServidor.mensagens.add(mensagem.charAt(0)+nome+": "+mensagem.substring(1, mensagem.length()));
					System.out.println("Mensagem recebida: " + ReceiveMsg.mensagens.lastElement());
					//saida.println(mensagem);
					sendMessage(saida, nome  + ": " , mensagem);
					cont++;
				}
				mensagem = entrada.readLine();
			}
			//System.out.println("Sai da conversa");
		} catch (Exception e) {
			System.err.println("Deu erro: "+e.getMessage());
		}
	}
	public void sendMessage(PrintStream saida, String autor, String mensagem)
	{
		try {
			Enumeration<PrintStream> e = Clientes.clientes.elements();
			while (e.hasMoreElements()) {
				// obtém o fluxo de saída de um dos CLIENTES
				PrintStream outroCLiente = (PrintStream) e.nextElement();
				// envia para todos, menos para o próprio usuário
				if (outroCLiente != saida) {
					char flag = mensagem.charAt(0);
					String dadoMensagem = mensagem.substring(1, mensagem.length());
					outroCLiente.println(flag + autor + dadoMensagem);
				}
			}
		} catch (Exception e) { System.out.println(e.getMessage());}
	}

}
class Clientes {
	public static Vector<PrintStream> clientes;

	public Clientes() {
		this.clientes = new Vector<PrintStream>();
	}
}

class MensagensServidor {
	public static Vector<String> mensagens;

	public MensagensServidor() {
		this.mensagens = new Vector<String>();
	}
}

