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

    public UserConnection(Socket socket){
        this.userSocket = socket;
        sa = userSocket.getLocalSocketAddress();
    }

    @Override
    public void run(){
        try {
            receive();
            br = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            ois = new ObjectInputStream(userSocket.getInputStream());
            String temp;
            while((temp = br.readLine()) != null){ //might have to change this and the br.readLine() like in example
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

    public void transfer(File file){
        try {
            DataInputStream dis = new DataInputStream(userSocket.getInputStream());
            String temp = dis.readUTF();
            OutputStream os = new FileOutputStream(file + "/" + temp);

            int bytes;
            long size = dis.readLong();
            byte[] arr = new byte[100000000];
            while (size > 0 && (bytes = dis.read(arr, 0, (int) Math.min(arr.length, size))) > -1) {
                os.write(arr, 0, bytes);
                size -= bytes;
            }
            os.close();
        } catch(IOException e) {
            e.printStackTrace();
        }


    }

    public void receive() throws IOException{
        ps = new PrintStream(userSocket.getOutputStream());
        ps.println(this.serverFiles.getName());
    }
}