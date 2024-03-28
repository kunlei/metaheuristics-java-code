package com.voyager.opt.metaheuristics.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PerfRecordsWriter {

  public static void write(String filename, List<PerfRecord<Integer>> records) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
      for (PerfRecord<Integer> record : records) {
        String line = record.toSpaceDelimitedString();
        writer.write(line);
        writer.newLine();
      }
    } catch (IOException e) {
      System.err.println("An error occurred while writing to the file: " + e.getMessage());
    }
  }
}
