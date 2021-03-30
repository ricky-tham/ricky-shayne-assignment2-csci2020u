package sample;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class FileServer{
    private static ServerSocket serverSocket = null;
    public static int socketPort = 49351;

    /*
     * Constructor for a new FileServer
     * @param port    the port number being used for the server connection
     */
    public FileServer(int port){
        try{
            this.serverSocket = new ServerSocket(port);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    //regular main method
    public static void main(String[] args) throws IOException{
        FileServer s = new FileServer(socketPort);
        while(true){
            try{
                Socket userSocket = serverSocket.accept();
                Thread thread = new Thread(new UserConnection(userSocket));
                thread.start();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}