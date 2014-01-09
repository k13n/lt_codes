package com.k13n.lt_codes;

import java.io.File;

public class App {
  private static final double DEFAULT_ERASURE_PROBABILITY = 0.1;
  private static final double DEFAULT_PACKET_OVERHEAD = 1.38;

  private final File file;
  private final ErasureChannel channel;
  private Server server;
  private Client client;

  public App(String[] args) {
    String filename = parseFilename(args);
    file = ensureFileExists(filename);
    client = new Client();
    channel = setUpErasureChannel();
    server = new Server(file, channel, DEFAULT_PACKET_OVERHEAD);
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

  private ErasureChannel setUpErasureChannel() {
    ErasureChannel.Callback callback = new ErasureChannel.Callback() {
      @Override
      public void call(ErasureChannel channel, DecodedPacket packet) {
        client.receive(packet);
      }
    };
    return new ErasureChannel(DEFAULT_ERASURE_PROBABILITY, callback);
  }

  public void execute() {
    server.startTransmission();
    client.stopProcessing();

    if (client.transferSucceeded())
      System.out.println("file transfer succeeded");
    else
      System.out.println("file transfer did not complete");
  }

  public static void main(String[] args) {
    new App(args).execute();
  }

}
