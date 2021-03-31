package sample;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ListView;
import java.io.*;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

    /*
     * Standard initialize method that fills both lists with the proper directory files
     */
    public void initialize() throws IOException{
        userFileList.setItems(FXCollections.observableArrayList(uFiles.list()));
        serverFileList.setItems(FXCollections.observableArrayList(sFiles.list()));
        //set to multiple
        userFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //track changes to selection for userFileList
        userFileList.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {

                    ObservableList<String> selected = userFileList.getSelectionModel().getSelectedItems();
                    //tracking selected items
                    StringBuilder builder = new StringBuilder("Selected items userFileList:");
                    for (String fileName : selected) {
                        builder.append("\n" + fileName);
                    }
                    System.out.println(builder);
                });

        //track changes to selection for serverFileList
        serverFileList.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {

                    ObservableList<String> selected = serverFileList.getSelectionModel().getSelectedItems();
                    //tracking selected items
                    StringBuilder builder = new StringBuilder("Selected items for serverFileList:");
                    for (String fileName : selected) {
                        builder.append("\n" + fileName);
                    }
                    System.out.println(builder);
                });

        try{
            socket = new Socket(serverAddress, socketPort);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
     * Refreshes client to show new files after upload/download
     * Does this by using the root parent to remake and load the scene
     */
    public void refresh(){
        Stage prevStage = (Stage) gridPane.getScene().getWindow();
        prevStage.hide();
        try{
            //can recreate the scene using root and parent
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            Parent root1 = (Parent) loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setTitle("File Share");
            stage.show();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
     * Gets the data from the file specified
     * Writes data to the new file in the directory
     * @param file   the file name of the file selected to transfer
     */
    public void transfer(List<String> files){
        try{
            //this for loop currently looks at each file in the list and individually transfers, hopefully
            //doesn't work right now I don't know if I need to do something in UserConnection.java
            //problem is that it only transfers one file still
            for (String fileForTransfer : files) {
                File newFile = new File(fileForTransfer);
                System.out.println("File: " + newFile + "\nFrom: " + fileForTransfer);
                byte[] arr = new byte[(int) newFile.length()];
                FileInputStream fis = new FileInputStream(newFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(arr, 0, arr.length);
                OutputStream os = socket.getOutputStream();
                System.out.println(newFile.getName());
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeUTF(newFile.getName());
                dos.writeLong(arr.length);
                dos.write(arr, 0, arr.length);
                dos.flush();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
     * Sets up proper Streams and calls transfer with download
     * Sends download command to server
     * Supplies new files to the userFileList and calls refresh to display them
     * @param e   the download button press
     */
    @FXML
    public void download(ActionEvent e) throws IOException{
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

        //need many selected not using .getSelectedItem but .getSelectedItems
        ObservableList<String> selected = serverFileList.getSelectionModel().getSelectedItems();

        //need many paths, possibly a list?
        List<String> pathsList = new ArrayList<String>();

        //adds paths from ObservableList (selected) to List (pathsList)
        for (String pathDir : selected) {
            pathsList.add(sFiles + "/" + pathDir);
        }

        ps.println("download");

        //need transfer to take a list of strings
        transfer(pathsList);
        userFileList.setItems(null);
        userFileList.setItems(FXCollections.observableArrayList(uFiles.list()));
        refresh();
    }

    /*
     * Sets up proper Streams and calls transfer with upload
     * Sends upload command to server
     * Supplies new files to the serverFileList and calls refresh to display them
     * @param e   the upload button press
     */
    @FXML
    public void upload(ActionEvent e) throws IOException{
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        //need many selected not using .getSelectedItem but .getSelectedItems
        ObservableList<String> selected = userFileList.getSelectionModel().getSelectedItems();

        //need many paths, possibly a list?
        List<String> pathsList = new ArrayList<String>();

        //adds paths from ObservableList (selected) to List (pathsList)
        for (String pathDir : selected) {
            pathsList.add(uFiles + "/" + pathDir);
        }
        ps.println("upload");
        transfer(pathsList);
        serverFileList.setItems(null);
        serverFileList.setItems(FXCollections.observableArrayList(sFiles.list()));
        refresh();
    }
}