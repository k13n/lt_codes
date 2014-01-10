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
  public static final int PORT = 4446;
  public static final String BROADCAST_ADDRESS = "230.0.0.1";

  private final DatagramSocket socket;
  private final Server server;
  private final InetAddress group;

  public BroadcastSender(File file) throws SocketException,
      UnknownHostException {
    socket = new DatagramSocket();
    group = resolveGroup();
    server = new FountainServer(file, setUpChannel());
  }

  private InetAddress resolveGroup() throws UnknownHostException {
    return InetAddress.getByName(BROADCAST_ADDRESS);
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
    try {
      byte[] data = EncodedPacket.encode(sourcePacket).toByteArray();
      DatagramPacket datagram = new DatagramPacket(data, 0, data.length, group,
          PORT);
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
    System.out.println("sender: starting up");
    File file = new File("");
    new BroadcastSender(file).startBroadcast();
    System.out.println("sender: shutting down");
  }

}
