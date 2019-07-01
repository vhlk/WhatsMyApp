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
	 * 		1: confirma��o de recebimento pelo cliente 
	 * 		2: mensagem de pedido de apagar mensagem 
	 * 		3: apagar mensagem (seguido pela posicao a ser apagada) 
	 * 		4: sair da conversa 
	 * 		5: confirma��o recebimento pelo servidor
	 * 		6: lido pelo cliente
	 * 		7: reconectar a conversa
	 * 		8: n�o faz nada
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
				ReceiveMsg t = new ReceiveMsg(socketIn, socketOut); //cria a thread que ir� receber mensagens
				t.start(); //coloca a thread para rodar
				System.out.println("Enviado");
				porta++; //a porta vai aumentar em 1 para que possa ser poss�vel rodar clientes no mesmo computador (porta primeiro cliente = 8421, do segundo = 8422)

			}

		} catch (BindException e) {
			System.out.println("Endereco em uso");
		} catch (Exception e) {
			System.out.println("Erro" + e.getMessage());
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
			PrintStream saida = new PrintStream(this.conexaoSaida.getOutputStream()); //criamos um printStream que ir� mandar as mensagens para o cliente usando o OutPutStream
			saida.println("Conectado, por favor digite seu nome:"); //mandar� mensagem pedindo para o cliente digitar seu nome
			String nome = entrada.readLine();  //recebe nome do cliente com flag
			nome = nome.substring(2, nome.length()); //retira flag
			saida.println("Pronto, comece a mandar suas mensagens!");
			for(int i = 0; i <  MensagensServidor.mensagens.size(); i++)  //caso o cliente estava offline e mandaram mensagem para ele, aqui o server envia quando ele se conectar
			{
				Thread.sleep(100);
				saida.println(MensagensServidor.mensagens.elementAt(i));
			}
			usuarios.add(nome); //adiciona o nome do cliente � seu vetor de usu�rios
			Clientes.clientes.add(saida); //adiciona o PrintStream do cliente atual a o vector de PrintStreams (global)
			String mensagem = "";
			mensagem = entrada.readLine();  //recebe a primeira mensagem do cliente
			int cont = 0;
			boolean saiu = false;  //para saber se o cliente se desconectou
			while (true) {  // flag � sempre o primeiro char da string recebida do cliente 
				if (mensagem.charAt(0) == '8') {} //8: n�o faz nada
				else if (mensagem.charAt(0) == '7') {  //7: reconectar a conversa
					saiu = false;
					System.out.println("Cliente retornou!");
				}
				else if (mensagem.charAt(0) == '4') {  //4: sair da conversa 
					saiu = true;
					System.out.println("Cliente saiu!");
				}
				else if (!saiu && mensagem.charAt(0) == '6') {  //6: lido pelo cliente
					Enumeration<PrintStream> e = Clientes.clientes.elements(); //pega todos os elementos (PrintStream)
					while (e.hasMoreElements()) { //isso est� explicado no sendMessage
						PrintStream outroCLiente = (PrintStream) e.nextElement();
						if (outroCLiente != saida) {
							outroCLiente.println("6");
							outroCLiente.flush();
						}
					}
					System.out.println("Mensagem lida pelo cliente!");
				}
				else if (!saiu && mensagem.charAt(0) == '1') {  //1: confirma��o de recebimento pelo cliente
					Enumeration<PrintStream> e = Clientes.clientes.elements(); //pega todos os elementos (PrintStream)
					while (e.hasMoreElements()) { //isso est� explicado no sendMessage
						PrintStream outroCLiente = (PrintStream) e.nextElement();
						if (outroCLiente != saida) {
							outroCLiente.println("1");
							outroCLiente.flush();
						}
					}
					System.out.println("Mensagem recebida pelo cliente!");
				}
				else if (!saiu && mensagem.charAt(0) == '2') {  //2: mensagem de pedido de apagar mensagem
					int posicaoRemover = Integer.parseInt(mensagem.substring(2, mensagem.length())); //a mensagem sem flags � o �ndice da posi��o a ser removida
					String removido = MensagensServidor.mensagens.remove(posicaoRemover); //remove do server a mensagem que consta naquela posi��o
					System.out.println("Mensagem removida: " + removido);
					Enumeration<PrintStream> e = Clientes.clientes.elements(); //pego todos os PrintStreams (por onde a mensagem sai)
					for (int i = 0; i < 100; i++)  //"limpar" console
						System.out.println();
					for (int i = 0; i < MensagensServidor.mensagens.size(); i++) { //reimprime as mensagens (sem a que foi apagada)
						System.out.println(MensagensServidor.mensagens.elementAt(i).substring(2, MensagensServidor.mensagens.elementAt(i).length()));
					}
					while (e.hasMoreElements()) { //manda sinal de apagar para todos os clientes
						PrintStream clienteMandar = (PrintStream) e.nextElement();
						clienteMandar.println("3" + posicaoRemover);
					}
					cont--;
				} 
				else if (!saiu){ //caso n�o tenha flag especial (flag vai ser 0) ent�o � uma mensagem normal
					PrintStream ack = new PrintStream(conexaoSaida.getOutputStream()); //criei outro PrintStream para n�o ficar confuso
					ack.println("5"); //mandar ack de mensagem recebida
					ReceiveMsg.mensagens.add(mensagem); //a mensagem � adiciona ao vetor da thread
					MensagensServidor.mensagens.add(String.valueOf(mensagem.charAt(0))+String.valueOf(mensagem.charAt(1))+nome+": "+mensagem.substring(2, mensagem.length())); //server armazena a mensagem sem a flag e com o nome do cliente
					System.out.println("Mensagem recebida: " + ReceiveMsg.mensagens.lastElement());
					//saida.println(mensagem);
					sendMessage(saida, nome  + ": " , mensagem); //chama o m�todo que vai mandar mensagem apenas para o outro cliente
					cont++;
				}
				saida.flush();
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
				// obt�m o fluxo de sa�da de um dos CLIENTES
				PrintStream outroCLiente = (PrintStream) e.nextElement();
				// envia para todos, menos para o pr�prio usu�rio
				if (outroCLiente != saida) { //se n�o for o PrintStream do usu�rio atual significa que � o outro, ent�o manda para ele a mensagem
					String flag = String.valueOf(mensagem.charAt(0));
					String flagCliente = String.valueOf(mensagem.charAt(1));
					String dadoMensagem = mensagem.substring(2, mensagem.length()); //separar mensagem da flag
					outroCLiente.println(flag + flagCliente + autor + dadoMensagem);
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

