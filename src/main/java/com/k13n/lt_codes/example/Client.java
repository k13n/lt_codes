package com.k13n.lt_codes.example;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.k13n.lt_codes.DecodedPacket;
import com.k13n.lt_codes.Decoder;
import com.k13n.lt_codes.IncrementalDecoder;

public class Client {
  private final Decoder decoder;
  private ExecutorService executor;

  public Client() {
    executor = Executors.newSingleThreadExecutor();
    decoder = new IncrementalDecoder(Server.DEFAULT_PACKET_SIZE);
  }

  public synchronized void stopProcessing() {
    try {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void receive(final DecodedPacket packet) {
    if (decoder.isDecodingFinished())
      return;

    executor.submit(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return decoder.receive(packet);
      }
    });
  }

  public boolean transferSucceeded() {
    return decoder.isDecodingFinished();
  }

}
