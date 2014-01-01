package com.k13n.lt_codes;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public final class Decoder {
  
  private final Random uniformRNG;
  private final int nPackets;
  private final List<Packet> receivedPackets;

  private final class Packet {
    private final byte[] data;
    private final int[] neighbours;

    public Packet(byte[] data, int[] neighbours) {
      this.data = data;
      this.neighbours = neighbours;
    }

    public byte[] getData() {
      return this.data;
    }

    public int[] getNeighbours() {
      return this.neighbours;
    }
    
  }

  
  public Decoder(long seed, int nPackets)
  {
    this.uniformRNG = new Random(seed);
    this.nPackets = nPackets;
    this.receivedPackets = new ArrayList<Packet>();
  }

  public byte[] receive(byte[] data, int[] neighbours)
  {
    this.receivedPackets.add(new Packet(data, neighbours));

    /* No way we could encode anything */
    if(this.receivedPackets.size() < this.nPackets) {

      return null;
    } else {
      /* we need a great deal more than k = this.nPackets,
       * but for anything > k, we give it a try.
       * Yes, this means that work is done for nothing
       * FIXME: use exact lower bounds here
       *
       */

      /* ... */

      return null;
    }

  }

}
