package com.k13n.lt_codes;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
  private final Decoder decoder;
  private final Queue<TransmissonPacket> queue;
  private ExecutorService executor;

  public Client() {
    queue = new ConcurrentLinkedQueue<>();
    decoder = setUpDecoder();
  }

  private Decoder setUpDecoder() {
    return new IncrementalDecoder(Server.DEFAULT_PACKET_SIZE);
  }

  public synchronized void startProcessing() {
    executor = Executors.newSingleThreadExecutor();
    executor.submit(new DecoderJob());
  }

  public synchronized void stopProcessing() {
    while (!queue.isEmpty())
      sleepMillis(100);
    executor.shutdownNow();
  }

  public void receive(TransmissonPacket packet) {
    queue.offer(packet);
  }

  public boolean transferSucceeded() {
    return decoder.isDecodingFinished();
  }

  private void sleepMillis(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignore) { }
  }

  private final class DecoderJob implements Runnable {

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted())
        processNextPacketOrWait();
    }

    private void processNextPacketOrWait() {
      if (!queue.isEmpty())
        decoder.receive(queue.poll());
      else
        sleepMillis(100);
    }

    private void sleepMillis(int millis) {
      try {
        Thread.sleep(millis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

  }

}
