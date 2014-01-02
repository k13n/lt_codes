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
    return new IncrementalDecoder(Server.DEFAULT_PACKET_SIZE);
  }

  public synchronized void startProcessing() {
    decodingThread = new DecoderThread();
    decodingThread.start();
  }

  public synchronized void stopProcessing() {
    while (!queue.isEmpty())
      sleepMillis(100);
    shutDownDecodingThread();
  }

  public void receive(TransmissonPacket packet) {
    queue.offer(packet);
  }

  public boolean transferSucceeded() {
    return decoder.isDecodingFinished();
  }

  private void shutDownDecodingThread() {
    try {
      decodingThread.interrupt();
      decodingThread.join();
    } catch (InterruptedException e) { }
  }

  private void sleepMillis(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) { }
  }

  private final class DecoderThread extends Thread {

    @Override
    public void run() {
      while (!isInterrupted())
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
        this.interrupt();
      }
    }

  }

}
