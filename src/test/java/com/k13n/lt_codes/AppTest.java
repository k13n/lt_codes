package com.k13n.lt_codes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import com.k13n.lt_codes.*;
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
      InputStream is = getClass().getResourceAsStream("/test.txt");
      URL url = this.getClass().getResource("/test.txt");
      assertNotNull(url);
      File file = new File(url.getFile());

      byte[] data = new byte[(int)file.length()];
      DataInputStream s = new DataInputStream(is);
      s.readFully(data);
      s.close();

      Encoder enc = new Encoder(data, 1000);
      final Decoder dec = new Decoder(enc.getSeed(), enc.getNPackets());

      enc.encode(new Encoder.Callback(){
        public boolean call(Encoder encoder, int[] neighbours, byte data[]) {
          return dec.receive(data, neighbours);
        }
      });

      dec.write(new FileOutputStream("/tmp/test.txt.out"));

      //assertFilesEqual("text.txt", "text.txt.out")
    }
}
