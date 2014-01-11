package com.k13n.lt_codes.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class EncodedPacket {
  public static final String HEADER_TOKEN = "SUPERAWESOME";
  private final byte[] data;
  public static final int MAX_PACKET_SIZE = 1024 * 1024;

  public EncodedPacket(byte[] data){
    this.data = data;
  }

  public static class DecodingException extends Exception {
    public DecodingException(String msg) {
      super(msg);
    }
  }

  public static EncodedPacket encode(DecodedPacket packet) throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(os);

    dos.writeLong(packet.getFilesize());

    int[] neighbors = packet.getNeighbors();
    dos.writeInt(neighbors.length);

    for(int neighbor: neighbors)
      dos.writeInt(neighbor);

    byte[] data = packet.getData();
    dos.writeInt(data.length);
    dos.write(data, 0, data.length);

    Checksum checksum = new Adler32();
    byte[] encodedData = os.toByteArray();
    checksum.update(encodedData, 0, encodedData.length);

    ByteArrayOutputStream os2 = new ByteArrayOutputStream();
    DataOutputStream dos2 = new DataOutputStream(os2);

    dos2.writeBytes(HEADER_TOKEN);
    dos2.writeLong(checksum.getValue());
    dos2.writeInt(encodedData.length);
    dos2.write(encodedData, 0, encodedData.length);

    return new EncodedPacket(os2.toByteArray());
  }

  public byte[] toByteArray() {
    return data;
  }

  public DecodedPacket decode() throws Exception {

    ByteArrayInputStream is = new ByteArrayInputStream(this.data);
    DataInputStream dis = new DataInputStream(is);

    byte[] headerToken = new byte[HEADER_TOKEN.length()];
    dis.read(headerToken, 0, HEADER_TOKEN.length());

    if(!Arrays.equals(HEADER_TOKEN.getBytes("US-ASCII"), headerToken))
      throw new DecodingException(String.format("Invalid header (%d)", headerToken.length));

    long checksumValue = dis.readLong();
    int encodedDataLen = dis.readInt();
    if(encodedDataLen <= 0)
      throw new DecodingException("Nonpositive packet size");

    if(encodedDataLen >= MAX_PACKET_SIZE)
      throw new DecodingException("Packet exceeds maximum size or is invalid");

    byte[] encodedData = new byte[encodedDataLen];
    dis.readFully(encodedData);

    Checksum checksum = new Adler32();
    checksum.update(encodedData, 0, encodedData.length);

    if(checksumValue != checksum.getValue())
      throw new DecodingException("Invalid checksum");

    ByteArrayInputStream is2 = new ByteArrayInputStream(encodedData);
    DataInputStream dis2 = new DataInputStream(is2);

    long filesize = dis2.readLong();
    if(filesize <= 0)
      throw new DecodingException("Nonpositive file size");

    int nNeighbors = dis2.readInt();
    if(nNeighbors < 0)
      throw new DecodingException("Negative number of neighbors");

    int[] neighbors = new int[nNeighbors];
    for(int i = 0; i < nNeighbors; i++) {
      neighbors[i] = dis2.readInt();
    }

    int dataLen = dis2.readInt();
    if(dataLen <= 0)
      throw new DecodingException("Nonpositive data length");

    byte[] data = new byte[dataLen];
    dis2.read(data);

    return new DecodedPacket(filesize, neighbors, data);
  }
}
