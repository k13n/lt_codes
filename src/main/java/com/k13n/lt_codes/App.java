package com.k13n.lt_codes;

import java.io.File;

public class App {
  private final File file;
  private final ErasureChannel channel;
  private Server server;
  private Client client;

  public App(String[] args) {
    String filename = parseFilename(args);
    file = ensureFileExists(filename);
    client = new Client();
    channel = setUpErasureChannel();
    server = setUpServer();
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
    ErasureChannel.Callback channelCallback = new ErasureChannel.Callback() {
      @Override public void call(ErasureChannel channel, TransmissonPacket packet) {
        client.receive(packet);
      }
    };
    return new ErasureChannel(channelCallback, 0.2);
  }

  private Server setUpServer() {
    double packetOverhead = 1.2;
    return new Server(file, channel, packetOverhead);
  }

  public void execute() {
    client.startProcessing();
    server.startTransmission();
    client.stopProcessing();
  }

  public static void main(String[] args) {
    new App(args).execute();
  }

}
