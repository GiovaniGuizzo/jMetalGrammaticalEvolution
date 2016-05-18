package org.uma.jmetal.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.uma.jmetal.measure.qualityindicator.HypervolumeCalculator;
import org.uma.jmetal.measure.statistic.FriedmanTest;
import org.uma.jmetal.measure.statistic.KruskalWallisTest;
import org.uma.jmetal.util.LatexTableBuilder;

public class RankHypervolumes {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        int executions = 30;

        String[] algorithms = new String[]{
            "ALG_6", "ALG_2", "ALG_7",
            "IRACE_7", "IRACE_3", "IRACE_9",
            "NSGAII", "SPEA2"
        };

        String[] problems = {"OO_MyBatis",
            "OA_AJHsqldb",
            "OA_AJHotDraw",
            "OO_BCEL",
            "OO_JHotDraw",
            "OA_HealthWatcher",
            "OA_TollSystems",
            "OO_JBoss"
        };

        NaturalRanking ranking = new NaturalRanking();

        Map<String, Map<String, String>> allRanks = new HashMap<>();
        Map<String, Map<String, Double>> allHypervolumeAverages = new HashMap<>();

        for (String problem : problems) {

            HypervolumeCalculator calculator = new HypervolumeCalculator();

            String path = "experiment/CITO/testing/" + problem + "/";
            for (String algorithm : algorithms) {
                String algorithmPath = path + algorithm + "/";
                for (int j = 0; j < executions; j++) {
                    try {
                        calculator.addParetoFront(algorithmPath + "EXECUTION_" + j + "/FUN_" + j + ".txt");
                    } catch (FileNotFoundException ex) {
                        calculator.addParetoFront(algorithmPath + "EXECUTION_" + j + "/FUN.txt");
                    }
                }
            }

            HashMap<String, Double[]> hypervolumeHashMap = new HashMap<>();

            for (String algorithm : algorithms) {
                String algorithmPath = path + algorithm + "/";

                Double[] hypervolumes = new Double[executions];

                for (int j = 0; j < executions; j++) {
                    try {
                        hypervolumes[j] = calculator.calculateHypervolume(algorithmPath + "EXECUTION_" + j + "/FUN_" + j + ".txt");
                    } catch (FileNotFoundException ex) {
                        hypervolumes[j] = calculator.calculateHypervolume(algorithmPath + "EXECUTION_" + j + "/FUN.txt");
                    }
                }

                hypervolumeHashMap.put(algorithm, hypervolumes);
            }

            Map<String, Double> hypervolumeAverages = Arrays.stream(algorithms)
                    .collect(Collectors.toMap(Function.identity(), (algorithm) -> hypervolumeHashMap.get(algorithm)))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> Arrays.stream(entry.getValue()).mapToDouble(Double::doubleValue).average().getAsDouble()));

            System.out.println("Problem: " + problem);
            HashMap<String, HashMap<String, Boolean>> kruskalResult = KruskalWallisTest.test(hypervolumeHashMap);

            double[] algorithmRanks = ranking.rank(Arrays.stream(algorithms)
                    .mapToDouble((algorithm) -> hypervolumeAverages.get(algorithm) * -1)
                    .toArray());

            HashMap<String, Double> algorithmRanksMap = IntStream.range(0, algorithms.length)
                    .collect(() -> new HashMap<String, Double>(),
                            (HashMap<String, Double> i, int j) -> i.put(algorithms[j], algorithmRanks[j]),
                            (HashMap<String, Double> i, HashMap<String, Double> j) -> System.out.println("Pq entrou aqui?"));

            HashMap<String, String> algorithmSecondRanksMap = new HashMap<>();
            for (Map.Entry<String, Double> entrySet : algorithmRanksMap.entrySet()) {
                String algorithm = entrySet.getKey();

                Double rankSum = entrySet.getValue() + kruskalResult.get(algorithm)
                        .entrySet()
                        .stream()
                        .filter((entry) -> !entry.getValue())
                        .mapToDouble((entry) -> algorithmRanksMap.get(entry.getKey()))
                        .sum();

                rankSum = rankSum / ((double) kruskalResult.get(algorithm)
                        .entrySet()
                        .stream()
                        .filter((entry) -> !entry.getValue())
                        .mapToDouble((entry) -> algorithmRanksMap.get(entry.getKey()))
                        .count() + 1.0);

                algorithmSecondRanksMap.put(algorithm, String.valueOf(rankSum));
            }
            allRanks.put(problem, algorithmSecondRanksMap);
            allHypervolumeAverages.put(problem, hypervolumeAverages);
        }

        allRanks.put("Average Hypervolume/Rank", Arrays.stream(algorithms)
                .collect(Collectors.toMap(Function.identity(), (algorithm) -> {
                    Double rank = 0.0;
                    for (String problem : problems) {
                        rank += Double.parseDouble(allRanks.get(problem).get(algorithm));
                    }
                    rank /= (double) problems.length;
                    return String.valueOf(rank);
                })));

        allRanks.entrySet()
                .stream()
                .filter(problemEntry -> problemEntry.getKey().equals("Average Hypervolume/Rank"))
                .forEach((problemEntry) -> {
                    problemEntry.getValue().entrySet().forEach(algorithmEntry -> {
                        String key = algorithmEntry.getKey();
                        Double value = Double.parseDouble(algorithmEntry.getValue());
                        Double average = allHypervolumeAverages.entrySet()
                        .stream()
                        .mapToDouble(entry -> entry.getValue().get(key))
                        .average()
                        .getAsDouble();
                        algorithmEntry.setValue(String.format("%.2f", average) + " (" + String.format("%.2f", value) + ")");
                    });
                });

        allRanks.entrySet()
                .stream()
                .filter(problemEntry -> !problemEntry.getKey().equals("Average Hypervolume/Rank"))
                .forEach((problemEntry) -> {
                    problemEntry.getValue().entrySet().forEach(algorithmEntry -> {
                        String key = algorithmEntry.getKey();
                        Double value = Double.parseDouble(algorithmEntry.getValue());
                        Double average = allHypervolumeAverages.get(problemEntry.getKey()).get(key);
                        algorithmEntry.setValue(String.format("%.2f", average) + " (" + String.format("%.2f", value) + ")");
                    });
                });
        
        LatexTableBuilder tableBuilder =  new LatexTableBuilder();
        tableBuilder.setCaption("Hypervolume and Ranks")
                .setLabel("tab:Hypervolume and Ranks")
                .setFirstColumnHeader("System");
        
        System.out.println(tableBuilder.build(allRanks));

        HashMap<String, double[]> hypervolumeAverages = new HashMap<>();
        for (String algorithm : algorithms) {
            hypervolumeAverages.put(algorithm, allHypervolumeAverages.entrySet()
                    .stream()
                    .mapToDouble((t) -> t.getValue().get(algorithm))
                    .toArray()
            );
        }

        HashMap<String, HashMap<String, Boolean>> test = FriedmanTest.test(hypervolumeAverages);

        Map<String, Map<String, String>> collect = test.entrySet()
                .stream()
                .collect(Collectors.toMap(
                                (entry) -> entry.getKey(),
                                entry -> entry.getValue()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                                entry2 -> entry2.getKey(),
                                                entry2 -> entry.getKey().equals(entry2.getKey()) ? "-" : entry2.getValue().toString()))));

         tableBuilder.setCaption("Friedman")
                .setLabel("tab:Friedman")
                .setFirstColumnHeader("Algortihms");
        
        System.out.println(tableBuilder.build(collect));
    }

}
