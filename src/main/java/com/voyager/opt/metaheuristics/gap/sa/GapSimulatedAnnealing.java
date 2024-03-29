package com.voyager.opt.metaheuristics.gap.sa;

import com.voyager.opt.metaheuristics.gap.GapInstance;
import com.voyager.opt.metaheuristics.gap.GapInstanceReader;
import com.voyager.opt.metaheuristics.gap.GapSolution;
import com.voyager.opt.metaheuristics.utils.PerfRecord;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GapSimulatedAnnealing {
  /**
   * instance to be solved
   */
  private final GapInstance instance;
  /**
   * random number generator
   */
  private final Random random;
  /**
   * best solution
   */
  private GapSolution bestSolution;
  private List<PerfRecord<Integer>> perfRecords;

  public GapSimulatedAnnealing(GapInstance instance) {
    this.instance = instance;
    this.random = new Random(42);
    this.bestSolution = null;
    this.perfRecords = new ArrayList<>();
  }

  public void solve() {
    // penalty factor for capacity violation
    int capacityViolationPenalty = 1000;
    double initialTemperature = 1000;
    double coolingRate = 0.9999;
    double endingTemperature = 0.0001;
    int iterationsPerTemperature = 100;

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

    // Set initial temperature
    double temperature = initialTemperature;

    while (temperature > endingTemperature) {
      System.out.println("temperature: " + temperature +
        ", curr_obj: " + currSolution.getObjective() +
        ", best_obj: " + bestSolution.getObjective());
      for (int i = 0; i < iterationsPerTemperature; i++) {
        // Generate neighbor solution
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

        // Calculate cost differences
        int deltaCost = newSolution.getObjective() - currSolution.getObjective();

        // Accept or reject neighbor solution based on Metropolis criterion
        if (deltaCost < 0 || Math.exp(-deltaCost / temperature) > random.nextDouble()) {
          currSolution = newSolution;
        }

        // Update best assignment
        if (currSolution.getObjective() < bestSolution.getObjective()) {
          bestSolution = currSolution;
        }
      }

      // Cool down temperature
      temperature *= coolingRate;
    }
  }

  public static void main(String[] args) throws IOException, URISyntaxException {
    File file = new File("src/main/resources/data/gap/gap1.txt");
    String filePath = file.getAbsolutePath();
    String outputFilename = "/Users/klian/dev/books/metaheuristics-java/data/gap/perf_records.csv";
    List<GapInstance> instances = GapInstanceReader.read(filePath);

    GapInstance instance = instances.get(1);
    GapSimulatedAnnealing simulatedAnnealing = new GapSimulatedAnnealing(instance);
    simulatedAnnealing.solve();
  }
}
