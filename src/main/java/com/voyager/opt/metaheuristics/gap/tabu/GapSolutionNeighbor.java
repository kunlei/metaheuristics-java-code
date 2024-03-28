package com.voyager.opt.metaheuristics.gap.tabu;

import com.voyager.opt.metaheuristics.gap.GapSolution;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GapSolutionNeighbor {
  /**
   * neighboring solution
   */
  private GapSolution newSolution;
  /**
   * the task for which agent assignment is changed
   */
  private int mutatedTaskIdx;
  /**
   * new agent index for the chosen task
   */
  private int newAgentIdx;
}
