package org.example.automotivedatabase;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.sql.*;
import java.util.ArrayList;

public class HelloController {

    @FXML
    private ComboBox<Integer> yearComboBox;

    @FXML
    private ComboBox<String> makeComboBox;

    @FXML
    private ComboBox<String> modelComboBox;

    @FXML
    private Label highwayMpgLabel;

    @FXML
    private Label cityMpgLabel;

    private static Connection connection;

    public HelloController() {
        initDatabaseConnection();
    }

    @FXML
    public void initialize() {
        ArrayList<Integer> years = fetchYears();
        yearComboBox.getItems().addAll(years);

        yearComboBox.setOnAction(event -> {
            Integer selectedYear = yearComboBox.getValue();
            if (selectedYear != null) {
                ArrayList<String> makes = fetchMakes(selectedYear);
                makeComboBox.getItems().clear();
                makeComboBox.getItems().addAll(makes);
            }
        });

        makeComboBox.setOnAction(event -> {
            String selectedMake = makeComboBox.getValue();
            if (selectedMake != null) {
                Integer selectedYear = yearComboBox.getValue();
                if (selectedYear != null) {
                    ArrayList<String> models = fetchModels(selectedYear, selectedMake);
                    modelComboBox.getItems().clear();
                    modelComboBox.getItems().addAll(models);
                }
            }
        });

        modelComboBox.setOnAction(event -> {
            String selectedModel = modelComboBox.getValue();
            if (selectedModel != null) {
                Integer selectedYear = yearComboBox.getValue();
                String selectedMake = makeComboBox.getValue();
                if (selectedYear != null && selectedMake != null) {
                    fetchMpg(selectedYear, selectedMake, selectedModel);
                }
            }
        });
    }

    private ArrayList<Integer> fetchYears() {
        ArrayList<Integer> years = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT year FROM vehicles ORDER BY year DESC;")) {
            while (rs.next()) {
                int year = rs.getInt("year");
                years.add(year);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return years;
    }

    private ArrayList<String> fetchMakes(int year) {
        ArrayList<String> makes = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT make FROM vehicles WHERE year = ? ORDER BY make ASC;")) {
            pstmt.setInt(1, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String make = rs.getString("make");
                    makes.add(make);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return makes;
    }

    private ArrayList<String> fetchModels(int year, String make) {
        ArrayList<String> models = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT model FROM vehicles WHERE year = ? AND make = ? ORDER BY model ASC;")) {
            pstmt.setInt(1, year);
            pstmt.setString(2, make);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String model = rs.getString("model");
                    models.add(model);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return models;
    }

    private void fetchMpg(int year, String make, String model) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT highway08, city08 FROM vehicles WHERE year = ? AND make = ? AND model = ?;")) {
            pstmt.setInt(1, year);
            pstmt.setString(2, make);
            pstmt.setString(3, model);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int highwayMpg = rs.getInt("highway08");
                    int cityMpg = rs.getInt("city08");
                    highwayMpgLabel.setText("Highway MPG: " + highwayMpg);
                    cityMpgLabel.setText("City MPG: " + cityMpg);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initDatabaseConnection() {
        String databaseURL = "jdbc:sqLite:C:\\Users\\zanes\\OneDrive\\Desktop\\OBU\\SPRING 24\\DATABASE MGMT\\vehicles.db";
        try {
            connection = DriverManager.getConnection(databaseURL);
            System.out.println("Connected to database...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void closeDatabaseConnection() {
        try {
            if  (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}