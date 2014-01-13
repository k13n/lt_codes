package lt_codes.util;

import java.io.File;

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
    return channel.getNrTransmissions() >= packetsToTransmit;
  }

}
