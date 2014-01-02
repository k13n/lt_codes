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

public final class IncrementalDecoder implements Decoder {
  private final int nrPackets;
  private final int packetSize;
  private final Queue<EncodedPacket> encodedPackets;
  private final SourcePacket[] sourcePackets;
  private int nrDecodedPackets;
  private int packetsProcessed;

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

  public IncrementalDecoder(int nrPackets, int packetSize) {
    this.nrPackets = nrPackets;
    this.packetSize = packetSize;
    encodedPackets = setUpQueue();
    sourcePackets = setUpSourcePakckets();
  }

  private PriorityQueue<EncodedPacket> setUpQueue() {
    return new PriorityQueue<>(nrPackets, new Comparator<EncodedPacket>() {
      @Override public int compare(EncodedPacket p1, EncodedPacket p2) {
        int nrNeighbors1 = p1.getNeighbors().size();
        int nrNeighbors2 = p2.getNeighbors().size();
        return Integer.compare(nrNeighbors1, nrNeighbors2);
      }
    });
  }

  private SourcePacket[] setUpSourcePakckets() {
    SourcePacket[] sourcePackets = new SourcePacket[nrPackets];
    for (int i = 0; i < nrPackets; i++)
      sourcePackets[i] = new SourcePacket(packetSize);
    return sourcePackets;
  }

  @Override
  public boolean receive(TransmissonPacket packet) {
    EncodedPacket encodedPacket = createPacketFromInput(packet);
    if (encodedPacket.getNeighbors().size() > 0) {
      encodedPackets.offer(encodedPacket);
      decodingStep();
    }
    packetsProcessed++;
    return isDecodingFinished();
  }

  private EncodedPacket createPacketFromInput(TransmissonPacket packet) {
    EncodedPacket encodedPacket = new EncodedPacket(packet.getData());
    for (int neighbor : packet.getNeighbors()) {
      if (!sourcePackets[neighbor].isDecoded()) {
        encodedPacket.addNeighbor(sourcePackets[neighbor]);
        sourcePackets[neighbor].addNeighbor(encodedPacket);
      } else {
        encodedPacket.xor(sourcePackets[neighbor]);
      }
    }
    return encodedPacket;
  }

  private void decodingStep() {
    while (queueHasSingleNeighborPacket()) {
      EncodedPacket encodedPacket = encodedPackets.poll();
      SourcePacket sourcePacket = encodedPacket.getFirstNeighbor();
      sourcePacket.xor(encodedPacket);
      sourcePacket.flagAsDecoded();
      cascadeDecode(sourcePacket);
      nrDecodedPackets++;
    }
  }

  private void cascadeDecode(SourcePacket sourcePacket) {
    Iterator<EncodedPacket> iterator = sourcePacket.getNeighbors().iterator();
    while (iterator.hasNext()) {
      EncodedPacket neighbor = iterator.next();
      neighbor.xor(sourcePacket);
      // remove each other as neighbors
      neighbor.removeNeighbor(sourcePacket);
      iterator.remove();
      reheapifyQueueAfterKeyUpdate(neighbor);
    }
  }

  private void reheapifyQueueAfterKeyUpdate(EncodedPacket packet) {
    encodedPackets.remove(packet);
    if (packet.getNeighbors().size() > 0)
      encodedPackets.add(packet);
  }

  private boolean queueHasSingleNeighborPacket() {
    return !encodedPackets.isEmpty() &&
        encodedPackets.peek().getNeighbors().size() == 1;
  }

  @Override
  public void write(OutputStream stream) throws IOException {
    for (SourcePacket packet: sourcePackets) {
      byte[] data = packet.getData().toByteArray();
      stream.write(data);
    }
  }

  public int getNrPacketsProcessed() {
    return packetsProcessed;
  }

  @Override
  public boolean isDecodingFinished() {
    return nrDecodedPackets == nrPackets;
  }

}
