package com.k13n.lt_codes;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {
  private final Decoder decoder;
  private ExecutorService executor;
  private Future<Boolean> lastJobFuture;

  public Client() {
    executor = Executors.newSingleThreadExecutor();
    decoder = new IncrementalDecoder(Server.DEFAULT_PACKET_SIZE);
  }

  public synchronized void stopProcessing() {
    try {
      lastJobFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    } finally {
      executor.shutdownNow();
    }
  }

  public void receive(final TransmissonPacket packet) {
    if (decoder.isDecodingFinished())
      return;

    lastJobFuture = executor.submit(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return decoder.receive(packet);
      }
    });
  }

  public boolean transferSucceeded() {
    return decoder.isDecodingFinished();
  }

}
