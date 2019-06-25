import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;

public class Teste {

	public static void main(String args[]) throws IOException {
		int port = 8888;// Porta do servidor
		String address = "G2C15";// host do servidor

		try {
			Socket socket = new Socket(address, port);
			char flag = '0';
			Scanner in = new Scanner(System.in);
			String clientMsg;
			DataOutputStream outputStream;
			Mensagens mensagens = new Mensagens();
			receiveThread rt = new receiveThread();
			rt.start();

			while (true) {
				clientMsg = in.nextLine();
				if (clientMsg.equals("quero apagar")) {
					flag = '2';
					int posicao = in.nextInt();
				}
				outputStream = new DataOutputStream(socket.getOutputStream());
				outputStream.writeUTF(flag + clientMsg);
				outputStream.flush();
				for (int i = 0; i < mensagens.mensagens.size(); i++) {
					System.out.println(mensagens.mensagens.get(i));
				}
				flag = '0';
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class receiveThread extends Thread {

	public receiveThread() {
	}

	public void run() {

		try {
			String serverMsg;
			int port = 8421;// Porta do cliente
			String address = "G2C15";// host do servidor

			DataInputStream inputStream;
			Socket socket = new Socket(address, port);

			while (true) {
				inputStream = new DataInputStream(socket.getInputStream());
				serverMsg = inputStream.readUTF();
				serverMsg.trim();
				if (serverMsg.contains("apagar")) {
					for (int i=0;i<50;i++) System.out.println();
					Mensagens.mensagens.remove(parseInteger(serverMsg.substring(6, serverMsg.length())));
				}
				else {
					Mensagens.mensagens.addElement(serverMsg);
					System.out.println(serverMsg);
					ackThread at = new ackThread();
					at.start();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Object parseInteger(String substring) {
		// TODO Auto-generated method stub
		return null;
	}
}

class ackThread extends Thread {

	public ackThread() {
	}

	public void run() {

		try {
			DataOutputStream outputStream;
			int port = 8888;// Porta do servidor
			String address = "G2C15";// host do servidor

			Socket socket = new Socket(address, port);

			outputStream = new DataOutputStream(socket.getOutputStream());
			outputStream.writeUTF("1");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class Mensagens {
	public static Vector<String> mensagens;

	public Mensagens() {
		this.mensagens = new Vector<String>();
	}
}
