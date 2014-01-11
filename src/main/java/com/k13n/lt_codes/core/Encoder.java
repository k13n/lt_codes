package com.k13n.lt_codes.core;

import java.util.Random;
import java.util.Arrays;
import java.util.BitSet;
import java.nio.ByteBuffer;

public final class Encoder {

  public interface Callback {
    public boolean call(Encoder encoder, DecodedPacket packet);
  }

  private static final double DEFAULT_FAILURE_PROBABILITY = 0.01;
  private static final int DEFAULT_SPIKE = 50;

  private final int filesize;
  private final int packetSize;
  private final int nPackets;
  private final Random uniformRNG;
  private final RobustSolitonGenerator solitonRNG;
  private final ByteBuffer buffer;
  private final long seed;

  public Encoder(byte[] data, int packetSize) {
    this(data, packetSize, DEFAULT_FAILURE_PROBABILITY, DEFAULT_SPIKE);
  }

  public Encoder(byte[] data, int packetSize, double failureProbability, int spike) {
    this.filesize = data.length;
    this.packetSize = packetSize;
    this.seed = (long)(Math.random() * 1024 * 5);
    this.nPackets = (int)Math.ceil(data.length / (double)packetSize);
    this.uniformRNG = new Random(this.seed);
    this.solitonRNG = new RobustSolitonGenerator(nPackets, spike, failureProbability);

    this.buffer = ByteBuffer.wrap(Arrays.copyOf(data, nPackets * packetSize),
                                  0, nPackets * packetSize);
  }

  public long getSeed() {
    return this.seed;
  }

  public int getNPackets() {
    return this.nPackets;
  }

  public void encode(Callback callback) {
    boolean abort = false;
    while(!abort) {
      try {

        int d = solitonRNG.next();
        BitSet xorSet = null;

        int[] neighbours = new int[d];

        for(int i = 0; i < d; i++)
        {
          int packetIndex = uniformRNG.nextInt(this.nPackets);
          int pos = packetIndex * this.packetSize;
          neighbours[i] = packetIndex;
          this.buffer.limit(pos + this.packetSize);
          this.buffer.position(pos);
          BitSet bitSet = BitSet.valueOf(this.buffer).get(0, this.packetSize * 8);
          if(xorSet == null)
            xorSet = bitSet;
          else
            xorSet.xor(bitSet);
        }

        byte[] packetData = xorSet.toByteArray();
        DecodedPacket packet = new DecodedPacket(filesize, neighbours, packetData);
        abort = callback.call(this, packet);


      } catch(Exception e) {
        /* FIXME*/
        e.printStackTrace();
      }

    }


  }
}
