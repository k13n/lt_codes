package com.k13n.lt_codes;

public final class ErasureChannel {
  private final double erasureProbability;

  public static final byte[] ERASED = new byte[0];

  public ErasureChannel(double erasureProbability) {
    this.erasureProbability = erasureProbability;
  }

  public byte[] transmit(byte[] data)
  {
    if(Math.random() <= erasureProbability)
    {
      return ERASED;
    }
    else
      return data;
  }
}
