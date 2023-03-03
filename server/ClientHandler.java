import java.net.*;
import java.util.LinkedList;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ClientHandler extends Thread
{
    private Socket clientSocket = null;
    private LinkedList<String> auctionItems = null;
    private LinkedList<Double> auctionItemPrices = null;
    private LinkedList<InetAddress> auctionItemHighestBidders = null;
    private Server server = null;

    public ClientHandler(Server server){
        this.clientSocket = server.getClientSocket();
        this.auctionItems = server.getAuctionItems();
        this.auctionItemPrices = server.getAuctionItemPrices();
        this.auctionItemHighestBidders = server.getAuctionItemHighestBidders();
        this.server = server;
    }


    //this code is run when thread is activated. Each thread will handle a separate client and communicate with it
    public void run(){
        try{
            //System.out.println("Connection made from " + clientSocket.getInetAddress().getHostName() );
            PrintWriter writeToClientSocket = new PrintWriter(clientSocket.getOutputStream(), true);
            ObjectInputStream readFromClientSocket = new ObjectInputStream(clientSocket.getInputStream());
            String[] clientArguments = (String[]) readFromClientSocket.readObject();
            String serverOutput = clientProtocol(clientArguments);
            updateServer();
            writeToClientSocket.write(serverOutput);
            writeToClientSocket.close();
            readFromClientSocket.close();
            //Thread.sleep(7000);
            //System.out.println("Connection from " + clientSocket.getInetAddress().getHostName() + "Has ended.");
            clientSocket.close();
        }
        catch (IOException e){
            System.err.println("Check below to see error");
            e.printStackTrace();
        }
        catch (ClassNotFoundException e){
            System.err.println("Error while reading object from socket");
        }
        /*catch (InterruptedException e){
            e.printStackTrace();
        }*/
    }


    //protocol and redirects based on client request
    private String clientProtocol(String[] clientArguments){
        String serverOutput = null;
        if(clientArguments[0].equals("show")){
            serverOutput = showProtocol();
        }
        else if(clientArguments[0].equals("item")){
            serverOutput = itemProtocol(clientArguments[1]);
        }
        else if(clientArguments[0].equals("bid")){
            serverOutput = bidProtocol(clientArguments);
        }
        textFileWriter(clientArguments, clientSocket.getInetAddress());
        return serverOutput;
    }


    // if client requests to display auction, do this
    private String showProtocol(){
        if (auctionItems.size() == 0) return "There are currently no items in this auction.";
        String serverOutput = "";
        for(int i=0;i<auctionItems.size();i++){
            serverOutput = serverOutput + (auctionItems.get(i) + ":" + Double.toString(auctionItemPrices.get(i)) + ":");
            if(auctionItemHighestBidders.get(i) == null){
                serverOutput = serverOutput + ("<No Bids>" + "\n");
            }
            else{
                serverOutput = serverOutput + (auctionItemHighestBidders.get(i).getHostAddress() + "\n");
            }

        }
        return serverOutput;
    }


    //if client requests to add item do this
    private String itemProtocol(String item){
        if(doesItemAlreadyExist(item)){
            return "Failure.";
        }
        else{
            addNewItem(item);
            return "Success.";
        }
    }


    //if client requests to bid do this
    private String bidProtocol(String[] clientArguments){
        if(!doesItemAlreadyExist(clientArguments[1]) || Double.parseDouble(clientArguments[2]) < 0) return "Failure.";
        for(int i=0;i<auctionItems.size();i++){
            if(clientArguments[1].equals(auctionItems.get(i))){
                if(auctionItemPrices.get(i) >= Double.parseDouble(clientArguments[2])){
                    return "Rejected.";
                }
                else{
                    updateBidAndBidder(i, Double.parseDouble(clientArguments[2]));
                    return "Accepted.";
                }
            }
        }
        return "Unknown Failure while bidding.";
    }


    //supporting function for bid
    private void updateBidAndBidder(int linkedListPosition, Double updatedPrice){
        auctionItemPrices.set(linkedListPosition, updatedPrice);
        auctionItemHighestBidders.set(linkedListPosition, clientSocket.getInetAddress());
    }


    //adds new item to data structure
    private void addNewItem(String item){
        auctionItems.add(item);
        auctionItemPrices.add(0.0);
        auctionItemHighestBidders.add(null);
    }


    //checks if item already exists in database
    private boolean doesItemAlreadyExist(String item){
        for(int i=0;i<auctionItems.size();i++){
            if(item.equals(auctionItems.get(i))){
                return true;
            }
        }
        return false;
    }


    //sends back updated data structure back to server
    private void updateServer(){
        server.setAuctionItems(auctionItems);
        server.setAuctionItemPrices(auctionItemPrices);
        server.setAuctionItemHighestBidders(auctionItemHighestBidders);
    }


    private String inputArgsToString(String[] inputArgs){
        String output = "";
        for(int i=0;i<inputArgs.length;i++){
            if(i == inputArgs.length-1){
                output = output + inputArgs[i] + "\n";
                break;
            }
            output = inputArgs[i] + " ";
        }
        return output;
    }

    //logs every request to txt file
    private void textFileWriter(String[] inputArgs, InetAddress clientAddress){

        File logFile = new File("log.txt");


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy|HH:mm:ss|");
        String input = dtf.format(LocalDateTime.now()) + clientAddress.getHostAddress() + "|" + inputArgsToString(inputArgs);


        try {
            FileWriter fileWriter = new FileWriter("log.txt", true);
            fileWriter.write(input);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("An error occurred writing to file.");
        }
    }
}
