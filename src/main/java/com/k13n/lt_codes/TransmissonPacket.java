package com.k13n.lt_codes;

public class TransmissonPacket {
  private final int[] neighbors;
  private final byte[] data;

  public TransmissonPacket(int[] neighbors, byte[] data) {
    this.neighbors = neighbors;
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }

  public int[] getNeighbors() {
    return neighbors;
  }

}
