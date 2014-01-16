package lt_codes.broadcast;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

class CliArguments {
  @Parameter(names = { "-h", "--help" }, help = true,
      description = "Print this help message")
  private Boolean help = false;

  @Parameter(names = { "-v", "--verbose" },
      description = "Turn on verbose output")
  private Boolean verbose = false;

  @Parameter(names = { "-p", "--port" },
      description = "Port used to open socket")
  private Integer port = BroadcastSender.DEFAULT_PORT;

  @Parameter(names = { "-a", "--address" },
      description = "IP broadcast address")
  private String ipAddress = BroadcastSender.DEFAULT_BROADCAST_ADDRESS;

  @Parameter(description = "Filename")
  private List<String> filenames = new ArrayList<>();

  public Integer getPort() {
    return port;
  }

  public String getBroadcastIpAddress() {
    return ipAddress;
  }

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

  public boolean isVerbose() {
    return verbose;
  }

}
