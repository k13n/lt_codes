package com.k13n.lt_codes.core;

import java.util.Random;

public class RobustSolitonGenerator {
  private Random random;
  private int nrBlocks; // k
  private int spike; // M
  private double failureProbability; // delta
  private double normalizationFactor; // beta
  private double c;
  private double R;

  public RobustSolitonGenerator(int nrBlocks, double c,
      double failureProbability, long seed) {
    checkNrBlocks(nrBlocks);
    checkConstantC(c);
    checkFailureProbability(failureProbability);
    this.nrBlocks = nrBlocks;
    this.c = c;
    this.failureProbability = failureProbability;
    random = new Random(seed);
    R = computeR();
    spike = computeSpikePosition();
    normalizationFactor = computeNormalizationFactor();
  }

  public RobustSolitonGenerator(int nrBlocks, double c,
      double failureProbability) {
    this(nrBlocks, c, failureProbability, new Random().nextLong());
  }

  private double computeR() {
    return c * Math.log(nrBlocks / failureProbability) * Math.sqrt(nrBlocks);
  }

  private int computeSpikePosition() {
    return (int) Math.floor(nrBlocks / R);
  }

  public RobustSolitonGenerator(int nrBlocks, int spike,
      double failureProbability, long seed) {
    checkNrBlocks(nrBlocks);
    checkSpike(spike, nrBlocks);
    checkFailureProbability(failureProbability);
    this.nrBlocks = nrBlocks;
    this.spike = spike;
    this.failureProbability = failureProbability;
    random = new Random(seed);
    R = nrBlocks / ((double) spike);
    normalizationFactor = computeNormalizationFactor();
  }

  public RobustSolitonGenerator(int nrBlocks, int spike,
      double failureProbability) {
    this(nrBlocks, spike, failureProbability, new Random().nextLong());
  }

  private double computeNormalizationFactor() {
    double sum = 0;
    for (int i = 1; i <= nrBlocks; i++)
      sum += idealSoliton(i) + unnormalizedRobustSoliton(i);
    return sum;
  }

  public int next() {
    double u = random.nextDouble();
    return inverseTransformSampling(u);
  }

  private int inverseTransformSampling(double u) {
    double sum = 0;
    int index = 1;
    while (sum <= u)
      sum += normalizedRobustSoliton(index++);
    return index - 1;
  }

  private double normalizedRobustSoliton(int i) {
    return (idealSoliton(i) + unnormalizedRobustSoliton(i))
        / normalizationFactor;
  }

  private double unnormalizedRobustSoliton(int i) {
    if (1 <= i && i <= spike - 1)
      return 1.0 / (i * spike);
    else if (i == spike)
      return Math.log(R / failureProbability) / spike;
    else
      return 0;
  }

  private double idealSoliton(int i) {
    if (i == 1)
      return 1.0 / nrBlocks;
    else
      return 1.0 / (i * (i - 1));
  }

  private void checkNrBlocks(int nrBlocks) {
    if (nrBlocks <= 0)
      throw new IllegalArgumentException(
          "number of blocks must be strictly positive");
  }

  private void checkConstantC(double c) {
    if (c <= 0)
      throw new IllegalArgumentException("the constant c must be positive");
  }

  private void checkFailureProbability(double failureProbability) {
    if (failureProbability < 0 || failureProbability > 1)
      throw new IllegalArgumentException(
          "the admissible failure probability must be within the range [0,1]");
  }

  private void checkSpike(int spike, int nrBlocks) {
    if (spike <= 0)
      throw new IllegalArgumentException(
          "the parameter spike must be strictly positive");
    if (spike > nrBlocks)
      throw new IllegalArgumentException(
          "the parameter spike must be less than or equal to the number of blocks");
  }

}
