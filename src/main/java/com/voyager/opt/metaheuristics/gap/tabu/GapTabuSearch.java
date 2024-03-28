package com.voyager.opt.metaheuristics.gap.tabu;

import com.voyager.opt.metaheuristics.gap.GapInstance;
import com.voyager.opt.metaheuristics.gap.GapInstanceReader;
import com.voyager.opt.metaheuristics.gap.GapSolution;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public final class GapTabuSearch {
  private final GapInstance instance;
  private final Random random;
  private final int[][] tabuList;

  private GapSolution bestSolution;

  public GapTabuSearch(GapInstance instance) {
    this.instance = instance;
    this.random = new Random(42);
    this.tabuList = new int[this.instance.getNumTasks()][this.instance.getNumAgents()];
    for (int i = 0; i < this.instance.getNumTasks(); i++) {
      for (int j = 0; j < this.instance.getNumAgents(); j++) {
        this.tabuList[i][j] = 0;
      }
    }

    this.bestSolution = null;
  }

  public void solve() {
    int capacityViolationPenalty = 200;

    int maxIter = 10000;
    int neighSize = 50;
    int tabuLength = 20;

    // create a starting solution
    GapSolution currSolution = new GapSolution(this.instance);
    currSolution.initialize(this.random);
    currSolution.computeObjective(capacityViolationPenalty);
    this.bestSolution = currSolution;

    int numTasks = this.instance.getNumTasks();
    int numAgents = this.instance.getNumAgents();

    // main workflow
    int iter = 0;
    while (true) {
      System.out.println("iter: " + iter + ", best obj: " + bestSolution.getObjective());
      // create neighboring solutions
      List<GapSolutionNeighbor> neighbors = new ArrayList<>(neighSize);
      for (int i = 0; i < neighSize; i++) {
        // create a copy of current solution
        GapSolution newSolution = new GapSolution(currSolution);

        // mutate one task assignment
        int randTaskIdx = this.random.nextInt(numTasks);
        int currAgentIdx = newSolution.getAssignedAgent(randTaskIdx);
        int newAgentIdx = this.random.nextInt(numAgents);
        while (newAgentIdx == currAgentIdx) {
          newAgentIdx = this.random.nextInt(numAgents);
        }
        newSolution.setAssignedAgent(randTaskIdx, newAgentIdx);

        // compute objective value after mutation
        newSolution.computeObjective(capacityViolationPenalty);

        GapSolutionNeighbor neighbor = GapSolutionNeighbor.builder()
          .newSolution(newSolution)
          .mutatedTaskIdx(randTaskIdx)
          .newAgentIdx(newAgentIdx)
          .build();
        neighbors.add(neighbor);
      }

      // sort neighboring solutions
      neighbors.sort(Comparator.comparingInt(neighbor -> neighbor.getNewSolution().getObjective()));

      // check tabu criteria
      for (GapSolutionNeighbor neighbor : neighbors) {
        GapSolution newSolution = neighbor.getNewSolution();
        int mutatedTaskIdx = neighbor.getMutatedTaskIdx();
        int newAgentIdx = neighbor.getNewAgentIdx();

        if (this.tabuList[mutatedTaskIdx][newAgentIdx] < iter) {
          // this move is not tabooed, proceed
          currSolution = newSolution;
          tabuList[mutatedTaskIdx][newAgentIdx] = iter + tabuLength;

          if (currSolution.getObjective() < this.bestSolution.getObjective()) {
            this.bestSolution = currSolution;
          }
        } else {
          // check aspiration criterion
          if (newSolution.getObjective() < bestSolution.getObjective()) {
            currSolution = newSolution;
            bestSolution = currSolution;
            tabuList[mutatedTaskIdx][newAgentIdx] = iter + tabuLength;
          }
        }
      }

      if (iter++ >= maxIter) {
        break;
      }
    }
  }

  public static void main(String[] args) {
    String filename = "/Users/klian/dev/metaheuristics-java-code/src/main/resources/data/gap/gap1.txt";
    List<GapInstance> instances = GapInstanceReader.read(filename);

    GapInstance instance = instances.getFirst();
    GapTabuSearch tabuSearch = new GapTabuSearch(instance);
    tabuSearch.solve();

  }
}
