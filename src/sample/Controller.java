package sample;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ListView;
import java.io.*;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.net.Socket;

public class Controller {


    private static String serverAddress = "localhost";
    public static int socketPort = 49351;
    private Socket socket;
    private File sFiles = new File("serverFiles/");
    private File uFiles = new File("userFiles/");

    @FXML
    private GridPane gridPane;
    @FXML
    private ListView<String> userFileList;
    @FXML
    private ListView<String> serverFileList;

    public void initialize() throws IOException{
        userFileList.setItems(FXCollections.observableArrayList(uFiles.list()));
        serverFileList.setItems(FXCollections.observableArrayList(sFiles.list()));
        try{
            socket = new Socket(serverAddress, socketPort);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    //refreshes client to show new files after upload/download
    public void refresh(){
        Stage prevStage = (Stage) gridPane.getScene().getWindow();
        prevStage.hide();
        try{
            //need to make this
            //could recreate the scene using root and parent
            //can do this in a similar way to the scene change in our midterm assignment
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            Parent root1 = (Parent) loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void transfer(String file){
        try{
            File newFile = new File(file);
            byte[] arr = new byte[(int) newFile.length()];

            FileInputStream fis = new FileInputStream(newFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(arr, 0 , arr.length);

            OutputStream os = socket.getOutputStream();

            //transfer
            System.out.println(newFile.getName());
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(newFile.getName());
            dos.writeLong(arr.length);
            dos.write(arr, 0, arr.length);
            dos.flush();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void download(ActionEvent e) throws IOException{
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        String selected = serverFileList.getSelectionModel().getSelectedItem();
        String path = sFiles + "/" + selected;
        ps.println("download");
        transfer(path);
        userFileList.setItems(null);
        userFileList.setItems(FXCollections.observableArrayList(uFiles.list()));
        refresh();
    }

    @FXML
    public void upload(ActionEvent e) throws IOException{
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        String selected = userFileList.getSelectionModel().getSelectedItem();
        String path = uFiles + "/" + selected;
        ps.println("upload");
        transfer(path);
        serverFileList.setItems(null);
        serverFileList.setItems(FXCollections.observableArrayList(sFiles.list()));
        refresh();
    }
}