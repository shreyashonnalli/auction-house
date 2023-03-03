import java.io.IOException;
import java.util.LinkedList;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
	private LinkedList<String> auctionItems = new LinkedList<String>();
	private LinkedList<Double> auctionItemPrices = new LinkedList<Double>();
	private LinkedList<InetAddress> auctionItemHighestBidders = new LinkedList<InetAddress>();
	private Socket clientSocket;


	//opens thread pool of 15 connections and listens on port 6505
	public Server(){
		ServerSocket serverSocket = null;

		try{
			serverSocket = new ServerSocket(6505);
		}
		catch (IOException e){
			System.exit(-1);
		}


		ExecutorService executorService = null;
		executorService = Executors.newFixedThreadPool(15);

		while (true) {
			try{
				clientSocket = serverSocket.accept();
				executorService.submit(new ClientHandler(this));
			}
			catch (IOException e){
				System.exit(-1);
			}
		}


	}

	
	//getters and setters for server variables
	public LinkedList<String> getAuctionItems() {
		return auctionItems;
	}

	public LinkedList<Double> getAuctionItemPrices() {
		return auctionItemPrices;
	}

	public LinkedList<InetAddress> getAuctionItemHighestBidders() {
		return auctionItemHighestBidders;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setAuctionItems(LinkedList<String> auctionItems) {
		this.auctionItems = auctionItems;
	}

	public void setAuctionItemPrices(LinkedList<Double> auctionItemPrices) {
		this.auctionItemPrices = auctionItemPrices;
	}

	public void setAuctionItemHighestBidders(LinkedList<InetAddress> auctionItemHighestBidders) {
		this.auctionItemHighestBidders = auctionItemHighestBidders;
	}

	public static void main(String[] args) {
		Server server = new Server();
	}
}