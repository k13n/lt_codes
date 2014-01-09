package com.k13n.lt_codes;

public class DecodedPacket {
  private final long filesize;
  private final int[] neighbors;
  private final byte[] data;

  public DecodedPacket(long filesize, int[] neighbors, byte[] data) {
    this.filesize = filesize;
    this.neighbors = neighbors;
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }

  public long getFilesize() {
    return filesize;
  }

  public int[] getNeighbors() {
    return neighbors;
  }

}
