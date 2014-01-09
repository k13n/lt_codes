package com.k13n.lt_codes.example;

import java.io.File;

import com.k13n.lt_codes.ErasureChannel;

public class FountainServer extends Server {

  public FountainServer(File file, ErasureChannel channel) {
    super(file, channel);
  }

  @Override
  protected boolean transferFinished() {
    return false;
  }

}
