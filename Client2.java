import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.Scanner;

public class Client2 {
    public static void main(String args[]) throws IOException {
    	int port = 8888;//Porta do servidor
        String address = "localhost";//host do servidor
        
        try {
            Socket socket = new Socket(address, port);
            Scanner in = new Scanner(System.in);
            String clientMsg;
            DataOutputStream outputStream;
            
            receiveThread2 rt = new receiveThread2();
            rt.start();

            
            while(true){
            	clientMsg = in.nextLine();
            	
            	outputStream = new DataOutputStream(socket.getOutputStream());
            	outputStream.writeUTF(clientMsg);    
            }
            
        }catch(Exception e) {
            e.printStackTrace();
        }
        

    }
}

 class receiveThread2 extends Thread {

    public receiveThread2(){}

    public void run(){
    	
        try{
            String serverMsg;
        	int port = 8421;//Porta do cliente
            String address = "localhost";//host do cliente
            
            DataInputStream inputStream;
            Socket socket = new Socket(address, port);
            
            while (true){
            	inputStream = new DataInputStream(socket.getInputStream());
            	serverMsg = inputStream.readUTF(); 
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
}