package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import javafx.application.Application;
import javafx.collections.FXCollections;import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart; import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.FormatStringConverter;
import sample.dataController;
import sample.SerialPortService;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.List;
import javafx.scene.Node;

import java.net.MalformedURLException;

public class Main extends Application {

    public static void main(String[] args){ launch(args);}

    public static TableView<XYChart.Data<Number, Number>> getTableView() {
        var table = new TableView<XYChart.Data<Number, Number>> ();
        var time = new TableColumn<XYChart.Data<Number, Number>,Number>("X");
        time.setCellValueFactory(row -> row.getValue().XValueProperty());
        var dataFormat = DateFormat.getTimeInstance();
        var converter = new FormatStringConverter<Number>(dataFormat);
        time.setCellFactory(column -> new TextFieldTableCell<>(converter));
        var value = new TableColumn<XYChart.Data<Number,Number>,Number>("Y");
        value.setCellValueFactory(row -> row.getValue().YValueProperty());
        table.getColumns().setAll(List.of(time,value));
        return table;
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
    var controller = new dataController(OutputStream.nullOutputStream());
    long timeStart = System.currentTimeMillis();



    var sp = SerialPortService.getSerialPort("COM3");
    var outputStream = sp.getOutputStream();
    sp.addDataListener(controller);

    //table
        var table = getTableView();
        table.setItems(controller.getDataPoints());
        var vbox = new VBox(table);

        //title
        primaryStage.setTitle("Automatic Watering Pump");

        //pane
        var pane = new BorderPane();

        //creates the axis
        var xAxis = new NumberAxis("time elapsed (Seconds)", timeStart, timeStart + 100000, 10000);
        var yAxis = new NumberAxis("Voltage", 200, 900, 50);

        // create the y axis
        var series = new XYChart.Series<>(controller.getDataPoints());

        // create the series
        var lineChart = new LineChart<>(xAxis, yAxis, FXCollections.singletonObservableList(series));

        // creates the chart
        lineChart.setTitle("Voltage from moisture sensor");


        //button
        var button = new Button("Pump");

        button.setOnMousePressed(value -> {
            try {
                outputStream.write ( 255 );

            } catch (IOException e) {
                e.printStackTrace ();
            }
            button.setText ( "Pumping" );
        });
        button.setOnMouseReleased(value -> {
            try{
                outputStream.write(1);


            }catch(IOException e){
                e.printStackTrace();
            }

            button.setText("Pump");

        });

        //slider and label box
        var slider = new Slider();
        slider.setMin(0.0);
        slider.setMax(100.0);
        var label = new Label();
        var hbox = new HBox(slider, label);
        hbox.setSpacing(100.0);
        label.setText("Initiate the Buzzer state");
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {

            if (newValue.intValue () >= 50) {
                String sliderOn = "on";

                try {
                    label.setText ( "Buzzer State: " + sliderOn );
                    outputStream.write ( 100 );
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            } else if (newValue.intValue () < 50) {
                String sliderOff = "OFF";
                label.setText ( "Buzzer State: " + sliderOff );
                try {
                    outputStream.write ( 2 );
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            } else {
                System.out.println ( "Something went wrong" );
            }

        });
        pane.setLeft(lineChart);
        pane.setPadding(new Insets(0,50,0,50));
        pane.setTop(hbox);
        pane.setBottom(button);
        pane.setCenter(table);

    var scene = new Scene(pane,750,500) ;
    primaryStage.setScene(scene);
    primaryStage.show();
    }

}








