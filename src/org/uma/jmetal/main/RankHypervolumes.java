package org.uma.jmetal.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.uma.jmetal.measure.qualityindicator.HypervolumeCalculator;
import org.uma.jmetal.measure.statistic.KruskalWallisTest;
import org.uma.jmetal.util.HashMapToLatexConverter;

public class RankHypervolumes {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        int executions = 30;

        String[] algorithms = new String[]{
            //            "NSGA-II", "SPEA2", "HITO-NSGA-II-CF", "ALG_6"
            "ALG_0", "ALG_1", "ALG_2", "ALG_3", "ALG_4", "ALG_5", "ALG_6", "ALG_7", "ALG_8", "ALG_9",
//            "IRACE_0", "IRACE_1", "IRACE_2", "IRACE_3", "IRACE_4", "IRACE_5", "IRACE_6", "IRACE_7", "IRACE_8", "IRACE_9",
//            "NSGAII", "SPEA2"
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
        
        HashMap<String, HashMap<String, Double>> allRanks = new HashMap<>();

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
                    .collect(Collectors.toMap(algorithm -> algorithm, (algorithm) -> hypervolumeHashMap.get(algorithm)))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> Arrays.stream(entry.getValue()).mapToDouble(Double::doubleValue).average().getAsDouble() * -1));

            HashMap<String, HashMap<String, Boolean>> kruskalResult = KruskalWallisTest.test(hypervolumeHashMap);

            double[] algorithmRanks = ranking.rank(Arrays.stream(algorithms)
                    .mapToDouble((algorithm) -> hypervolumeAverages.get(algorithm))
                    .toArray());

            HashMap<String, Double> algorithmRanksMap = IntStream.range(0, algorithms.length)
                    .collect(() -> new HashMap<String, Double>(),
                            (HashMap<String, Double> i, int j) -> i.put(algorithms[j], algorithmRanks[j]),
                            (HashMap<String, Double> i, HashMap<String, Double> j) -> System.out.println("Pq entrou aqui?"));

            HashMap<String, Double> algorithmSecondRanksMap = new HashMap<>();
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
                
                algorithmSecondRanksMap.put(algorithm, rankSum);
            }
            allRanks.put(problem, algorithmSecondRanksMap);
        }
        System.out.println(HashMapToLatexConverter.convert("Ranks", "Ranks", allRanks));
    }

}
