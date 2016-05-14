package org.uma.jmetal.main;

import org.uma.jmetal.measure.statistic.KruskalWallisTest;
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
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;

public class ExperimentHypervolume {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        args = new String[]{"0", "experiment/CITO/testing/", "NSGAII,SPEA2,"
            //                + "ALG_0,ALG_1,ALG_2,ALG_3,ALG_4,ALG_5,ALG_6,ALG_7,ALG_8,ALG_9,"
            + "ALG_6,"
            //                + "IRACE_0,IRACE_1,IRACE_2,IRACE_3,IRACE_4,IRACE_5,IRACE_6,IRACE_7,IRACE_8,IRACE_9"};
            + "IRACE_7"};
        int generations = Integer.parseInt(args[0]);
        String[] algorithms = args[2].split(",");
        try (FileWriter tableWriter = new FileWriter(args[1] + "/TABLE.txt")) {
            //problem instance
            String[] problems = {"OO_MyBatis",
                "OA_AJHsqldb",
                "OA_AJHotDraw",
                "OO_BCEL",
                "OO_JHotDraw",
                "OA_HealthWatcher",
                "OA_TollSystems",
                "OO_JBoss"};

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
