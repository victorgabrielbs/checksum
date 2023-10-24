package com.kldv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

public class Calculate {

  private static final int BUFFER_SIZE = 1024;

  static String calculateSum(File file, String algorithm) throws NoSuchAlgorithmException, IOException {

    MessageDigest digest = MessageDigest.getInstance(algorithm);

    try (FileInputStream fis = new FileInputStream(file)) {

      byte[] byteArray = new byte[BUFFER_SIZE];
      int bytesCount;

      while ((bytesCount = fis.read(byteArray)) != -1) {
        digest.update(byteArray, 0, bytesCount);
      }

    } catch (IOException e) {
      LoggerConfig.logger.log(Level.SEVERE, "An error occurred in calculateSum() method", e);
    }

    byte[] bytes = digest.digest();
    StringBuilder sb = new StringBuilder();

    final int HEX_MASK = 0x100;

    for (byte aByte : bytes) {
      sb.append(Integer.toString((aByte & 0xff) + HEX_MASK, 16).substring(1));
    }

    return sb.toString();
  }
}
