package com.voyager.opt.metaheuristics.gap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GapInstanceReader {

  public static List<GapInstance> read(String filename) {
    List<GapInstance> instances = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      int numInstances = Integer.parseInt(reader.readLine().trim());

      for (int p = 0; p < numInstances; p++) {
        String[] mn = reader.readLine().trim().split(" ");
        int numAgents = Integer.parseInt(mn[0]); // Number of agents
        int numTasks = Integer.parseInt(mn[1]); // Number of tasks

        // Reading costs
        int[][] costs = new int[numAgents][numTasks];
        for (int i = 0; i < numAgents; i++) {
          String[] costsLine = reader.readLine().trim().split(" ");
          for (int j = 0; j < numTasks; j++) {
            costs[i][j] = Integer.parseInt(costsLine[j]);
          }
        }

        // Reading resources
        int[][] resources = new int[numAgents][numTasks];
        for (int i = 0; i < numAgents; i++) {
          String[] resourcesLine = reader.readLine().trim().split(" ");
          for (int j = 0; j < numTasks; j++) {
            resources[i][j] = Integer.parseInt(resourcesLine[j]);
          }
        }

        // Reading resource capacities
        int[] agentCapacities = new int[numAgents];
        String[] capacitiesLine = reader.readLine().trim().split(" ");
        for (int i = 0; i < numAgents; i++) {
          agentCapacities[i] = Integer.parseInt(capacitiesLine[i]);
        }

        GapInstance instance = GapInstance.builder()
          .numTasks(numTasks)
          .numAgents(numAgents)
          .costs(costs)
          .resources(resources)
          .capacities(agentCapacities)
          .build();
        instances.add(instance);
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return instances;
  }
}
