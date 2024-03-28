package com.voyager.opt.metaheuristics.gap;

import lombok.Getter;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

@Getter
public final class GapSolution {
  /**
   * reference to the instance to be solved
   */
  private final GapInstance instance;
  /**
   * dimension: 1 * numTasks
   * assigned agent index for each task
   */
  private final int[] agentAssignments;
  /**
   * dimension: 1 * numAgents
   * consumed capacity of each agent
   */
  private final int[] consumedCapacities;
  /**
   * total objective value
   */
  private int objective;
  /**
   * assignment cost, without penalties
   */
  private int assignmentCost;
  /**
   * capacity violation penalties of all agents
   */
  private int capacityViolationPenalty;

  public GapSolution(GapInstance instance) {
    this.instance = instance;
    this.agentAssignments = new int[this.instance.getNumTasks()];
    this.consumedCapacities = new int[this.instance.getNumAgents()];
    Arrays.fill(this.agentAssignments, 0);
    Arrays.fill(consumedCapacities, 0);
    this.objective = 0;
    this.assignmentCost = 0;
    this.capacityViolationPenalty = 0;
  }

  /**
   * copy constructor
   * @param other the other solution to copy from
   */
  public GapSolution(GapSolution other) {
    this.instance = other.instance;
    this.agentAssignments = new int[this.instance.getNumTasks()];
    System.arraycopy(other.agentAssignments, 0,
      this.agentAssignments, 0,
      this.instance.getNumTasks());
    this.consumedCapacities = new int[this.instance.getNumAgents()];
    System.arraycopy(other.consumedCapacities, 0,
      this.consumedCapacities, 0,
      this.instance.getNumAgents());
    this.objective = other.objective;
    this.assignmentCost = other.assignmentCost;
    this.capacityViolationPenalty = other.capacityViolationPenalty;
  }

  /**
   * randomly assign tasks to agents
   * @param random random number generator
   */
  public void initialize(Random random) {
    int[][] resources = this.instance.getResources();
    for (int i = 0; i < instance.getNumTasks(); i++) {
      int agentIdx = random.nextInt(instance.getNumAgents());
      this.agentAssignments[i] = agentIdx;
      this.consumedCapacities[agentIdx] += resources[agentIdx][i];
    }
  }

  /**
   * compute objective values
   * @param capacityViolationPenalty penalty factor
   */
  public void computeObjective(int capacityViolationPenalty) {
    // compute assignment costs
    this.assignmentCost = IntStream.range(0, instance.getNumTasks())
      .map(taskIdx -> instance.getCosts()[agentAssignments[taskIdx]][taskIdx])
      .sum();

    // compute capacity violation costs
    this.capacityViolationPenalty = IntStream.range(0, instance.getNumAgents())
      .map(agentIdx -> capacityViolationPenalty *
        Math.max(0, this.consumedCapacities[agentIdx] - instance.getCapacities()[agentIdx]))
      .sum();

    this.objective = this.assignmentCost + this.capacityViolationPenalty;
  }

  public int getAssignedAgent(int taskIdx) {
    return this.agentAssignments[taskIdx];
  }

  /**
   * assign agent to task
   * @param taskIdx the task to be assigned
   * @param agentIdx the agent index
   */
  public void setAssignedAgent(int taskIdx, int agentIdx) {
    int currAgentIdx = this.agentAssignments[taskIdx];
    this.agentAssignments[taskIdx] = agentIdx;
    this.consumedCapacities[currAgentIdx] -= instance.getResources()[currAgentIdx][taskIdx];
    this.consumedCapacities[agentIdx] += instance.getResources()[agentIdx][taskIdx];
  }
}
