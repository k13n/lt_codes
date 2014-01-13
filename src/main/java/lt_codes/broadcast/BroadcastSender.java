package lt_codes.broadcast;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.beust.jcommander.JCommander;
import lt_codes.core.DecodedPacket;
import lt_codes.core.EncodedPacket;
import lt_codes.util.ErasureChannel;
import lt_codes.util.FountainServer;
import lt_codes.util.Server;
import lt_codes.util.ErasureChannel.Callback;

public class BroadcastSender {
  public static final int DEFAULT_PORT = 4446;
  public static final String DEFAULT_BROADCAST_ADDRESS = "230.0.0.1";

  private final DatagramSocket socket;
  private final Server server;
  private final InetAddress group;
  private final int port;

  public BroadcastSender(File file, int port, String address)
      throws SocketException, UnknownHostException {
    this.port = port;
    socket = new DatagramSocket();
    group = resolveGroup(address);
    server = new FountainServer(file, setUpChannel());
  }

  public BroadcastSender(File file) throws SocketException,
      UnknownHostException {
    this(file, DEFAULT_PORT, DEFAULT_BROADCAST_ADDRESS);
  }

  private InetAddress resolveGroup(String address) throws UnknownHostException {
    return InetAddress.getByName(address);
  }

  private ErasureChannel setUpChannel() {
    return new ErasureChannel(0.0, new Callback() {
      @Override
      public void call(ErasureChannel channel, DecodedPacket packet) {
        sendPacket(packet);
        sleepMillis(10);
      }
    });
  }

  private void sendPacket(DecodedPacket sourcePacket) {
    DatagramPacket datagram;
    try {
      byte[] data = EncodedPacket.encode(sourcePacket).toByteArray();
      datagram = new DatagramPacket(data, 0, data.length, group, port);
      socket.send(datagram);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sleepMillis(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void startBroadcast() {
    server.startTransmission();
  }

  public static void main(String[] args) throws SocketException,
      UnknownHostException {
    CliArguments arguments = new CliArguments();
    JCommander commander = new JCommander(arguments, args);
    if (!arguments.hasFilename() || arguments.getHelp())
      commander.usage();
    else {
      System.out.println("sender: starting up");
      File filename = new File(arguments.getFilename());
      BroadcastSender sender = new BroadcastSender(filename,
          arguments.getPort(), arguments.getBroadcastIpAddress());
      sender.startBroadcast();
      System.out.println("sender: shutting down");
    }
  }

}
