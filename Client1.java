import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.Scanner;

public class Client1 {
    public static void main(String args[]) throws IOException {
    	int port = 8888;//Porta do servidor
        String address = "localhost";//host do servidor
        
        try {
            Socket socket = new Socket(address, port);
            Scanner in = new Scanner(System.in);
            String clientMsg;
            DataOutputStream outputStream;
            
            receiveThread rt = new receiveThread();
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

 class receiveThread extends Thread {

    public receiveThread(){}

    public void run(){
    	
        try{
            String serverMsg;
        	int port = 1248;//Porta do cliente
            String address = "localhost";//host do cliente
            
            DataInputStream inputStream;
            Socket socket = new Socket(address, port);
            
            while (true){
            	inputStream = new DataInputStream(socket.getInputStream());
            	serverMsg = inputStream.readUTF();
            	System.out.println(serverMsg);
                ackThread at = new ackThread();
            	at.start();
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
}
 
class ackThread extends Thread {

	    public ackThread(){}

	    public void run(){
	    	
	        try{
	            DataOutputStream outputStream;
	        	int port = 8888;//Porta do servidor
	            String address = "localhost";//host do servidor
	            
	            Socket socket = new Socket(address, port);
	            
	            outputStream = new DataOutputStream(socket.getOutputStream());
	            outputStream.writeUTF("1");
	         
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        
	    }
}