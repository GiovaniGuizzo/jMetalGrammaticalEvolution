package org.uma.jmetal.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.uma.jmetal.measure.qualityindicator.HypervolumeCalculator;
import org.uma.jmetal.measure.statistic.VarghaDelaneyEffectSize;

public class GenerateEffectSizeBoxPlot {

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
            "IRACE_7", "IRACE_3", "IRACE_9",
            "ALG_6", "ALG_2", "ALG_7",
            "NSGAII", "SPEA2"
        };

        File gnuScript = File.createTempFile("GNUPLOT", ".txt");
        gnuScript.deleteOnExit();
        File dataSet = File.createTempFile("GNUDATA", ".txt");
        dataSet.deleteOnExit();

        try (FileWriter gnuScriptWriter = new FileWriter(gnuScript);
                FileWriter dataSetWriter = new FileWriter(dataSet)) {

            dataSetWriter.write("# Effect sizes for algorithm " + algorithms[0] + ":\t" + Arrays.stream(algorithms).skip(1).collect(Collectors.joining("\t")));
            dataSetWriter.write("\n");

            gnuScriptWriter.write("set term png size 1024,768\n"
                    + "set output \"experiment/CITO/testing/" + algorithms[0] + ".png\"\n"
                    + "algorithms = \"" + Arrays.stream(algorithms).skip(1).collect(Collectors.joining(" ")) + "\"\n"
                    + "unset key\n"
                    + "set grid ytics\n"
                    + "\n"
                    + "set style fill solid 1 border -1\n"
                    + "set border 3\n"
                    + "set style data boxplot\n"
                    + "set style line 7 linecolor \"white\" lw 2\n"
                    + "\n"
                    + "set ylabel \"Effect Size Value\"\n"
                    + "set ytics 0.1 nomirror\n"
                    + "\n"
                    + "set xlabel \"Algorithms Compared to " + algorithms[0] + "\"\n"
                    + "set xtics 1 nomirror\n"
                    + "set format x \"\"\n"
                    + "\n"
                    + "set for[i=1:words(algorithms)] xtics add (word(algorithms,i) i)\n"
                    + "set datafile separator '\\t'\n"
                    + "plot "
                    + "0.5 lc 'black' title '', "
//                    + "0.56 lc 'blue' title '', "
//                    + "0.44 lc 'blue' title '', "
//                    + "0.64 lc 'green' title '', "
//                    + "0.36 lc 'green' title '', "
//                    + "0.71 lc 'red' title '', "
//                    + "0.29 lc 'red' title '', "
                    + "for[i=1:words(algorithms)] '" + dataSet.getAbsolutePath() + "' using (i):i linestyle 7\n");

            for (String problem : problems) {

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

                String groupA = algorithms[0];
                for (int j = 1; j < algorithms.length; j++) {
                    String groupB = algorithms[j];
                    double hypervolumeValue = hypervolumeEffectSize.get(groupA.replaceAll("-", "")).get(groupB.replaceAll("-", ""));
                    dataSetWriter.write(String.valueOf(hypervolumeValue) + "\t");
                }
                dataSetWriter.write("\n");
            }
        }

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(System.getProperty("os.name").contains("win") ? "gnuplot.exe" : "gnuplot", gnuScript.getAbsolutePath());
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.start().waitFor();

        gnuScript.delete();
        dataSet.delete();
    }
}
