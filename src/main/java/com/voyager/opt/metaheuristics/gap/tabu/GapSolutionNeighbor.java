package com.voyager.opt.metaheuristics.gap.tabu;

import com.voyager.opt.metaheuristics.gap.GapSolution;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GapSolutionNeighbor {
  private GapSolution newSolution;
  private int mutatedTaskIdx;
  private int newAgentIdx;
}
