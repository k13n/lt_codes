package com.k13n.lt_codes;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
  private final Decoder decoder;
  private final Queue<TransmissonPacket> queue;
  private Thread decodingThread;

  public Client() {
    queue = new ConcurrentLinkedQueue<>();
    decoder = setUpDecoder();
  }

  private Decoder setUpDecoder() {
    // FIXME
    return null;
  }

  public synchronized void startProcessing() {
    decodingThread = new Thread() {
      @Override public void run() {
        while (!isInterrupted()) {
          if (!queue.isEmpty()) {
            // FIXME change the neighbor array
            decoder.receive(queue.poll());
          } else {
            sleepMillis(100);
          }
        }
      }
    };
    decodingThread.start();
  }

  public synchronized void stopProcessing() {
    decodingThread.interrupt();
  }

  public void receive(TransmissonPacket packet) {
    queue.offer(packet);
  }

  private static void sleepMillis(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
