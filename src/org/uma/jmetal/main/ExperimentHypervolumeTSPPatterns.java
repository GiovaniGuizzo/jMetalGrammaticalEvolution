package org.uma.jmetal.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.uma.jmetal.measure.qualityindicator.HypervolumeCalculator;
import org.uma.jmetal.measure.statistic.KruskalWallisTest;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;

public class ExperimentHypervolumeTSPPatterns {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        args = new String[]{"0", "experiments/", "NSGA-II,NSGA-II_jMetal"};
        int generations = Integer.parseInt(args[0]);
        String[] algorithms = args[2].split(",");
        try (FileWriter tableWriter = new FileWriter(args[1] + "/TABLE.txt")) {
            HashMap<String, String[]> problemsHash = new HashMap<>();
            problemsHash.put("kroAB100", new String[]{"kroA100.tsp", "kroB100.tsp"});
            problemsHash.put("kroAC100", new String[]{"kroA100.tsp", "kroC100.tsp"});
            problemsHash.put("kroAD100", new String[]{"kroA100.tsp", "kroD100.tsp"});
            problemsHash.put("kroAE100", new String[]{"kroA100.tsp", "kroE100.tsp"});
            problemsHash.put("kroBC100", new String[]{"kroB100.tsp", "kroC100.tsp"});
            problemsHash.put("kroBD100", new String[]{"kroB100.tsp", "kroD100.tsp"});
            problemsHash.put("kroBE100", new String[]{"kroB100.tsp", "kroE100.tsp"});
            problemsHash.put("kroCD100", new String[]{"kroC100.tsp", "kroD100.tsp"});
            problemsHash.put("kroCE100", new String[]{"kroC100.tsp", "kroE100.tsp"});
            problemsHash.put("kroDE100", new String[]{"kroD100.tsp", "kroE100.tsp"});
            problemsHash.put("kroAB150", new String[]{"kroA150.tsp", "kroB150.tsp"});
            problemsHash.put("kroAB200", new String[]{"kroA200.tsp", "kroB200.tsp"});

            ArrayList<String> problems = new ArrayList<>(problemsHash.keySet());
            problems.sort(String::compareToIgnoreCase);

            tableWriter.write("Problem & ");
            tableWriter.append(
                    Arrays.asList(algorithms)
                            .stream()
                            .collect(Collectors.joining(" & ")));
            tableWriter.write(" \\\\\n\\hline\n");

            for (String problem : problems) {
                tableWriter.append(problem + " & ");
                File inputDir = new File(args[1] + "/" + problem);
                HypervolumeCalculator calculator = new HypervolumeCalculator();
                HypervolumeCalculator generationalCalculator = new HypervolumeCalculator();

                List<File> algorithmVars = new ArrayList<>();
                for (String algorithm : algorithms) {
                    File file = new File(inputDir.getPath() + "/" + algorithm);
                    algorithmVars.add(file);
                }

                for (File algorithmDir : algorithmVars) {
                    List<DoubleSolution> algorithmSolutions = new ArrayList();
                    for (File executionDir : algorithmDir.listFiles(file -> file.isDirectory())) {
                        File fun = executionDir.listFiles((dir, fileName) -> fileName.startsWith("FUN"))[0];
                        calculator.addParetoFront(fun.getPath());
                        algorithmSolutions.addAll(FrontUtils.convertFrontToSolutionList(new ArrayFront(fun.getPath())));

                        if (generations > 0) {
                            File[] fronts = executionDir.listFiles((dir, fileName) -> fileName.endsWith(".txt") && !fileName.contains("VAR") && !fileName.contains("TIME") && !fileName.contains("FUN"));
                            for (File front : fronts) {
                                generationalCalculator.addParetoFront(front.getPath());
                            }
                        }
                    }
                    algorithmSolutions = SolutionListUtils.getNondominatedSolutions(algorithmSolutions);
                    SolutionSetOutput.printObjectivesToFile(algorithmSolutions, algorithmDir.getPath() + "/FUN_ALL.txt");
                }

                HashMap<String, Double[]> values = new HashMap<>();
                HashMap<String, Double> means = new HashMap<>();
                for (File algorithmDir : algorithmVars) {
                    List<Double> hypervolumes = new ArrayList<>();
                    for (File executionDir : algorithmDir.listFiles(file -> file.isDirectory())) {
                        File fun = executionDir.listFiles((dir, fileName) -> fileName.startsWith("FUN"))[0];
                        hypervolumes.add(calculator.calculateHypervolume(fun.getPath()));

                        if (generations > 0) {
                            try (FileWriter hvWriter = new FileWriter(executionDir.getPath() + "/GEN_HV.txt")) {
                                for (int i = 0; i < generations; i++) {
                                    hvWriter.write(String.valueOf(generationalCalculator.calculateHypervolume(executionDir.getPath() + "/" + i + ".txt")));
                                    hvWriter.write("\n");
                                }
                            }
                        }
                    }
                    Double mean = hypervolumes.stream().reduce(Double::sum).get() / hypervolumes.size();
                    means.put(algorithmDir.getName(), mean);
                    try (FileWriter hvWriter = new FileWriter(algorithmDir.getPath() + "/HYPERVOLUME.txt")) {
                        hvWriter.write(String.valueOf(mean));
                        hvWriter.write("\n");
                    }
                    Double[] hypervolumeArray = new Double[hypervolumes.size()];
                    values.put(algorithmDir.getName(), hypervolumes.toArray(hypervolumeArray));
                }

                HashMap<String, HashMap<String, Boolean>> result = KruskalWallisTest.test(values);

                Double maxMean = Double.NEGATIVE_INFINITY;
                String maxMeanAlgorithm = null;
                for (File algorithmDir : algorithmVars) {
                    Double mean = means.get(algorithmDir.getName());
                    if (mean > maxMean) {
                        maxMean = mean;
                        maxMeanAlgorithm = algorithmDir.getName();
                    }
                }
                final String reallyMaxMeanAlgorithm = maxMeanAlgorithm;

                DecimalFormat df = new DecimalFormat("0.00E0");

                String tableLine = algorithmVars.stream()
                        .map(algorithmDir -> {
                            String value = "";
                            boolean bold = reallyMaxMeanAlgorithm.equals(algorithmDir.getName()) || !result.get(reallyMaxMeanAlgorithm).get(algorithmDir.getName());
                            if (bold) {
                                value += "\\textbf{";
                            }
                            value += df.format(means.get(algorithmDir.getName()));
                            if (bold) {
                                value += "}";
                            }
                            return value;
                        })
                        .collect(Collectors.joining(" & "));
                tableWriter.write(tableLine + " \\\\\n");
            }
        }
    }
}
