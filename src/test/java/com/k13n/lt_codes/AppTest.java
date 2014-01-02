package com.k13n.lt_codes;

import java.io.*;
import java.net.URL;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppTest {

  @Test
  public void assertWorksWithPerfectChannel() throws Exception {
    String filename = "test.txt";
    byte[] data = readFile(filename);

    int packetSize = 100;
    Encoder enc = new Encoder(data, packetSize);
    final Decoder dec = new DefaultDecoder(enc.getSeed(), enc.getNPackets());

    enc.encode(new Encoder.Callback() {
      public boolean call(Encoder encoder, TransmissonPacket packet) {
        return dec.receive(packet);
      }
    });

    dec.write(new FileOutputStream("/tmp/" + filename + ".out"));
  }

  @Test
  public void assertWorksWithLossyChannel() throws Exception {
    String filename = "test.txt";
    byte[] data = readFile(filename);

    int packetSize = 100;
    Encoder enc = new Encoder(data, packetSize);
    final Decoder dec = new DefaultDecoder(enc.getSeed(), enc.getNPackets());

    final ErasureChannel channel = new ErasureChannel(
        new ErasureChannel.Callback() {
          @Override
          public void call(ErasureChannel channel, TransmissonPacket packet) {
            dec.receive(packet);
          }
        }, 0.3);
    enc.encode(new Encoder.Callback() {
      public boolean call(Encoder encoder, TransmissonPacket packet) {
        channel.transmit(packet);
        return dec.isDecodingFinished();
      }
    });

    dec.write(new FileOutputStream("/tmp/" + filename + ".out"));
  }

  private byte[] readFile(String filename) {
    filename = "/" + filename;
    byte[] data = null;
    try {
      URL url = this.getClass().getResource(filename);
      InputStream is = getClass().getResourceAsStream(filename);
      assertNotNull(url);
      File file = new File(url.getFile());

      data = new byte[(int) file.length()];
      DataInputStream s = new DataInputStream(is);
      s.readFully(data);
      s.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return data;
  }

}
