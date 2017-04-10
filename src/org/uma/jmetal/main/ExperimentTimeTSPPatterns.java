package org.uma.jmetal.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.uma.jmetal.measure.statistic.KruskalWallisTest;
import org.uma.jmetal.measure.statistic.VarghaDelaneyEffectSize;

public class ExperimentTimeTSPPatterns {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        args = new String[]{"experiments/", "NSGA-II,NSGA-II_jMetal"};
        String[] algorithms = args[1].split(",");
        try (FileWriter tableWriter = new FileWriter(args[0] + "/TABLE_TIME.txt")) {
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
            tableWriter.append(Arrays.asList(algorithms)
                    .stream()
                    .collect(Collectors.joining(" & ")));
            tableWriter.write(" & Kruskal-Wallis & Vargha-Delaney \\\\\n\\midrule\n");

            for (String problem : problems) {
                tableWriter.append(problem + " & ");
                File inputDir = new File(args[0] + "/" + problem);

                List<File> algorithmVars = new ArrayList<>();
                for (String algorithm : algorithms) {
                    File file = new File(inputDir.getPath() + "/" + algorithm);
                    algorithmVars.add(file);
                }

                HashMap<String, Double[]> values = new HashMap<>();
                HashMap<String, Double> means = new HashMap<>();
                for (File algorithmDir : algorithmVars) {
                    List<Double> times = new ArrayList<>();
                    for (File executionDir : algorithmDir.listFiles(File::isDirectory)) {
                        File timeFile = executionDir.listFiles((dir, fileName) -> fileName.startsWith("TIME"))[0];
                        Stream<String> lines = Files.lines(timeFile.toPath());
                        double nanoSeconds = Double.parseDouble(lines.findFirst().get());
                        times.add((double) nanoSeconds / 1000000000.0);
                    }
                    values.put(algorithmDir.getName(), times.toArray(new Double[times.size()]));

                    Double mean = times.stream().reduce(Double::sum).get() / times.size();
                    means.put(algorithmDir.getName(), mean);
                }

                HashMap<String, HashMap<String, Boolean>> result = KruskalWallisTest.test(values);
                HashMap<String, HashMap<String, Double>> effectSizes = VarghaDelaneyEffectSize.computeEffectSize(values);

                DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.UK));

                String tableLine = algorithmVars.stream()
                        .map(algorithmDir -> {
                            String value = "";
                            value += df.format(means.get(algorithmDir.getName()));
                            return value;
                        })
                        .collect(Collectors.joining(" & "));
                tableLine += " & " + (result.get(algorithms[0]).get(algorithms[1]) ? "$\\neq$" : "$=$") + " & " + VarghaDelaneyEffectSize.interpretEffectSize(effectSizes.get(algorithms[0]).get(algorithms[1]));
                tableWriter.write(tableLine + " \\\\\n");
            }
        }
    }
}
