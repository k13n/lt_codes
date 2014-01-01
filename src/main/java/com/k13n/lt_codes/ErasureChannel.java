package com.k13n.lt_codes;

public final class ErasureChannel {

  public interface Callback {
    public void call(ErasureChannel channel, byte[] data);
  }

  private final Callback callback;
  private final double erasureProbability;

  public ErasureChannel(Callback callback, double erasureProbability) {
    this.callback = callback;
    this.erasureProbability = erasureProbability;
  }

  public void transmit(byte[] data) {
    if (Math.random() > erasureProbability)
      callback.call(this, data);
  }

}
