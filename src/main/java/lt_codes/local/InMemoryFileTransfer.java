package lt_codes.local;

import java.io.File;

import com.beust.jcommander.JCommander;
import lt_codes.core.DecodedPacket;
import lt_codes.util.Client;
import lt_codes.util.ErasureChannel;
import lt_codes.util.FixedRateServer;
import lt_codes.util.Server;
import lt_codes.util.ErasureChannel.Callback;

public class InMemoryFileTransfer {
  public static final double DEFAULT_ERASURE_PROBABILITY = 0.0;
  public static final double DEFAULT_PACKET_OVERHEAD = 1.23;

  private final ErasureChannel channel;
  private Server server;
  private Client client;

  public InMemoryFileTransfer(File file, double erasureProbability,
      double packetOverhead) {
    if (!file.exists())
      throw new IllegalArgumentException("File does not exist");

    client = new Client();
    channel = setUpErasureChannel(erasureProbability);
    server = new FixedRateServer(file, channel, packetOverhead);
  }

  public InMemoryFileTransfer(File file) {
    this(file, DEFAULT_ERASURE_PROBABILITY, DEFAULT_PACKET_OVERHEAD);
  }

  private ErasureChannel setUpErasureChannel(double erasureProbability) {
    Callback callback = new Callback() {
      @Override
      public void call(ErasureChannel channel, DecodedPacket packet) {
        client.receive(packet);
      }
    };
    return new ErasureChannel(erasureProbability, callback);
  }

  public boolean execute() {
    server.startTransmission();
    client.stopProcessing();
    return client.transferSucceeded();
  }

  public static void main(String[] args) {
    CliArguments arguments = new CliArguments();
    JCommander commander = new JCommander(arguments, args);
    if (!arguments.hasFilename() || arguments.getHelp())
      commander.usage();
    else {
      File file = new File(arguments.getFilename());
      InMemoryFileTransfer app = new InMemoryFileTransfer(file,
          arguments.getErasureProbability(), arguments.getPacketOverhead());
      boolean transferSucceeded = app.execute();

      if (transferSucceeded)
        System.out.println("file transfer succeeded");
      else
        System.out.println("file transfer failed");

      System.exit(transferSucceeded ? 0 : 1);
    }
  }

}
