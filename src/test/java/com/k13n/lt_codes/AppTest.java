package com.k13n.lt_codes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import com.k13n.lt_codes.*;

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
    public void testApp()
    {
        assertWorksWithPerfectChannel();
        assertTrue( true );
    }

    private void assertWorksWithPerfectChannel() {
      File testFile = new File("text.txt");
      byte[] data = new byte[(int) file.length()];
      DataInputStream s = new DataInputStream(new FileInputStream(testFile));
      s.readFully(data);
      s.close();

      Encoder enc = new Encoder(data, 1000, 0.1);
      Decoder dec = new Decoder(enc.getSeed(), enc.getNPackets());

      enc.encode(new Encoder.Callback(){
        public boolean call(Encoder encoder, int[] neighbours, byte data[]) {
          return !decoder.receive(data, neighbours);
        }
      });

      dec.write(new FileOutputStream("test.txt.out"));

      //assertFilesEqual("text.txt", "text.txt.out")
    }
}
