package sample;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private TextField tfId;
    @FXML
    private TextField tfTitle;
    @FXML
    private TextField tfAuthor;
    @FXML
    private TextField tfYear;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnDel;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnLoad;
    @FXML
    private Button btnSave;

    @FXML
    private TableColumn<Book, Integer> tcId;
    @FXML
    private TableColumn<Book, String> tcTitle;
    @FXML
    private TableColumn<Book, String> tcAuthor;
    @FXML
    private TableColumn<Book, Integer> tcYear;
    @FXML
    private TableView<Book> tvBooks;

    private ObservableList<Book> dataBooks= FXCollections.observableArrayList();
    private Book selected;

    int globId = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tcId.setCellValueFactory(new PropertyValueFactory<Book, Integer>("id"));
        tcTitle.setCellValueFactory(new PropertyValueFactory<Book, String>("title"));
        tcAuthor.setCellValueFactory(new PropertyValueFactory<Book, String>("author"));
        tcYear.setCellValueFactory(new PropertyValueFactory<Book, Integer>("year"));
        tvBooks.setItems(dataBooks);

        TableView.TableViewSelectionModel<Book> model=tvBooks.getSelectionModel();
        ObservableList<Book> selectedBooks =model.getSelectedItems();
        selectedBooks.addListener(new ListChangeListener<Book>() {
            @Override
            public void onChanged(Change<? extends Book> c) {
                try {
                    tfId.setText(c.getList().get(0).getId().toString());
                    tfTitle.setText(c.getList().get(0).getTitle());
                    tfAuthor.setText(c.getList().get(0).getAuthor());
                    tfYear.setText(c.getList().get(0).getYear().toString());
                    selected = c.getList().get(0);
                }
                catch (java.lang.IndexOutOfBoundsException ex){

                }
            }
        });
    }

    @FXML
    private void onDeleteClick(){
        if (selected == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("You need to select an entry");
            alert.showAndWait();
        }
        dataBooks.remove(selected);
    }

    @FXML
    private void onUpdateClick(){
        for (Book b: dataBooks){
            if (b.equals(selected)){
                b.setAuthor(tfAuthor.getText());
                b.setTitle(tfTitle.getText());
                b.setYear(Integer.parseInt(tfYear.getText()));
            }
        }
        tvBooks.refresh();
    }

    @FXML
    private void onAddClick(){
        try {
            if (tfTitle.getText() == "" || tfAuthor.getText() == "") {
                throw new java.lang.NumberFormatException();
            }
            else {
                dataBooks.add(new Book(globId++, tfTitle.getText(), tfAuthor.getText(), Integer.parseInt(tfYear.getText())));
                tfId.clear();
                tfTitle.clear();
                tfAuthor.clear();
                tfYear.clear();
            }
        }
        catch(java.lang.NumberFormatException ex){
            Alert alert= new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText(null);
            alert.setContentText("You must enter all the data!\nClick OK to continue:");
            alert.showAndWait();
        }
    }

    @FXML
    public void onLoadClick(){

        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File data = fc.showOpenDialog(new Stage());

        JSONParser parser = new JSONParser();
        if (data == null){
        }
        else {
            try {
                Object obj = parser.parse(new FileReader(data));
                JSONArray books = (JSONArray) obj;
                Iterator bookIter = books.iterator();
                while (bookIter.hasNext()) {
                    JSONObject book = (JSONObject) bookIter.next();
                    String title = book.get("title").toString();
                    String author = book.get("author").toString();
                    Integer year = Integer.parseInt(book.get("year").toString());
                    dataBooks.add(new Book(globId++, title, author, year));
                }
            } catch (RuntimeException | ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onSaveClick(){
        JSONArray allBooks = new JSONArray();
        if (dataBooks.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("There is nothing to save");
            alert.showAndWait();
        }
        else {
            for (Book b : dataBooks) {
                JSONObject book = new JSONObject();
                book.put("id", b.getId());
                book.put("title", b.getTitle());
                book.put("author", b.getAuthor());
                book.put("year", b.getYear());
                allBooks.add(book);
            }

            Stage stage = new Stage();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.setTitle("Select directory for save");
            fileChooser.setInitialFileName("Books");

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            File file = fileChooser.showSaveDialog(stage);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(allBooks.toString());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Saved");
                alert.setHeaderText(null);
                alert.setContentText("The information was saved");
                alert.showAndWait();
            } catch (IOException | RuntimeException e) {
            }
        }
    }
}
