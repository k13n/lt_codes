package lt_codes.broadcast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.beust.jcommander.JCommander;
import lt_codes.core.DecodedPacket;
import lt_codes.core.Decoder;
import lt_codes.core.EncodedPacket;
import lt_codes.core.IncrementalDecoder;
import lt_codes.util.Server;

public class BroadcastReceiver {
  private final MulticastSocket socket;
  private final InetAddress group;
  private final Decoder decoder;
  private final byte[] buffer;

  public BroadcastReceiver(int port, String address) throws IOException {
    socket = new MulticastSocket(port);
    group = resolveGroup(address);
    socket.joinGroup(group);
    decoder = new IncrementalDecoder(Server.DEFAULT_PACKET_SIZE);
    buffer = new byte[EncodedPacket.MAX_PACKET_SIZE];
  }

  public BroadcastReceiver() throws IOException {
    this(BroadcastSender.DEFAULT_PORT,
        BroadcastSender.DEFAULT_BROADCAST_ADDRESS);
  }

  private InetAddress resolveGroup(String address) throws UnknownHostException {
    return InetAddress.getByName(address);
  }

  public void receive() {
    while (!decoder.isDecodingFinished()) {
      DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
      waitForDatagram(datagram);
    }
  }

  private void waitForDatagram(DatagramPacket datagram) {
    try {
      socket.receive(datagram);
      EncodedPacket encodedPacket = new EncodedPacket(datagram.getData());
      DecodedPacket packet = encodedPacket.decode();
      decoder.receive(packet);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(String filename) {
    try {
      FileOutputStream fos = new FileOutputStream(new File(filename));
      decoder.write(fos);
      fos.flush();
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    CliArguments arguments = new CliArguments();
    JCommander commander = new JCommander(arguments, args);
    if (!arguments.hasFilename() || arguments.getHelp())
      commander.usage();
    else {
      System.out.println("receiver: starting up");
      BroadcastReceiver receiver = new BroadcastReceiver(arguments.getPort(),
          arguments.getBroadcastIpAddress());
      receiver.receive();
      receiver.write(arguments.getFilename());
      System.out.println("receiver: shutting down");
    }
  }

}
