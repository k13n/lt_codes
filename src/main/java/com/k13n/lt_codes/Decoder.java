package com.k13n.lt_codes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public final class Decoder {
  private final int nrPackets;
  private final Queue<EncodedPacket> encodedPackets;
  private final SourcePacket[] sourcePackets;
  private int nrDecodedPackets;

  @SuppressWarnings("rawtypes")
  private static class Packet<T extends Packet> {
    private final BitSet data;
    private List<T> neighbors;

    public Packet(byte[] data) {
      this.data = BitSet.valueOf(data);
      neighbors = new ArrayList<>();
    }

    public BitSet getData() {
      return data;
    }

    public List<T> getNeighbors() {
      return neighbors;
    }

    public void addNeighbor(T packet) {
      neighbors.add(packet);
    }

    public void removeNeighbor(T packet) {
      neighbors.remove(packet);
    }

    public void xor(Packet packet) {
      this.data.xor(packet.getData());
    }

    public T getFirstNeighbor() {
      return neighbors.get(0);
    }

  }

  private static class EncodedPacket extends Packet<SourcePacket> {
    public EncodedPacket(byte[] data) {
      super(data);
    }
  }

  private static class SourcePacket extends Packet<EncodedPacket> {
    private boolean isDecoded;

    public SourcePacket(int packetSize) {
      super(new byte[packetSize]);
      isDecoded = false;
    }

    public boolean isDecoded() {
      return isDecoded;
    }

    public void flagAsDecoded() {
      isDecoded = true;
    }

  }

  public Decoder(int nrPackets, int packetSize) {
    this.nrPackets = nrPackets;
    encodedPackets = new PriorityQueue<>(nrPackets, new Comparator<EncodedPacket>() {
      @Override public int compare(EncodedPacket p1, EncodedPacket p2) {
        int nrNeighbors1 = p1.getNeighbors().size();
        int nrNeighbors2 = p2.getNeighbors().size();
        return Integer.compare(nrNeighbors1, nrNeighbors2);
      }
    });
    sourcePackets = new SourcePacket[nrPackets];
    for (int i = 0; i < nrPackets; i++)
      sourcePackets[i] = new SourcePacket(packetSize);
  }

  public boolean receive(byte[] data, int[] neighbors) {
    EncodedPacket packet = new EncodedPacket(data);
    for (int neighbor : neighbors) {
      if (!sourcePackets[neighbor].isDecoded()) {
        packet.addNeighbor(sourcePackets[neighbor]);
        sourcePackets[neighbor].addNeighbor(packet);
      }
    }
    if (packet.getNeighbors().size() > 0) {
      encodedPackets.offer(packet);
      decodingStep();
    }
    System.out.println(nrDecodedPackets + " " + nrPackets + " " + encodedPackets.size());
    return nrDecodedPackets == nrPackets;
  }

  private void decodingStep() {
    while (queueHasSingleNeighborPackets()) {
      EncodedPacket encodedPacket = encodedPackets.poll();
      SourcePacket sourcePacket = encodedPacket.getFirstNeighbor();
      sourcePacket.xor(encodedPacket);
      sourcePacket.flagAsDecoded();
      nrDecodedPackets++;

      Iterator<EncodedPacket> iterator = sourcePacket.getNeighbors().iterator();
      while (iterator.hasNext()) {
        EncodedPacket neighbor = iterator.next();
        neighbor.xor(sourcePacket);
        // remove each other as neighbors
        neighbor.removeNeighbor(sourcePacket);
        iterator.remove();
        // re-insert encoded packet to update its position
        if (neighbor.getNeighbors().size() > 0) {
          encodedPackets.remove(neighbor);
          encodedPackets.add(neighbor);
        } else if (neighbor.getNeighbors().size() == 0)
          encodedPackets.remove(neighbor);
      }
    }
  }

  private boolean queueHasSingleNeighborPackets() {
//    if (!encodedPackets.isEmpty())
//      System.out.println("foo: " + encodedPackets.peek().getNeighbors().size());
    return !encodedPackets.isEmpty() && encodedPackets.peek().getNeighbors().size() == 1;
  }

  public void write(OutputStream stream) throws IOException {
    for(SourcePacket packet: sourcePackets)
    {
      byte[] data = packet.getData().toByteArray();
      stream.write(data, 0, data.length);
    }
  }

}
