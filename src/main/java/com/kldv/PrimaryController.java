package com.kldv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final Logger logger = LoggerConfig.configureLogger();

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
    private void selectFile(ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar a ISO");
            String lastSelectedDirectory =
                    userPreferences.get("lastSelectedDirectory", System.getProperty("user.home"));
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
            logger.log(Level.SEVERE, "An error occurred in selectFile() method", e);
        }
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
                        String checksum = calculateSum(selectedFile, algorithm);
                        Platform.runLater(() -> {
                            resultChecksum = checksum;
                            txtResultChecksum.setText(resultChecksum);
                        });
                        firstRadioButton.setDisable(false);
                        secondRadioButton.setDisable(false);
                    } catch (NoSuchAlgorithmException | IOException e) {
                        e.printStackTrace();
                        logger.log(Level.SEVERE,
                                "An error occurred in the Method checkSum(), executorService.submit()",
                                e);
                        Platform.runLater(
                                () -> txtResultChecksum.setText("Erro ao calcular o checksum."));
                    }

                });
            } else {
                txtFileName.setText("O arquivo não existe mais, tente denovo");
            }

        } catch (Exception e) {
            txtResultChecksum.setText("Aconteceu um problema, tente denovo");
            logger.log(Level.SEVERE, "An error occurred in checkSum() method", e);
        }
    }

    private String calculateSum(File file, String algorithm)
            throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        } catch (IOException e) {
            txtResultChecksum.setText("Aconteceu algum problema, tente denovo");
            logger.log(Level.SEVERE, "An error occurred in calculateSum() method", e);
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    @FXML
    private void compareChecksum(ActionEvent event) {
        try {
            if (receivedSum != null && !receivedSum.getText().isEmpty()) {
                txtComparisonResult.setText(resultChecksum != null
                        && resultChecksum.equalsIgnoreCase(receivedSum.getText()) ? "É igual"
                                : "Não é igual");
            }
        } catch (Exception e) {
            receivedSum.setText("Aconteceu um problema, tente denovo");
            logger.log(Level.SEVERE, "An error occurred in compareChecksum() method", e);
        }

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
