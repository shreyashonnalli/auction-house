import java.io.*;
import java.net.Socket;

public class Client
{
	private Socket serverSocket = null;
	private ObjectOutputStream writeToServerSocket = null;
	private BufferedReader readFromServerSocket = null;

	
	//This function communicates with server
	public void attemptServerConnection(String[] args){
		if(erroneousInputChecker(args)){
			printErrorInputMessage();
		}
		openSocketAndBuffers();
		writeToServer(args);
		readFromServer();
	}

	
	//this function checks for improper user inputs
	private boolean erroneousInputChecker(String[] args){
		if(args.length < 1 || args.length >3) return true;
		if(!args[0].equals("show") && !args[0].equals("item") && !args[0].equals("bid")) return true;
		if(args[0].equals("show") && args.length != 1){
			return true;
		}
		if(args[0].equals("item") && args.length != 2){
			return true;
		}
		if(args[0].equals("bid")){
			if (args.length != 3) return true;
			try{
				Double.parseDouble(args[2]);
			}
			catch (NumberFormatException e){
				return true;
			}
		}
		return false;
	}

	
	//opens socket and buffers for socket
	private void openSocketAndBuffers(){
		try{
			serverSocket = new Socket("localhost", 6505);
			readFromServerSocket = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			writeToServerSocket = new ObjectOutputStream(serverSocket.getOutputStream());
		}
		catch (IOException e){
			System.err.println("Error while opening socket or setting up Input/Output Streams");
			System.exit(-1);
		}
	}

	
	//writes data to server
	private void writeToServer(String[] args){
		try{
			writeToServerSocket.writeObject(args);
		}
		catch(IOException e){
			System.err.println("Error while writing to server");
			System.exit(-1);
		}
	}

	
	//reads data from server
	private void readFromServer(){
		try{
			String serverOutput;
			while((serverOutput=readFromServerSocket.readLine())!=null){
				System.out.println(serverOutput);
			}
		}
		catch(IOException e){
			System.err.println("Error while reading from server");
			System.exit(-1);
		}
	}

	
	//prints error message and closes program
	private void printErrorInputMessage(){
		System.out.println("Invalid argument. Valid Arguments include:");
		System.out.println("show");
		System.out.println("item <string>");
		System.out.println("bid <string> <double>");
		System.exit(-1);
	}

	public static void main( String[] args )
	{
		Client client = new Client();
		client.attemptServerConnection(args);
	}
}