package com.k13n.lt_codes;

import java.io.*;
import java.net.URL;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppTest {
  private static final String FILENAME = "test.txt";
  private static final String OUTFILE = "/tmp/" + FILENAME + ".out";
  private static final int PACKET_SIZE = 100;

  @Test
  public void itWorksWithPerfectChannel() throws Exception {
    byte[] data = readFile(FILENAME);
    Encoder enc = new Encoder(data, PACKET_SIZE);
    final Decoder dec = new DefaultDecoder(enc.getSeed(), enc.getNPackets());

    enc.encode(new Encoder.Callback() {
      public boolean call(Encoder encoder, TransmissonPacket packet) {
        return dec.receive(packet);
      }
    });

    dec.write(new FileOutputStream(OUTFILE));
  }

  @Test
  public void itWorksWithLossyChannel() throws Exception {
    byte[] data = readFile(FILENAME);
    Encoder enc = new Encoder(data, PACKET_SIZE);
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

    dec.write(new FileOutputStream(OUTFILE));
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
