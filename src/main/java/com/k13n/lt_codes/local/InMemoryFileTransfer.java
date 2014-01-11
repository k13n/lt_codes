package com.k13n.lt_codes.local;

import java.io.File;

import com.beust.jcommander.JCommander;
import com.k13n.lt_codes.core.DecodedPacket;
import com.k13n.lt_codes.util.Client;
import com.k13n.lt_codes.util.ErasureChannel;
import com.k13n.lt_codes.util.FixedRateServer;
import com.k13n.lt_codes.util.Server;
import com.k13n.lt_codes.util.ErasureChannel.Callback;

public class InMemoryFileTransfer {
  private static final double DEFAULT_ERASURE_PROBABILITY = 0.1;
  private static final double DEFAULT_PACKET_OVERHEAD = 1.38;

  private final ErasureChannel channel;
  private Server server;
  private Client client;

  public InMemoryFileTransfer(File file) {
    if (!file.exists())
      throw new IllegalArgumentException("File does not exist");

    client = new Client();
    channel = setUpErasureChannel();
    server = new FixedRateServer(file, channel, DEFAULT_PACKET_OVERHEAD);
  }

  private ErasureChannel setUpErasureChannel() {
    Callback callback = new Callback() {
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
      System.out.println("file transfer failed");
  }

  public static void main(String[] args) {
    CliArguments arguments = new CliArguments();
    JCommander commander = new JCommander(arguments, args);
    if (!arguments.hasFilename() || arguments.getHelp())
      commander.usage();
    else {
      File file = new File(arguments.getFilename());
      new InMemoryFileTransfer(file).execute();
    }
  }

}
