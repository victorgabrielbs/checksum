package com.kldv;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerConfig {

  private static final Logger logger = Logger.getLogger(LoggerConfig.class.getName());

  public static Logger configureLogger() {
    try {
      File logFolder = new File("logs");

      if (!logFolder.exists()) {
        logFolder.mkdir();
      }
      FileHandler fileHandler = new FileHandler("logs/app.log", true);
      SimpleFormatter formatter = new SimpleFormatter();
      fileHandler.setFormatter(formatter);
      logger.addHandler(fileHandler);
      return logger;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
