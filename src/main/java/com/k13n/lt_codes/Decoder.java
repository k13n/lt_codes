package com.k13n.lt_codes;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public final class Decoder {
  
  private final Random uniformRNG;
  private final int nPackets;
  private final List<Packet> undecodedPackets;
  private final Packet[] decodedPackets;
  private int receivedPackets;

  private static final class Packet {
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

    public boolean hasNeighbour(int packetId)
    {
      for(int i = 0; i < this.neighbours.length; i++)
      {
        if(this.neighbours[i] == packetId)
        {
          return true;
        }
      }
      return false;
    }
    
  }

  public Decoder(long seed, int nPackets) {
    this.uniformRNG = new Random(seed);
    this.nPackets = nPackets;
    this.undecodedPackets = new ArrayList<Packet>();
    this.decodedPackets = new Packet[nPackets];
  }

  private int nUndecodedNeighbours(Packet packet)
  {
    int d = 0;
    for(int neighbourId: packet.getNeighbours())
    {
      if(this.decodedPackets[neighbourId] == null)
        d++;
    }
    return d;
  }

  private int undecodedNeighbourId(Packet packet)
  {
    for(int neighbourId: packet.getNeighbours())
    {
      if(this.decodedPackets[neighbourId] == null)
        return neighbourId;
    }
    return -1;
  }

  private Packet decodePacket(Packet packet) {
    return null;
  }

  private void decodingStep() {
    for(Packet packet: this.undecodedPackets)
    {
      if(nUndecodedNeighbours(packet) == 1)
      {
        int undecodedNeighbourId = undecodedNeighbourId(packet);
        decodedPackets[undecodedNeighbourId] = decodePacket(packet);
      }
    }
  }

  public byte[] receive(byte[] data, int[] neighbours)
  {
    if(neighbours.length > 1)
      this.undecodedPackets.add(new Packet(data, neighbours));
    else
    {
      this.decodedPackets[neighbours[0]] = new Packet(data, neighbours);
    }

    receivedPackets++;

    /* No way we could encode anything */
    if(receivedPackets < this.nPackets) {

      return null;
    } else {
      /* we need a great deal more than k = this.nPackets,
       * but for anything > k, we give it a try.
       * Yes, this means that work is done for nothing
       * FIXME: use exact lower bounds here
       *
       */

      decodingStep();

      /* ... */

      return null;
    }

  }

}
