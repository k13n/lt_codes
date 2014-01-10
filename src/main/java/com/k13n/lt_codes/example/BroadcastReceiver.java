package com.k13n.lt_codes.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import com.k13n.lt_codes.DecodedPacket;
import com.k13n.lt_codes.Decoder;
import com.k13n.lt_codes.EncodedPacket;
import com.k13n.lt_codes.IncrementalDecoder;

public class BroadcastReceiver {
  private final MulticastSocket socket;
  private final InetAddress group;
  private final Decoder decoder;
  private final byte[] buffer;

  public BroadcastReceiver() throws IOException {
    socket = new MulticastSocket(BroadcastSender.PORT);
    group = resolveGroup();
    socket.joinGroup(group);
    decoder = new IncrementalDecoder(Server.DEFAULT_PACKET_SIZE);
    buffer = new byte[EncodedPacket.MAX_PACKET_SIZE];
  }

  private InetAddress resolveGroup() throws UnknownHostException {
    return InetAddress.getByName(BroadcastSender.BROADCAST_ADDRESS);
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
    System.out.println("receiver: starting up");
    BroadcastReceiver receiver = new BroadcastReceiver();
    receiver.receive();
    receiver.write("");
    System.out.println("receiver: shutting down");
  }

}
