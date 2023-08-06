package com.kldv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
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
            }
        } catch (Exception e) {
            txtFileName.setText(e.toString());
        }
    }

    @FXML
    private void selectAlgorithm(ActionEvent event) {
        if (firstRadioButton.isSelected()) {
            secondRadioButton.setSelected(false);
            secondRadioButton.setDisable(true);
        } else if (secondRadioButton.isSelected()) {
            firstRadioButton.setSelected(false);
            firstRadioButton.setDisable(true);
        } else {
            firstRadioButton.setDisable(false);
            secondRadioButton.setDisable(false);
        }
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private void checkSum(ActionEvent event) {
        if (firstRadioButton.isSelected() || secondRadioButton.isSelected()) {
            String algorithm = firstRadioButton.isSelected() ? "SHA-256" : "MD5";

            firstRadioButton.setDisable(true);
            secondRadioButton.setDisable(true);

            executorService.submit(() -> {
                try {
                    String checksum = calculateChecksum(selectedFile, algorithm);

                    Platform.runLater(() -> {
                        if (checksum != null) {
                            resultChecksum = checksum;
                            txtResultChecksum.setText(resultChecksum);
                        } else {
                            txtResultChecksum.setText("Erro ao calcular o checksum.");
                        }

                        firstRadioButton.setDisable(false);
                        secondRadioButton.setDisable(false);
                    });
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        txtResultChecksum.setText("Erro ao calcular o checksum.");
                        firstRadioButton.setDisable(false);
                        secondRadioButton.setDisable(false);
                    });
                }
            });
        } else if (!firstRadioButton.isSelected() && !secondRadioButton.isSelected()) {
            txtResultChecksum.setText("Marque se voce quer SHA256 ou MD5");
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
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    @FXML
    private void testStrings(ActionEvent event) {
        if (resultChecksum != null && resultChecksum.equalsIgnoreCase(receivedSum.getText())) {
            txtComparisonResult.setText("É igual");
        } else {
            txtComparisonResult.setText("Não é igual");
        }
    }
}
