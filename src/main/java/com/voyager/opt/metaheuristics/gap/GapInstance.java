package com.voyager.opt.metaheuristics.gap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GapInstance {
  /**
   * total number of tasks
   */
  private int numTasks;
  /**
   * total number of agents
   */
  private int numAgents;
  /**
   * costs of assigning tasks to agents
   */
  private int[][] costs;
  /**
   * resource consumption of assigning tasks to agents
   */
  private int[][] resources;
  /**
   * agent capacities
   */
  private int[] capacities;
}