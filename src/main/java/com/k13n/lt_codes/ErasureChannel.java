package com.k13n.lt_codes;

import java.util.concurrent.atomic.AtomicLong;

public final class ErasureChannel {

  public interface Callback {
    public void call(ErasureChannel channel, TransmissonPacket packet);
  }

  private final Callback callback;
  private final double erasureProbability;
  private AtomicLong nrTransmissions;

  public ErasureChannel(double erasureProbability, Callback callback) {
    this.erasureProbability = erasureProbability;
    this.callback = callback;
    nrTransmissions = new AtomicLong(0);
  }

  public void transmit(TransmissonPacket packet) {
    nrTransmissions.incrementAndGet();
    if (Math.random() > erasureProbability)
      callback.call(this, packet);
  }

  public long getNrTransmissions() {
    return nrTransmissions.get();
  }

}
