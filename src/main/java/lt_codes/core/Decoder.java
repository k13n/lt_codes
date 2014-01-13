package lt_codes.core;

import java.io.IOException;
import java.io.OutputStream;

public interface Decoder {

  public boolean receive(DecodedPacket packet);
  public void write(OutputStream stream) throws IOException;
  public boolean isDecodingFinished();
}
