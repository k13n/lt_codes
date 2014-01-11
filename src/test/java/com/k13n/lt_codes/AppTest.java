package com.k13n.lt_codes;

import java.io.*;
import java.net.URL;

import org.junit.Test;

import com.k13n.lt_codes.core.DecodedPacket;
import com.k13n.lt_codes.core.Decoder;
import com.k13n.lt_codes.core.EncodedPacket;
import com.k13n.lt_codes.core.Encoder;
import com.k13n.lt_codes.core.IncrementalDecoder;
import com.k13n.lt_codes.util.ErasureChannel;

import static org.junit.Assert.*;

public class AppTest {
  private static final String FILENAME = "firework.jpg";
  private static final String OUTFILE = "/tmp/" + FILENAME + ".out";
  private static final int PACKET_SIZE = 1024;

  @Test
  public void testEncoding() throws Exception {
    DecodedPacket p = new DecodedPacket(1024,
                                        new int[]{1,2,3,4,5,6},
                                        new byte[]{100, 2, 44, 12, 76, -32, 34, -66});

    EncodedPacket ep = EncodedPacket.encode(p);
    DecodedPacket dp = ep.decode();

    assertEquals(p.getFilesize(), dp.getFilesize());
    assertArrayEquals(p.getNeighbors(), dp.getNeighbors());
    assertArrayEquals(p.getData(), dp.getData());
  }

  @Test
  public void itWorksWithPerfectChannel() throws Exception {
    byte[] data = readFile(FILENAME);
    Encoder enc = new Encoder(data, PACKET_SIZE);
//    final Decoder dec = new DefaultDecoder(enc.getSeed(), enc.getNPackets());
    final Decoder dec = new IncrementalDecoder(PACKET_SIZE);

    enc.encode(new Encoder.Callback() {
      public boolean call(Encoder encoder, DecodedPacket packet) {
        return dec.receive(packet);
      }
    });

    dec.write(new FileOutputStream(OUTFILE));

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    dec.write(bos);
    assertArrayEquals(data, bos.toByteArray());
  }

  @Test
  public void itWorksWithLossyChannel() throws Exception {
    byte[] data = readFile(FILENAME);
    Encoder enc = new Encoder(data, PACKET_SIZE);
//    final Decoder dec = new DefaultDecoder(enc.getSeed(), enc.getNPackets());
    final Decoder dec = new IncrementalDecoder(PACKET_SIZE);

    final ErasureChannel channel = new ErasureChannel(0.3,
      new ErasureChannel.Callback() {
        @Override
        public void call(ErasureChannel channel, DecodedPacket packet) {
          dec.receive(packet);
        }
      });
    enc.encode(new Encoder.Callback() {
      public boolean call(Encoder encoder, DecodedPacket packet) {
        channel.transmit(packet);
        return dec.isDecodingFinished();
      }
    });

    dec.write(new FileOutputStream(OUTFILE));

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    dec.write(bos);
    assertArrayEquals(data, bos.toByteArray());
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
