package com.voyager.opt.metaheuristics.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PerfRecord<T> {
  private int iteration;
  private T currSolutionObj;
  private T bestSolutionObj;

  public String toCommaDelimitedString() {
    return iteration + ", " + currSolutionObj + ", " + bestSolutionObj;
  }
}
