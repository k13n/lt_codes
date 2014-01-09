package com.k13n.lt_codes.example;

import java.io.File;

import com.k13n.lt_codes.ErasureChannel;

public class FixedRateServer extends Server {
  private final int packetsToTransmit;

  public FixedRateServer(File file, ErasureChannel channel,
      double packetOverhead) {
    super(file, channel);
    if (packetOverhead < 1)
      throw new IllegalArgumentException("The packet overhead must be >= 1");

    this.packetsToTransmit = (int) (encoder.getNPackets() * packetOverhead);
  }

  @Override
  protected boolean transferFinished() {
    return channel.getNrTransmissions() <= packetsToTransmit;
  }

}
