package com.voyager.opt.metaheuristics.gap.tabu;

import com.voyager.opt.metaheuristics.gap.GapInstance;
import com.voyager.opt.metaheuristics.gap.GapInstanceReader;
import com.voyager.opt.metaheuristics.gap.GapSolution;
import com.voyager.opt.metaheuristics.utils.PerfRecord;
import com.voyager.opt.metaheuristics.utils.PerfRecordsWriter;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Getter
@Setter
public final class GapTabuSearch {
  /**
   * instance to be solved
   */
  private final GapInstance instance;
  /**
   * random number generator
   */
  private final Random random;
  /**
   * tabu table
   */
  private final int[][] tabuTable;
  /**
   * best solution
   */
  private GapSolution bestSolution;
  private List<PerfRecord<Integer>> perfRecords;

  public GapTabuSearch(GapInstance instance) {
    this.instance = instance;
    this.random = new Random(42);
    int numTasks = instance.getNumTasks();
    this.tabuTable = new int[numTasks][instance.getNumAgents()];
    for (int i = 0; i < numTasks; i++) {
      Arrays.fill(this.tabuTable[i], 0);
    }

    this.bestSolution = null;
    this.perfRecords = new ArrayList<>();
  }

  public void solve() {
    // penalty factor for capacity violation
    int capacityViolationPenalty = 1000;

    // tabu search parameters
    int neighSize = 100;
    int tabuLength = 100;

    // stopping criteria
    int maxIter = 2000;
    int maxIterNoImprove = 500;

    // create a starting solution
    GapSolution currSolution = new GapSolution(this.instance);
    currSolution.initialize(this.random);
    currSolution.computeObjective(capacityViolationPenalty);
    this.bestSolution = currSolution;
    this.perfRecords.add(new PerfRecord<>(0,
      currSolution.getObjective(),
      bestSolution.getObjective()));

    int numTasks = this.instance.getNumTasks();
    int numAgents = this.instance.getNumAgents();

    // main workflow
    int iter = 0;
    int iterNoImprove = 0;
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

      boolean currSolutionUpdated = false;
      boolean bestSolutionUpdated = false;
      // check tabu criteria
      for (GapSolutionNeighbor neighbor : neighbors) {
        GapSolution newSolution = neighbor.getNewSolution();
        int mutatedTaskIdx = neighbor.getMutatedTaskIdx();
        int newAgentIdx = neighbor.getNewAgentIdx();

        if (this.tabuTable[mutatedTaskIdx][newAgentIdx] < iter) {
          // this move is not tabooed, proceed
          currSolution = newSolution;
          currSolutionUpdated = true;
          this.tabuTable[mutatedTaskIdx][newAgentIdx] = iter + tabuLength;

          if (currSolution.getObjective() < this.bestSolution.getObjective()) {
            this.bestSolution = currSolution;
            bestSolutionUpdated = true;
            break;
          }
        } else {
          // check aspiration criterion
          if (newSolution.getObjective() < bestSolution.getObjective()) {
            currSolution = newSolution;
            bestSolution = currSolution;
            currSolutionUpdated = true;
            bestSolutionUpdated = true;
            this.tabuTable[mutatedTaskIdx][newAgentIdx] = iter + tabuLength;
            break;
          }
        }
      }

      // in case no move is possible, choose the best neighbor
      if (!currSolutionUpdated) {
        GapSolutionNeighbor neighbor = neighbors.getFirst();
        currSolution = neighbor.getNewSolution();
        this.tabuTable[neighbor.getMutatedTaskIdx()][neighbor.getNewAgentIdx()] = iter + tabuLength;
      }

      iter++;
      this.perfRecords.add(new PerfRecord<>(iter,
        currSolution.getObjective(),
        bestSolution.getObjective()));

      // check stopping criteria
      iterNoImprove = bestSolutionUpdated ? 0 : iterNoImprove + 1;
      if (iter >= maxIter || iterNoImprove >= maxIterNoImprove) {
        break;
      }
    }
  }

  public void savePerfRecords(String filename) {
    PerfRecordsWriter.write(filename, perfRecords);
  }

  public static void main(String[] args) throws IOException, URISyntaxException {
    File file = new File("src/main/resources/data/gap/gap1.txt");
    String filePath = file.getAbsolutePath();
    String outputFilename = "/Users/klian/dev/books/metaheuristics-java/data/gap/perf_records.csv";
    List<GapInstance> instances = GapInstanceReader.read(filePath);

    GapInstance instance = instances.get(1);
    GapTabuSearch tabuSearch = new GapTabuSearch(instance);
    tabuSearch.solve();
    tabuSearch.savePerfRecords(outputFilename);
  }
}
