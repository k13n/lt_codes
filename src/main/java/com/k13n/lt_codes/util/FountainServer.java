package com.k13n.lt_codes.util;

import java.io.File;

public class FountainServer extends Server {

  public FountainServer(File file, ErasureChannel channel) {
    super(file, channel);
  }

  @Override
  protected boolean transferFinished() {
    return false;
  }

}
