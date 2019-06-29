import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
	public static Vector<String> mensagens = new Vector<String>();
	private Socket conexao;
	private static Vector CLIENTES;
	
	public Server(Socket socket)
	{
		this.conexao = socket;
	}
	
	public static void main(String[] args) {
		int port = 8888;
		try {
			ServerSocket tmpSocketIn = new ServerSocket(port);
			ServerSocket tmpSocketOut = new ServerSocket(8421);
			
			while (true) {
				System.out.println("Aguardando cliente");
				Socket socketIn = tmpSocketIn.accept();
				Socket socketOut = tmpSocketOut.accept();
				DataInputStream inputStream;
				DataOutputStream outputStream;
				String strData, ans;
				/*inputStream = new DataInputStream(socketIn.getInputStream());
				strData = inputStream.readUTF();
				strData = strData.trim();
				
				System.out.println("Texto recebido do cliente: " + strData);
				if (strData.charAt(0) == 1) { //1 -> ack p/ cliente
					ans = "ack";
				} else if (strData.charAt(0) == 2) { //2 -> apagar
					ans = "apagar" + strData.substring(1, strData.length());
				} else if (strData.contains("InfraCom")) {
					ans = "IF678, a disciplina de InfraEstrutura de Comunicacao ministrada pelo professor Paulo Goncalves ";
				} else if (strData.contains("Ola") || strData.contains("Oi")) {
					ans = "Ola, como esta voce?s";
				} else if (strData.contains("Tudo bem?")) {
					ans = "Tudo, e com voce?";
				} else if (strData.contains("Adriano")) {
					ans = "O melhor monitor!";
				} else {
					ans = "Mensagem recebida!";
				}
				*/
				ReceiveMsg t = new ReceiveMsg(socketIn);
				t.start();
				PrintStream saida = new PrintStream(socketOut.getOutputStream());
				//outputStream = new DataOutputStream(socketOut.getOutputStream());
				saida.println("Recebi");
				System.out.println("Enviado");
				
			}

		} catch (BindException e) {
			System.out.println("Endereco em uso");
		} catch (Exception e) {
			System.out.println("Erro" + e);
		}

	}
	
	/*public void run(Socket socketClient)
	{
		try
		{
			BufferedReader entrada = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
			PrintStream saida = new PrintStream(this.conexao.getOutputStream());
			saida.println("Conectado");
			mensagens.add(entrada.readLine());
			System.out.println(entrada.readLine());
			System.out.println(mensagens.get(0));
			
			
			
			DataInputStream inputStream;
			String strData;
			inputStream = new DataInputStream(socketClient.getInputStream());
			strData = inputStream.readUTF();
			strData = strData.trim();
			mensagens.add(strData);
			System.out.println(mensagens.get(0));
			System.out.println(strData);
			
		}
		catch(Exception e)
		{
			System.out.println("Deu erro!");
		}
	}*/

}

class ReceiveMsg extends Thread
{
	public static Vector<String> mensagens = new Vector<String>();
	public static Vector<String> usuarios = new Vector<String>();
	
	private Socket conexao;
	
	public ReceiveMsg(Socket conexao)
	{
		this.conexao = conexao;
	}
	
	public void run()
	{
		try
		{
			BufferedReader entrada = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
			PrintStream saida = new PrintStream(this.conexao.getOutputStream());

			saida.println("Conectado");
			String nome = entrada.readLine();
			usuarios.add(nome);
			String mensagem = "";
			mensagem = entrada.readLine();
			int cont = 0;
			while(mensagem != null)
			{
				ackThreadS ack = new ackThreadS();
				ack.start();
				if (mensagem.charAt(0) == '3') {
					mensagens.remove(Integer.parseInt(mensagem.substring(1, mensagem.length())));
					cont--;
				}
				else {
					mensagens.add(mensagem);
					System.out.println(mensagens.get(cont));
				}
				mensagem = entrada.readLine();
				cont++;
			}
			System.out.println("Sai da conversa");
		}
		catch(Exception e)
		{
			System.out.println("Deu erro!");
		}
	}

}
class ackThreadS extends Thread {

	public ackThreadS() {
	}

	public void run() {

		try {
			DataOutputStream outputStream;
			int port = 8888;// Porta do servidor
			String address = "localhost";// host do servidor

			Socket socket = new Socket(address, port);

			outputStream = new DataOutputStream(socket.getOutputStream());
			outputStream.writeUTF("1");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
	