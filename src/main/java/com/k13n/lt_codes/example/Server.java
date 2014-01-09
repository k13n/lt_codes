package com.k13n.lt_codes.example;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.k13n.lt_codes.DecodedPacket;
import com.k13n.lt_codes.Encoder;
import com.k13n.lt_codes.ErasureChannel;
import com.k13n.lt_codes.Encoder.Callback;

public abstract class Server {
  public static final int DEFAULT_PACKET_SIZE = 1024;
  protected final ErasureChannel channel;
  protected final Encoder encoder;
  private final File file;
  private boolean stopRequested;

  public Server(File file, ErasureChannel channel) {
    this.file = file;
    this.channel = channel;
    this.encoder = setUpEncoder();
    stopRequested = false;
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
      public boolean call(Encoder encoder, DecodedPacket packet) {
        channel.transmit(packet);
        return !stopRequested() && !transferFinished();
      }
    });
  }

  public synchronized void stopTransmission() {
    stopRequested = true;
  }

  private synchronized boolean stopRequested() {
    return stopRequested;
  }

  abstract protected boolean transferFinished();

}
