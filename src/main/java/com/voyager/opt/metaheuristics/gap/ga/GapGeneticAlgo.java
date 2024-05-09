package com.voyager.opt.metaheuristics.gap.ga;

import com.voyager.opt.metaheuristics.gap.GapInstance;
import com.voyager.opt.metaheuristics.gap.GapInstanceReader;
import com.voyager.opt.metaheuristics.gap.GapSolution;
import com.voyager.opt.metaheuristics.utils.PerfRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GapGeneticAlgo {
  private static final int MAX_GENERATIONS = 1000;
  private static final int POPULATION_SIZE = 100;
  private static final double MUTATION_RATE = 0.2;
  private static final int TOURNAMENT_SIZE = 5;

  private final GapInstance instance;
  /**
   * random number generator
   */
  private final Random random;
  /**
   * best solution
   */
  private GapSolution bestSolution;
  // performance scores
  private List<PerfRecord<Integer>> perfRecords;

  private final int capacityViolationPenalty = 10000;

  public GapGeneticAlgo(GapInstance instance) {
    this.instance = instance;
    this.random = new Random(42);
    this.perfRecords = new ArrayList<>();
  }

  public GapSolution solve() {
    // Initialize population
    List<GapSolution> population = initializePopulation();
    bestSolution = selectBestSolution(population);

    // Evolution loop
    for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
      PerfRecord<Integer> perf = collectPerformanceMetrics(population);
      perf.setIteration(generation);
      perfRecords.add(perf);
      System.out.println(perf.toCommaDelimitedString());

      // Crossover
      List<GapSolution> offspring = crossover(population);

      // Mutation
      mutate(offspring);

      // Replace old population with new population
      population.addAll(offspring);
      population.sort(Comparator.comparingInt(GapSolution::getObjective));

      List<GapSolution> nextGenPop = new ArrayList<>(POPULATION_SIZE);
      for (int i = 0; i < POPULATION_SIZE; i++) {
        nextGenPop.add(population.get(i));
      }
      population = nextGenPop;
    }

    // Select the best solution from the final population
    return selectBestSolution(population);
  }

  /**
   * randomly create a list of solutions
   * @return new solutions
   */
  private List<GapSolution> initializePopulation() {
    List<GapSolution> population = new ArrayList<>(POPULATION_SIZE);
    for (int i = 0; i < POPULATION_SIZE; i++) {
      GapSolution solution = new GapSolution(instance);
      solution.initialize(random);
      solution.computeObjective(capacityViolationPenalty);
      population.add(solution);
    }
    return population;
  }

  private PerfRecord<Integer> collectPerformanceMetrics(List<GapSolution> population) {
    for (GapSolution solution : population) {
      if (bestSolution == null || solution.getObjective() < bestSolution.getObjective()) {
        this.bestSolution = solution;
      }
    }

    double avgObj = population.stream()
      .mapToInt(GapSolution::getObjective)
      .average()
      .getAsDouble();
    return new PerfRecord<Integer>(0, (int) avgObj, bestSolution.getObjective());
  }

  private GapSolution selectParent(List<GapSolution> population) {
    GapSolution champion = population.get(random.nextInt(POPULATION_SIZE));
    for (int i = 0; i < TOURNAMENT_SIZE - 1; i++) {
      GapSolution challenger = population.get(random.nextInt(POPULATION_SIZE));
      if (challenger.getObjective() < champion.getObjective()) {
        champion = challenger;
      }
    }
    return champion;
  }

  private List<GapSolution> crossover(List<GapSolution> parents) {
    // Here, we can use different crossover techniques such as one-point crossover or uniform crossover
    // For simplicity, let's use one-point crossover
    List<GapSolution> offspring = new ArrayList<>(POPULATION_SIZE);
    for (int i = 0; i < POPULATION_SIZE; i++) {
      GapSolution parent1 = selectParent(parents);
      GapSolution parent2 = selectParent(parents);

      int crossoverPoint = random.nextInt(instance.getNumTasks() - 1) + 1; // Ensure crossoverPoint is not 0

      // Create offspring by swapping genes between parents
      GapSolution offspring1 = new GapSolution(parent1);
      GapSolution offspring2 = new GapSolution(parent2);

      for (int j = crossoverPoint; j < instance.getNumTasks(); j++) {
        int temp = offspring1.getAssignedAgent(j);
        offspring1.setAssignedAgent(j, offspring2.getAssignedAgent(j));
        offspring2.setAssignedAgent(j, temp);
      }

      offspring1.computeObjective(capacityViolationPenalty);
      offspring2.computeObjective(capacityViolationPenalty);

      offspring.add(offspring1);
      offspring.add(offspring2);
    }

    return offspring;
  }

  private void mutate(List<GapSolution> population) {
    for (GapSolution solution : population) {
      if (random.nextDouble() < MUTATION_RATE) {
        for (int taskIdx = 0; taskIdx < instance.getNumTasks(); taskIdx++) {
          if (random.nextDouble() < MUTATION_RATE / 2.0) {
            int newAgentIdx = random.nextInt(instance.getNumAgents());
            solution.setAssignedAgent(taskIdx, newAgentIdx);
          }
        }
        solution.computeObjective(capacityViolationPenalty);
      }
    }
  }

  private GapSolution selectBestSolution(List<GapSolution> population) {
    // Find the best solution (solution with the lowest objective value) in the population
    GapSolution bestSolution = population.getFirst();
    for (GapSolution solution : population) {
      if (solution.getObjective() < bestSolution.getObjective()) {
        bestSolution = solution;
      }
    }
    return bestSolution;
  }

  public static void main(String[] args) {
    File file = new File("src/main/resources/data/gap/gap1.txt");
    String filePath = file.getAbsolutePath();
    String outputFilename = "/Users/klian/dev/books/metaheuristics-java/data/gap/perf_records.csv";
    List<GapInstance> instances = GapInstanceReader.read(filePath);

    GapInstance instance = instances.get(1);
    GapGeneticAlgo solver = new GapGeneticAlgo(instance);
    GapSolution solution = solver.solve();

    // Output the solution
    System.out.println("Best Solution:" + solution.getObjective());
    // Print solution details
  }
}
