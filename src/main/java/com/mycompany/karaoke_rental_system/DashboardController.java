/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Neil
 */
public class DashboardController implements Initializable {

    @FXML
    private Label date_lbl;

    @FXML
    private ListView<?> karaoke_availability_listview;

    @FXML
    private LineChart<?, ?> lineChart;

    @FXML
    private ListView<?> money_listview;

    @FXML
    private Label total_money_lbl;

    @FXML
    private Label total_rents_lbl;

    @FXML
    private Text username_lbl;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
