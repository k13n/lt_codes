package com.k13n.lt_codes;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public final class Decoder {
  
  private final Random uniformRNG;
  private final int nPackets;
  private final List<Packet> undecodedPackets;
  private final List<Packet> decodedPackets;

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

  public Decoder(long seed, int nPackets) {
    this.uniformRNG = new Random(seed);
    this.nPackets = nPackets;
    this.undecodedPackets = new ArrayList<Packet>();
    this.decodedPackets = new ArrayList<Packet>();
  }

  private int getNReceivedPackets() {
    return this.undecodedPackets.size() + this.decodedPackets.size();
  }

  public byte[] receive(byte[] data, int[] neighbours)
  {
    if(neighbours.length > 1)
      this.undecodedPackets.add(new Packet(data, neighbours));
    else
      this.decodedPackets.add(new Packet(data, neighbours));

    /* No way we could encode anything */
    if(this.getNReceivedPackets() < this.nPackets) {

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
