/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uma.jmetal.main;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import org.uma.jmetal.measure.qualityindicator.HypervolumeCalculator;
import org.uma.jmetal.measure.statistic.VarghaDelaneyEffectSize;

/**
 *
 * @author giovani
 */
public class ComputeEffectSize {

    public static void main(String[] args) throws IOException, InterruptedException {
        int executions = 30;

        String[] problems = new String[]{
            "OO_MyBatis",
            "OA_AJHsqldb",
            "OA_AJHotDraw",
            "OO_BCEL",
            "OO_JHotDraw",
            "OA_HealthWatcher",
            "OA_TollSystems",
            "OO_JBoss"
        };

        String[] algorithms = new String[]{
            "ALG_6", "ALG_2", "ALG_7",
            "IRACE_7", "IRACE_3", "IRACE_9",
            "NSGAII", "SPEA2"
        };

        DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

        try (FileWriter hypervolumeTableWriter = new FileWriter("experiment/CITO/testing/HYPERVOLUME_ES.txt")) {

            StringBuilder hypervolumeLatexTableBuilder = new StringBuilder();

            hypervolumeLatexTableBuilder
                    .append("\\begin{table*}[!htb]\n")
                    .append("\t\\centering\n")
                    .append("\t\\fontsize{7pt}{8pt}\\selectfont\n")
                    .append("\t\\caption{Effect Size results}\n")
                    .append("\t\\label{tab:Effect Size}\n")
                    .append("\t\\begin{tabulary}{\\linewidth}{c");

            for (int i = 0; i < algorithms.length; i++) {
                for (int j = i + 1; j < algorithms.length + 1; j++) {
                    hypervolumeLatexTableBuilder.append("l");
                }
            }

            hypervolumeLatexTableBuilder.append("}\n");
            hypervolumeLatexTableBuilder.append("\t\t\\toprule\n");
            hypervolumeLatexTableBuilder.append("\t\t\\textbf{System}");

            for (int i = 0; i < algorithms.length - 1; i++) {
                for (int j = i + 1; j < algorithms.length; j++) {
                    hypervolumeLatexTableBuilder.append(" & \\textbf{").append(algorithms[i].replaceAll("\\_", "\\\\_")).append("/").append(algorithms[j].replaceAll("\\_", "\\\\_")).append("}");
                }
            }

            hypervolumeLatexTableBuilder.append("\\\\\n");
            hypervolumeLatexTableBuilder.append("\t\t\\midrule\n");

            for (String problem : problems) {
                hypervolumeLatexTableBuilder.append("\t\t").append(problem.replaceAll("O[OA]\\_", ""));

                HypervolumeCalculator calculator = new HypervolumeCalculator();

                for (String algorithm : algorithms) {
                    String path = "experiment/CITO/testing/" + problem + "/";
                    String hyperheuristicDirectory = path + algorithm + "/";
                    for (int j = 0; j < executions; j++) {
                        try {
                            calculator.addParetoFront(hyperheuristicDirectory + "EXECUTION_" + j + "/FUN_" + j + ".txt");
                        } catch (FileNotFoundException ex) {
                            calculator.addParetoFront(hyperheuristicDirectory + "EXECUTION_" + j + "/FUN.txt");
                        }
                    }
                }

                HashMap<String, double[]> hypervolumeHashMap = new HashMap<>();

                for (String algorithm : algorithms) {
                    String path = "experiment/CITO/testing/" + problem + "/";
                    String hyperheuristicDirectory = path + algorithm + "/";

                    double[] hypervolumes = new double[executions];

                    for (int j = 0; j < executions; j++) {
                        try {
                            hypervolumes[j] = calculator.calculateHypervolume(hyperheuristicDirectory + "EXECUTION_" + j + "/FUN_" + j + ".txt");
                        } catch (FileNotFoundException ex) {
                            hypervolumes[j] = calculator.calculateHypervolume(hyperheuristicDirectory + "EXECUTION_" + j + "/FUN.txt");
                        }
                    }

                    hypervolumeHashMap.put(algorithm.replaceAll("-", ""), hypervolumes);
                }

                HashMap<String, HashMap<String, Double>> hypervolumeEffectSize = VarghaDelaneyEffectSize.computeEffectSize(hypervolumeHashMap);

                for (int i = 0; i < algorithms.length - 1; i++) {
                    String groupA = algorithms[i];
                    for (int j = i + 1; j < algorithms.length; j++) {
                        String groupB = algorithms[j];

                        double hypervolumeValue = hypervolumeEffectSize.get(groupA.replaceAll("-", "")).get(groupB.replaceAll("-", ""));

                        hypervolumeLatexTableBuilder.append(" & ").append(decimalFormatter.format(hypervolumeValue)).append(" (").append(VarghaDelaneyEffectSize.interpretEffectSize(hypervolumeValue)).append(")");
                    }
                }
                hypervolumeLatexTableBuilder.append("\\\\\n");
            }

            hypervolumeLatexTableBuilder
                    .append("\t\t\\bottomrule\n")
                    .append("\t\\end{tabulary}\n")
                    .append("\\end{table*}\n");

            hypervolumeTableWriter.write(hypervolumeLatexTableBuilder.toString().replaceAll("ChoiceFunction", "HITO-CF").replaceAll("MultiArmedBandit", "HITO-MAB").replaceAll("Random", "HITO-R"));
        }
    }

}
