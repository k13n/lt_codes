package com.k13n.lt_codes;

import java.io.File;

public class App {
  private final File file;

  public App(String[] args) {
    String filename = parseFilename(args);
    file = ensureFileExists(filename);
  }

  private String parseFilename(String[] args) {
    if (args.length == 0)
      throw new IllegalArgumentException("No file provided");
    return args[0];
  }

  private File ensureFileExists(String filename) {
    File file = new File(filename);
    if (!file.exists())
      throw new IllegalArgumentException("File does not exist");
    return file;
  }

  public void execute() {
    // FIXME
  }

  public static void main(String[] args) {
    new App(args).execute();
  }

}
