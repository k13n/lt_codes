package com.k13n.lt_codes.example;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.k13n.lt_codes.DecodedPacket;
import com.k13n.lt_codes.EncodedPacket;
import com.k13n.lt_codes.ErasureChannel;
import com.k13n.lt_codes.ErasureChannel.Callback;

public class BroadcastSender {
  public static final int DEFAULT_PORT = 4446;
  public static final String DEFAULT_BROADCAST_ADDRESS = "230.0.0.1";

  private final DatagramSocket socket;
  private final Server server;
  private final InetAddress group;
  private final int port;

  public BroadcastSender(File file, int port, String address) throws SocketException,
      UnknownHostException {
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

  public void startBroadcast() {
    server.startTransmission();
  }

  public static void main(String[] args) throws SocketException,
      UnknownHostException {
    String filename = args[0];
    int port = Integer.parseInt(args[1]);
    String address = args[2];

    System.out.println("sender: starting up");
    File file = new File(filename);
    new BroadcastSender(file, port, address).startBroadcast();
    System.out.println("sender: shutting down");
  }

}
