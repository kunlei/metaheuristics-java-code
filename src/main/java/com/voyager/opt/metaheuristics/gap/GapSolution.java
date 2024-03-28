package com.voyager.opt.metaheuristics.gap;

import lombok.Getter;

import java.util.Arrays;
import java.util.Random;

@Getter
public final class GapSolution {
  private final GapInstance instance;

  private final int[] agentAssignments;

  private int objective;

  public GapSolution(GapInstance instance) {
    this.instance = instance;
    this.agentAssignments = new int[this.instance.getNumTasks()];
    this.objective = 0;
  }

  public GapSolution(GapSolution other) {
    this.instance = other.instance;
    this.agentAssignments = new int[this.instance.getNumTasks()];
    System.arraycopy(other.agentAssignments, 0, this.agentAssignments, 0, this.instance.getNumTasks());
    this.objective = other.objective;
  }

  public void initialize(Random random) {
    int numTasks = this.instance.getNumTasks();
    int numAgents = this.instance.getNumAgents();
    for (int i = 0; i < numTasks; i++) {
      this.agentAssignments[i] = random.nextInt(numAgents);
    }
  }

  public void computeObjective(int capacityViolationPenalty) {
    int numTasks = this.instance.getNumTasks();
    int numAgents = this.instance.getNumAgents();
    int[][] costs = this.instance.getCosts();
    int[][] resources = this.instance.getResources();
    int[] capacities = this.instance.getCapacities();
    int[] consumedCapacities = new int[numAgents];
    Arrays.fill(consumedCapacities, 0);

    this.objective = 0;
    // compute assignment costs
    for (int i = 0; i < numTasks; i++) {
      int assignedAgent = agentAssignments[i];
      consumedCapacities[assignedAgent] += resources[assignedAgent][i];
      this.objective += costs[assignedAgent][i];
    }

    // compute capacity violation costs
    for (int i = 0; i < numAgents; i++) {
      this.objective += capacityViolationPenalty * Math.max(0, consumedCapacities[i] - capacities[i]);
    }
  }

  public int getAssignedAgent(int taskIdx) {
    return this.agentAssignments[taskIdx];
  }

  public void setAssignedAgent(int taskIdx, int agentIdx) {
    this.agentAssignments[taskIdx] = agentIdx;
  }
}
