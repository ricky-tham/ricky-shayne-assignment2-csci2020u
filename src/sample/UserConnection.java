package sample;
import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class UserConnection implements Runnable{
    private Socket userSocket;
    private SocketAddress sa;
    private File serverFiles = new File("serverFiles/");
    private File userFiles = new File("userFiles/");

    private BufferedReader br = null;
    private ObjectInputStream ois;
    private static PrintStream ps;

    /*
     * Constructor for the user connection client
     * @param socket    socket used for user connection
     */
    public UserConnection(Socket socket){
        this.userSocket = socket;
        sa = userSocket.getLocalSocketAddress();
    }

    /*
     * Reads through each line of the object stream and calls transfer
     * Calls userFiles transfer when word is download
     * Calls serverFiles transfer when word is upload
     */
    @Override
    public void run(){
        try {
            receive();
            br = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            ois = new ObjectInputStream(userSocket.getInputStream());
            String temp;
            //had to change to inline while readLine()
            while((temp = br.readLine()) != null){
                if(temp.equals("download")){
                    transfer(userFiles);
                }
                if(temp.equals("upload")){
                    transfer(serverFiles);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * The transfer method that writes a file to the folder chosen
     * Writes based on byte size
     * @param file   the directory wanted to transfer from
     */
    public void transfer(File file){
        try {
            DataInputStream dis = new DataInputStream(userSocket.getInputStream());
            String temp = dis.readUTF();
            OutputStream os = new FileOutputStream(file + "/" + temp);

            int bytes;
            long size = dis.readLong();
            byte[] arr = new byte[100000000];
            //checks byte size when transfering and subtracts from total bytes
            while (size > 0 && (bytes = dis.read(arr, 0, (int) Math.min(arr.length, size))) > -1) {
                os.write(arr, 0, bytes);
                size -= bytes;
            }
            os.close();
        } catch(IOException e) {
            e.printStackTrace();
        }


    }

    /*
     * Opens and returns a fileName to the PrintStream
     * So it can be displayed in the new list on the ui
     */
    public void receive() throws IOException{
        ps = new PrintStream(userSocket.getOutputStream());
        ps.println(this.serverFiles.getName());
    }
}