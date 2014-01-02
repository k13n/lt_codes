package com.k13n.lt_codes;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.k13n.lt_codes.Encoder.Callback;

public class Server {
  public static final int DEFAULT_PACKET_SIZE = 1024;
  private final File file;
  private final ErasureChannel channel;
  private final Encoder encoder;
  private final int packetsToTransmit;

  public Server(File file, ErasureChannel channel, double packetOverhead) {
    if (packetOverhead < 1)
      throw new IllegalArgumentException("The packet overhead must be >= 1");

    this.file = file;
    this.channel = channel;
    this.encoder = setUpEncoder();
    this.packetsToTransmit = (int) (encoder.getNPackets() * packetOverhead);
  }

  private Encoder setUpEncoder() {
    byte[] fileData = readFile();
    return new Encoder(fileData, DEFAULT_PACKET_SIZE);
  }

  private byte[] readFile() {
    byte[] fileData = new byte[(int) file.length()];
    try {
      FileInputStream fis = new FileInputStream(file);
      DataInputStream dis = new DataInputStream(fis);
      dis.readFully(fileData);
      dis.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      // FIXME
    } catch (IOException e) {
      e.printStackTrace();
      // FIXME
    }
    return fileData;
  }

  public void startTransmission() {
    encoder.encode(new Callback() {
      @Override
      public boolean call(Encoder encoder, TransmissonPacket packet) {
        channel.transmit(packet);
        return channel.getNrTransmissions() >= packetsToTransmit;
      }
    });
  }

}
