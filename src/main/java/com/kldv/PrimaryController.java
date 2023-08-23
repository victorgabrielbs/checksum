package com.kldv;

import java.io.*;
import java.security.*;
import java.util.concurrent.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) txtFileName.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                executorService.shutdown();
            });
        });
        enableRadioButtons();
    }

    @FXML
    private void selectFile(ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar a ISO");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                txtFileName.setText(selectedFile.getName());
            } else {
                txtFileName.setText("Selecione a ISO.");
            }
        } catch (Exception e) {
            txtFileName.setText("Aconteceu um problema, tente denovo");
            System.err.println(e);
        }
    }

    @FXML
    private void selectAlgorithm(ActionEvent event) {
        firstRadioButton.setDisable(secondRadioButton.isSelected());
        secondRadioButton.setDisable(firstRadioButton.isSelected());
    }

    @FXML
    private void checkSum(ActionEvent event) {
        try {
            if (!firstRadioButton.isSelected() && !secondRadioButton.isSelected()) {
                txtResultChecksum.setText("Marque se você quer SHA256 ou MD5");
                return;
            }

            String algorithm = firstRadioButton.isSelected() ? "SHA-256" : "MD5";
            disableRadioButtons();
            firstRadioButton.setDisable(true);
            secondRadioButton.setDisable(true);

            executorService.submit(() -> {
                calculateAndDisplayChecksum(algorithm);
                enableRadioButtons();
            });
        } catch (Exception e) {
            txtResultChecksum.setText("Aconteceu um problema, tente denovo");
            System.err.println(e);
        }
    }

    private void calculateAndDisplayChecksum(String algorithm) {
        try {
            String checksum = calculateChecksum(selectedFile, algorithm);
            Platform.runLater(() -> displayChecksum(checksum));
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> displayError());
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
            System.err.println(e);
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

    private void disableRadioButtons() {
        firstRadioButton.setDisable(true);
        secondRadioButton.setDisable(true);
    }

    private void enableRadioButtons() {
        firstRadioButton.setDisable(false);
        secondRadioButton.setDisable(false);
    }

    @FXML
    private void compareChecksum(ActionEvent event) {
        try {
            if (receivedSum != null && !receivedSum.getText().isEmpty()) {
                txtComparisonResult.setText(
                        resultChecksum != null && resultChecksum.equalsIgnoreCase(receivedSum.getText()) ? "É igual"
                                : "Não é igual");
            } else {
                receivedSum.setText("Escreva a soma que voce recebeu.");
            }
        } catch (Exception e) {
            receivedSum.setText("Aconteceu um problema, tente denovo");
            System.err.println(e);
        }

    }
}
