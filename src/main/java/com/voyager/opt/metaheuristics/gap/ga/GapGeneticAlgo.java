package com.voyager.opt.metaheuristics.gap.ga;

import com.voyager.opt.metaheuristics.gap.GapInstance;
import com.voyager.opt.metaheuristics.gap.GapInstanceReader;
import com.voyager.opt.metaheuristics.gap.GapSolution;
import com.voyager.opt.metaheuristics.utils.PerfRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GapGeneticAlgo {

  private static final double MUTATION_RATE = 0.1;
  private static final int POPULATION_SIZE = 100;
  private static final int MAX_GENERATIONS = 10000;

  private GapInstance instance;
  /**
   * random number generator
   */
  private final Random random;
  /**
   * best solution
   */
  private GapSolution bestSolution;
  private List<PerfRecord<Integer>> perfRecords;

  private int capacityViolationPenalty = 1000;

  public GapGeneticAlgo(GapInstance instance) {
    this.instance = instance;
    this.random = new Random(42);
  }

  public GapSolution solve() {
    // Initialize population
    List<GapSolution> population = initializePopulation();

    // Evolution loop
    for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
      // Select parents
      List<GapSolution> parents = selectParents(population);

      // Crossover
      List<GapSolution> offspring = crossover(parents);

      // Mutation
      mutate(offspring);

      // Replace old population with new population
      population = offspring;

      // Termination condition
      // In this example, we use a fixed number of generations
    }

    // Select the best solution from the final population
    return selectBestSolution(population);
  }

  private List<GapSolution> initializePopulation() {
    List<GapSolution> population = new ArrayList<>();

    for (int i = 0; i < POPULATION_SIZE; i++) {
      GapSolution solution = new GapSolution(instance);
      solution.initialize(random);
      solution.computeObjective(capacityViolationPenalty);
      population.add(solution);
    }

    return population;
  }

  private List<GapSolution> selectParents(List<GapSolution> population) {
    // Here, we can use different selection methods such as roulette wheel selection or tournament selection
    // For simplicity, let's use tournament selection with tournament size = 2
    List<GapSolution> parents = new ArrayList<>();

    for (int i = 0; i < POPULATION_SIZE; i++) {
      GapSolution parent1 = population.get(random.nextInt(POPULATION_SIZE));
      GapSolution parent2 = population.get(random.nextInt(POPULATION_SIZE));
      GapSolution winner = (parent1.getObjective() < parent2.getObjective()) ? parent1 : parent2;
      parents.add(winner);
    }

    return parents;
  }

  private List<GapSolution> crossover(List<GapSolution> parents) {
    // Here, we can use different crossover techniques such as one-point crossover or uniform crossover
    // For simplicity, let's use one-point crossover
    List<GapSolution> offspring = new ArrayList<>();
    Random random = new Random();

    for (int i = 0; i < POPULATION_SIZE; i += 2) {
      GapSolution parent1 = parents.get(i);
      GapSolution parent2 = parents.get(i + 1);

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
    Random random = new Random();

    for (GapSolution solution : population) {
      if (random.nextDouble() < MUTATION_RATE) {
        // Perform mutation by randomly changing a task assignment
        int taskIndex = random.nextInt(instance.getNumTasks());
        int agentIndex = random.nextInt(instance.getNumAgents());
        solution.setAssignedAgent(taskIndex, agentIndex);
        solution.computeObjective(capacityViolationPenalty);
      }
    }
  }

  private GapSolution selectBestSolution(List<GapSolution> population) {
    // Find the best solution (solution with the lowest objective value) in the population
    GapSolution bestSolution = population.get(0);
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
