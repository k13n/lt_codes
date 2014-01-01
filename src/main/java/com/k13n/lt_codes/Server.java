package com.k13n.lt_codes;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.k13n.lt_codes.Encoder.Callback;

public class Server {
  private final File file;
  private final ErasureChannel channel;
  private final Encoder encoder;
  private final int packetsToDeliver;

  public Server(File file, ErasureChannel channel, int packetsToDeliver) {
    this.file = file;
    this.channel = channel;
    this.encoder = setUpEncoder();
    this.packetsToDeliver = packetsToDeliver;
  }

  private Encoder setUpEncoder() {
    byte[] fileData = readFile();
    final int packetSize = 1024;
    return new Encoder(fileData, packetSize);
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
      public boolean call(Encoder encoder, int[] neighbours, byte[] data) {
        channel.transmit(data);
        return channel.getNrTransmissions() >= packetsToDeliver;
      }
    });
  }

}
