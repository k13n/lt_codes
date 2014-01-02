package com.k13n.lt_codes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.net.URL;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws Exception
    {
        assertWorksWithPerfectChannel();
        assertTrue( true );
    }

    private void assertWorksWithPerfectChannel() throws Exception {
      String filename = "test.txt";
      InputStream is = getClass().getResourceAsStream("/" + filename);
      URL url = this.getClass().getResource("/" + filename);
      assertNotNull(url);
      File file = new File(url.getFile());

      byte[] data = new byte[(int)file.length()];
      DataInputStream s = new DataInputStream(is);
      s.readFully(data);
      s.close();

      int packetSize = 100;
      Encoder enc = new Encoder(data, packetSize);
      final Decoder dec = new DefaultDecoder(enc.getSeed(), enc.getNPackets());

      enc.encode(new Encoder.Callback(){
        public boolean call(Encoder encoder, TransmissonPacket packet) {
          return dec.receive(packet);
        }
      });

      dec.write(new FileOutputStream("/tmp/" + filename + ".out"));

      //assertFilesEqual("text.txt", "text.txt.out")
    }
}
