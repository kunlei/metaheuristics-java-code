package com.voyager.opt.metaheuristics.gap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GapInstance {
  private int numTasks;
  private int numAgents;
  private int[][] costs;
  private int[][] resources;
  private int[] capacities;
}