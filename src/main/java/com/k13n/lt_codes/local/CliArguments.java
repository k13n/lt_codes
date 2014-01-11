package com.k13n.lt_codes.local;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class CliArguments {
  @Parameter(names = { "-h", "--help" }, help = true,
      description = "Print this help message")
  private Boolean help = false;

  @Parameter(names = { "-e", "--erasure_probability" },
      description = "Probability that the channel loses a packet")
  private Double erasureProbability = InMemoryFileTransfer.DEFAULT_ERASURE_PROBABILITY;

  @Parameter(names = { "-o", "--packet_overhead" },
      description = "Defines the percentage of the total number of packets that "
          + "should be transferred. Reasonable values are 1.2 meaning that 20% "
          + "more packets are transferred than the origianl file has")
  private Double packetOverhead = InMemoryFileTransfer.DEFAULT_PACKET_OVERHEAD;

  @Parameter(description = "Filename")
  private List<String> filenames = new ArrayList<>();

  public String getFilename() {
    if (filenames.isEmpty())
      throw new ParameterException("Main parameters cannot be left blank");
    return filenames.get(0);
  }

  public boolean hasFilename() {
    return !filenames.isEmpty();
  }

  public Boolean getHelp() {
    return help;
  }

  public Double getErasureProbability() {
    return erasureProbability;
  }

  public Double getPacketOverhead() {
    return packetOverhead;
  }

}
