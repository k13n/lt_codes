package lt_codes.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;

public final class IncrementalDecoder implements Decoder {
  private static final Logger logger = Logger
      .getLogger(IncrementalDecoder.class);

  private final int packetSize;
  private final Queue<EncodedPacket> encodedPackets;
  private SourcePacket[] sourcePackets;
  private int nrPackets;
  private int nrDecodedPackets;
  private int packetsProcessed;
  private long filesize;
  private boolean hasFirstPacketArrived;

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
    private static final byte[] EMPTY_ARRAY = new byte[0];
    private boolean isDecoded;

    public SourcePacket() {
      super(EMPTY_ARRAY);
      isDecoded = false;
    }

    public boolean isDecoded() {
      return isDecoded;
    }

    public void flagAsDecoded() {
      isDecoded = true;
    }

  }

  public IncrementalDecoder(int packetSize) {
    this.packetSize = packetSize;
    hasFirstPacketArrived = false;
    encodedPackets = setUpQueue();
  }

  private PriorityQueue<EncodedPacket> setUpQueue() {
    int initialCapacity = 1000;
    return new PriorityQueue<>(initialCapacity, new Comparator<EncodedPacket>() {
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
      sourcePackets[i] = new SourcePacket();
    return sourcePackets;
  }

  @Override
  public boolean receive(DecodedPacket packet) {
    if (!hasFirstPacketArrived)
      handleFirstPacket(packet);
    if (isDecodingFinished())
      return true;

    EncodedPacket encodedPacket = createPacketFromInput(packet);
    if (encodedPacket.getNeighbors().size() > 0) {
      encodedPackets.offer(encodedPacket);
      decodingStep();
    }

    packetsProcessed++;
    logProgress();

    return isDecodingFinished();
  }

  private EncodedPacket createPacketFromInput(DecodedPacket packet) {
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

  private void handleFirstPacket(DecodedPacket packet) {
    filesize = packet.getFilesize();
    nrPackets = (int) Math.ceil(filesize / (double)packetSize);
    sourcePackets = setUpSourcePakckets();
    hasFirstPacketArrived = true;
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

  private void logProgress() {
    double progress = (nrDecodedPackets / (double) nrPackets) * 100;
    String format = "received: %d, decoded: %6.2f%% (%d / %d)";
    logger.debug(String.format(format, packetsProcessed, progress,
        nrDecodedPackets, nrPackets));
  }


  @Override
  public void write(OutputStream stream) throws IOException {
    if (!isDecodingFinished())
      throw new IllegalStateException("Encoding is not yet finished");

    for (int i = 0; i < sourcePackets.length; i++) {
      SourcePacket packet = sourcePackets[i];
      byte[] data = createEmptyBuffer(packet, i == sourcePackets.length - 1);
      byte[] packetData = packet.getData().toByteArray();
      System.arraycopy(packetData, 0, data, 0, packetData.length);
      stream.write(data);
    }
    stream.flush();
  }

  private byte[] createEmptyBuffer(SourcePacket packet, boolean isLastPacket) {
    int size = isLastPacket ? lastPacketSize() : packetSize;
    return new byte[size];
  }

  private int lastPacketSize() {
    return (int) (filesize - (nrPackets-1) * packetSize);
  }

  public int getNrPacketsProcessed() {
    return packetsProcessed;
  }

  @Override
  public boolean isDecodingFinished() {
    return hasFirstPacketArrived && nrDecodedPackets == nrPackets;
  }

}
