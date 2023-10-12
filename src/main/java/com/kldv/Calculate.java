package com.kldv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Calculate {

  private static Logger logger = LoggerConfig.configureLogger();

  static String calculateSum(File file, String algorithm) throws NoSuchAlgorithmException, IOException {
    MessageDigest digest = MessageDigest.getInstance(algorithm);

    try (FileInputStream fis = new FileInputStream(file)) {

      byte[] byteArray = new byte[1024];
      int bytesCount;
      while ((bytesCount = fis.read(byteArray)) != -1) {
        digest.update(byteArray, 0, bytesCount);
      }

    } catch (IOException e) {
      logger.log(Level.SEVERE, "An error occurred in calculateSum() method", e);
    }

    byte[] bytes = digest.digest();
    StringBuilder sb = new StringBuilder();

    for (byte aByte : bytes) {
      sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();
  }
}
