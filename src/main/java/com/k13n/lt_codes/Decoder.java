package com.k13n.lt_codes;

import java.io.IOException;
import java.io.OutputStream;

public interface Decoder {

  public boolean receive(TransmissonPacket packet);
  public void write(OutputStream stream) throws IOException;
  public boolean isDecodingFinished();
}
