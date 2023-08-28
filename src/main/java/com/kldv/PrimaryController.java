package com.kldv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
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
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Preferences userPreferences;

    private static final Logger logger = Logger.getLogger(PrimaryController.class.getName());

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) txtFileName.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                executorService.shutdown();
            });
        });
        configureLogger();
        firstRadioButton.setDisable(false);
        secondRadioButton.setDisable(false);
        userPreferences = Preferences.userNodeForPackage(getClass());
    }

    private void configureLogger() {
        try {
            File logFolder = new File("logs");
            if (!logFolder.exists()) {
                logFolder.mkdir();
            }

            FileHandler fileHandler = new FileHandler("logs/app.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selectFile(ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar a ISO");
            String lastSelectedDirectory = userPreferences.get("lastSelectedDirectory",
                    System.getProperty("user.home"));
            fileChooser.setInitialDirectory(new File(lastSelectedDirectory));
            selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                txtFileName.setText(selectedFile.getName());
                userPreferences.put("lastSelectedDirectory", selectedFile.getParent());
            } else {
                txtFileName.setText("Selecione a ISO.");
            }
        } catch (Exception e) {
            txtFileName.setText("Aconteceu um problema, tente denovo");
            System.err.println("Method selectFile(): " + e + "\n");
            logger.log(Level.SEVERE, "An error occurred in selectFile() method", e);
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

    @FXML
    private void checkSum(ActionEvent event) {
        try {
            if (selectedFile != null && selectedFile.exists()) {
                if (!firstRadioButton.isSelected() && !secondRadioButton.isSelected()) {
                    txtResultChecksum.setText("Marque se você quer SHA256 ou MD5");
                    return;
                }

                String algorithm = firstRadioButton.isSelected() ? "SHA-256" : "MD5";
                firstRadioButton.setDisable(true);
                secondRadioButton.setDisable(true);

                executorService.submit(() -> {
                    try {
                        String checksum = calculateChecksum(selectedFile, algorithm);
                        Platform.runLater(() -> displayChecksum(checksum));
                        firstRadioButton.setDisable(false);
                        secondRadioButton.setDisable(false);
                    } catch (NoSuchAlgorithmException | IOException e) {
                        e.printStackTrace();
                        System.err.println("In the Method checkSum(), executorService.submit() : " + e + "\n");
                        logger.log(Level.SEVERE, "An error occurred in the Method checkSum(), executorService.submit()",
                                e);
                        Platform.runLater(() -> displayError());
                    }

                });
            } else {
                txtFileName.setText("O arquivo não existe mais, tente denovo");
            }

        } catch (Exception e) {
            txtResultChecksum.setText("Aconteceu um problema, tente denovo");
            System.err.println("Method checkSum(): " + e + "\n");
            logger.log(Level.SEVERE, "An error occurred in checkSum() method", e);
        }
    }

    private String calculateChecksum(File file, String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        } catch (IOException e) {
            txtResultChecksum.setText("Aconteceu algum problema, tente denovo");
            System.err.println("Method calculateCheckSum(): " + e + "\n");
            logger.log(Level.SEVERE, "An error occurred in calculateChecksum() method", e);
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private void displayChecksum(String checksum) {
        resultChecksum = checksum;
        txtResultChecksum.setText(resultChecksum);
    }

    private void displayError() {
        txtResultChecksum.setText("Erro ao calcular o checksum.");
    }

    @FXML
    private void compareChecksum(ActionEvent event) {
        try {
            if (receivedSum != null && !receivedSum.getText().isEmpty()) {
                txtComparisonResult.setText(
                        resultChecksum != null && resultChecksum.equalsIgnoreCase(receivedSum.getText()) ? "É igual"
                                : "Não é igual");
            }
        } catch (Exception e) {
            receivedSum.setText("Aconteceu um problema, tente denovo");
            System.err.println("Method compareChecksum(): " + e + "\n");
            logger.log(Level.SEVERE, "An error occurred in compareChecksum() method", e);
        }

    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        if (receivedSum.isFocused()) {
            receivedSum.getParent().requestFocus();
        }
    }

}
