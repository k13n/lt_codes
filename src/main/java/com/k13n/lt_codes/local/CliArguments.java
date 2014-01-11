package com.k13n.lt_codes.local;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class CliArguments {
  @Parameter(names = { "-h", "--help" }, help = true)
  private Boolean help = false;

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

}
