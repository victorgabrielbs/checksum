package com.kldv;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private Text txtFileName;

    @FXML
    private Text txtResultChecksum;

    @FXML
    private Text txtComparisonResult;

    @FXML
    private TextField receivedSum;

    @FXML
    private RadioButton firstRadioButton;

    @FXML
    private RadioButton secondRadioButton;

    private File selectedFile;
    private String resultChecksum;
    private Preferences userPreferences;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) txtFileName.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                executorService.shutdown();
            });
        });
        LoggerConfig.configureLogger();
        firstRadioButton.setDisable(false);
        secondRadioButton.setDisable(false);
        userPreferences = Preferences.userNodeForPackage(getClass());
    }

    @FXML
    private boolean selectFile(ActionEvent event) {
        try {

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar a ISO");

            String lastSelectedDirectory = userPreferences.get("lastSelectedDirectory",
                    System.getProperty("user.home"));
            fileChooser.setInitialDirectory(new File(lastSelectedDirectory));
            selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile == null) {
                txtFileName.setText("Selecione a ISO.");
                return false;
            }

            txtFileName.setText(selectedFile.getName());
            userPreferences.put("lastSelectedDirectory", selectedFile.getParent());

        } catch (Exception e) {
            txtFileName.setText("Aconteceu um problema, tente denovo");
            LoggerConfig.logger.log(Level.SEVERE, "An error occurred in selectFile() method", e);
        }
        return true;
    }

    @FXML
    private boolean checkSum(ActionEvent event) {
        try {

            if (selectedFile == null) {
                return false;
            }

            if (!selectedFile.exists()) {
                txtFileName.setText("O arquivo não existe mais, tente denovo");
                return false;
            }
            if (!firstRadioButton.isSelected() && !secondRadioButton.isSelected()) {
                txtResultChecksum.setText("Marque se você quer SHA256 ou MD5");
                return false;
            }

            String algorithm = firstRadioButton.isSelected() ? "SHA-256" : "MD5";

            firstRadioButton.setDisable(true);
            secondRadioButton.setDisable(true);

            executorService.submit(() -> {
                try {
                    String checksum = Calculate.calculateSum(selectedFile, algorithm);
                    Platform.runLater(() -> {
                        resultChecksum = checksum;
                        txtResultChecksum.setText(resultChecksum);
                    });
                    firstRadioButton.setDisable(false);
                    secondRadioButton.setDisable(false);
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                    LoggerConfig.logger.log(Level.SEVERE,
                            "An error occurred in the Method checkSum(), executorService.submit()", e);
                    Platform.runLater(() -> txtResultChecksum.setText("Aconteceu algum problema, tente denovo"));
                }

            });

        } catch (Exception e) {
            txtResultChecksum.setText("Aconteceu um problema, tente denovo");
            LoggerConfig.logger.log(Level.SEVERE, "An error occurred in checkSum() method", e);
        }
        return true;
    }

    @FXML
    private boolean compareChecksum(ActionEvent event) {
        try {
            if (receivedSum == null) {
                return false;
            }
            if (receivedSum.getText().isEmpty()) {
                return false;
            }
            if (resultChecksum == null) {
                return false;
            }
            txtComparisonResult.setText(
                    resultChecksum.equalsIgnoreCase(receivedSum.getText()) ? "É igual" : "Não é igual");

        } catch (Exception e) {
            receivedSum.setText("Aconteceu um problema, tente denovo");
            LoggerConfig.logger.log(Level.SEVERE, "An error occurred in compareChecksum() method", e);
        }
        return true;
    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        if (receivedSum.isFocused()) {
            receivedSum.getParent().requestFocus();
        }
    }

    @FXML
    private void firstSelectAlgorithm(ActionEvent event) {
        secondRadioButton.setSelected(false);
    }

    @FXML
    private void secondSelectAlgorithm(ActionEvent event) {
        firstRadioButton.setSelected(false);
    }

}
