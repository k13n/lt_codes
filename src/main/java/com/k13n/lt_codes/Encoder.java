package com.k13n.lt_codes;

import com.k13n.soliton.RobustSolitonGenerator;
import java.util.Random;
import java.util.Arrays;
import java.util.BitSet;
import java.nio.ByteBuffer;

public final class Encoder {

  public interface Callback {
    public boolean call(Encoder encoder, int[] neighbours, byte data[]);
  }

  private static final double DEFAULT_C = 0.3333;

  private final int packetSize;
  private final int nPackets;
  private final Random uniformRNG;
  private final RobustSolitonGenerator solitonRNG;
  private final ByteBuffer buffer;
  private final long seed;

  public Encoder(byte[] data, int packetSize, double failureProbability) {
    this(data, packetSize, failureProbability, DEFAULT_C);
  }

  public Encoder(byte[] data, int packetSize, double failureProbability, double c) {
    this.packetSize = packetSize;
    this.seed = (long)(Math.random() * 1024 * 5);
    this.nPackets = (int)Math.ceil(data.length / packetSize);
    this.uniformRNG = new Random(this.seed);
    this.solitonRNG = new RobustSolitonGenerator(nPackets, c, failureProbability);

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
          int pos = this.packetIndex * this.packetSize;
          neighbours[i] = packetIndex;
          this.buffer.position(pos);
          BitSet bitSet = BitSet.valueOf(this.buffer).get(0, this.packetSize);
          if(xorSet == null)
            xorSet = bitSet;
          else
            xorSet.xor(bitSet);
        }

        abort = callback.call(this, neighbours, xorSet.toByteArray());


      } catch(Exception e) {
        /* FIXME*/
        e.printStackTrace();
      }


    }
      

  }
}
