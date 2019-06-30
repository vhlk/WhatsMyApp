import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
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

	public static void main(String[] args) {
		int port = 8888;
		int porta = 8421;
		Clientes clientes = new Clientes(); //chamamos o construtor de clientes
		MensagensServidor mensagens = new MensagensServidor(); //construtor de mensagens, que vai guardar mensagens do server
		try {
			ServerSocket tmpSocketIn = new ServerSocket(port); //socket de entrada

			while (true) {
				System.out.println("Aguardando cliente");
				ServerSocket tmpSocketOut = new ServerSocket(porta); //socket de saida
				Socket socketIn = tmpSocketIn.accept(); //cria o socket (de entrada)
				Socket socketOut = tmpSocketOut.accept();  //cria o socket (de saida)
				ReceiveMsg t = new ReceiveMsg(socketIn, socketOut); //cria a thread que irá receber mensagens
				t.start(); //coloca a thread para rodar
				System.out.println("Enviado");
				porta++; //a porta vai aumentar em 1 para que possa ser possível rodar clientes no mesmo computador (porta primeiro cliente = 8421, do segundo = 8422)

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
			BufferedReader entrada = new BufferedReader(new InputStreamReader(this.conexaoEntrada.getInputStream())); //criamos um buffer para receber as mensagens
			PrintStream saida = new PrintStream(this.conexaoSaida.getOutputStream()); //criamos um printStream que irá mandar as mensagens para o cliente usando o OutPutStream
			saida.println("Conectado, por favor digite seu nome:"); //mandará mensagem pedindo para o cliente digitar seu nome
			String nome = entrada.readLine();  //recebe nome do cliente com flag
			nome = nome.substring(1, nome.length()); //retira flag
			saida.println("Pronto, comece a mandar suas mensagens!");
			for(int i = 0; i <  MensagensServidor.mensagens.size(); i++)  //caso o cliente estava offline e mandaram mensagem para ele, aqui o server envia quando ele se conectar
			{
				saida.println(MensagensServidor.mensagens.elementAt(i));
				Thread.sleep(1);
			}
			usuarios.add(nome); //adiciona o nome do cliente à seu vetor de usuários
			Clientes.clientes.add(saida); //adiciona o PrintStream do cliente atual a o vector de PrintStreams (global)
			String mensagem = "";
			mensagem = entrada.readLine();  //recebe a primeira mensagem do cliente
			int cont = 0;
			boolean saiu = false;  //para saber se o cliente se desconectou
			while (true) {  // flag é sempre o primeiro char da string recebida do cliente 
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
					sendMessage(saida, nome, mensagem); //reaproveito o método apenas para enviar uma mensagem com flag 6
					System.out.println("Mensagem lida pelo cliente!");
				}
				else if (!saiu && mensagem.charAt(0) == '1') {  //1: confirmação de recebimento pelo cliente
					sendMessage(saida, nome, mensagem);  //novamente reaproveito para mandar mensagem com flag 1
					System.out.println("Mensagem recebida pelo cliente!");
				}
				else if (!saiu && mensagem.charAt(0) == '2') {  //2: mensagem de pedido de apagar mensagem
					int posicaoRemover = Integer.parseInt(mensagem.substring(1, mensagem.length())); //a mensagem sem flag é o índice da posição a ser removida
					String removido = MensagensServidor.mensagens.remove(posicaoRemover); //remove do server a mensagem que consta naquela posição
					System.out.println("Mensagem removida: " + removido);
					Enumeration<PrintStream> e = Clientes.clientes.elements(); //pego todos os PrintStreams (por onde a mensagem sai)
					for (int i = 0; i < 100; i++)  //"limpar" console
						System.out.println();
					for (int i = 0; i < MensagensServidor.mensagens.size(); i++) { //reimprime as mensagens (sem a que foi apagada)
						System.out.println(MensagensServidor.mensagens.elementAt(i).substring(1, MensagensServidor.mensagens.elementAt(i).length()));
					}
					while (e.hasMoreElements()) { //manda sinal de apagar para todos os clientes
						PrintStream clienteMandar = (PrintStream) e.nextElement();
						clienteMandar.println("3" + posicaoRemover);
					}
					cont--;
				} 
				else if (!saiu){ //caso não tenha flag especial (flag vai ser 0) então é uma mensagem normal
					PrintStream ack = new PrintStream(conexaoSaida.getOutputStream()); //criei outro PrintStream para não ficar confuso
					ack.println("5"); //mandar ack de mensagem recebida
					ReceiveMsg.mensagens.add(mensagem); //a mensagem é adiciona ao vetor da thread
					MensagensServidor.mensagens.add(mensagem.charAt(0)+nome+": "+mensagem.substring(1, mensagem.length())); //server armazena a mensagem sem a flag e com o nome do cliente
					System.out.println("Mensagem recebida: " + ReceiveMsg.mensagens.lastElement());
					//saida.println(mensagem);
					sendMessage(saida, nome  + ": " , mensagem); //chama o método que vai mandar mensagem apenas para o outro cliente
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
			Enumeration<PrintStream> e = Clientes.clientes.elements(); //pega todos os elementos (PrintStream)
			while (e.hasMoreElements()) {
				// obtém o fluxo de saída de um dos CLIENTES
				PrintStream outroCLiente = (PrintStream) e.nextElement();
				// envia para todos, menos para o próprio usuário
				if (outroCLiente != saida) { //se não for o PrintStream do usuário atual significa que é o outro, então manda para ele a mensagem
					char flag = mensagem.charAt(0);
					String dadoMensagem = mensagem.substring(1, mensagem.length()); //separar mensagem da flag
					outroCLiente.println(flag + autor + dadoMensagem);
				}
			}
		} catch (Exception e) { System.out.println(e.getMessage());}
	}

}
class Clientes {  //classe que contem vetores de PrintStream (socket de saida, podemos dizer assim)
	public static Vector<PrintStream> clientes;

	public Clientes() {
		this.clientes = new Vector<PrintStream>();
	}
}

class MensagensServidor {  //vetor para armazenar mensagens dos clientes
	public static Vector<String> mensagens;

	public MensagensServidor() {
		this.mensagens = new Vector<String>();
	}
}

