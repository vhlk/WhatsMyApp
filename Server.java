import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) {
		int port = 8888;
		try {
			ServerSocket tmpSocketIn = new ServerSocket(port);
			ServerSocket tmpSocketOut = new ServerSocket(8421);
			System.out.println("Aguardando cliente");
			Socket socketIn = tmpSocketIn.accept();
			Socket socketOut = tmpSocketOut.accept();
			DataInputStream inputStream;
			DataOutputStream outputStream;
			String strData, ans;

			while (true) {
				inputStream = new DataInputStream(socketIn.getInputStream());
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
					ans = "Ola, como esta voce?";
				} else if (strData.contains("Tudo bem?")) {
					ans = "Tudo, e com voce?";
				} else if (strData.contains("Adriano")) {
					ans = "O melhor monitor!";
				} else {
					ans = "Mensagem recebida!";
				}
				outputStream = new DataOutputStream(socketOut.getOutputStream());
				outputStream.writeUTF(ans);
				System.out.println("Enviado");
			}

		} catch (BindException e) {
			System.out.println("Endereco em uso");
		} catch (Exception e) {
			System.out.println("Erro" + e);
		}

	}

}
