package org.uma.jmetal.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.uma.jmetal.algorithm.builder.DynamicNSGAIIBuilder;
import org.uma.jmetal.algorithm.impl.DynamicNSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.experiment.impl.AlgorithmRunner;
import org.uma.jmetal.operator.impl.crossover.PMXCrossover;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.problem.multiobjective.MultiobjectiveTSP;
import org.uma.jmetal.solution.PermutationSolution;

public class ExperimentAlgorithmsTSPPatterns {

    public static void main(String[] args) throws IOException, InterruptedException {
        int executions = 30;
        String outputDir = args[0];
        new File(outputDir).mkdirs();
        ExecutorService threadPool = Executors.newFixedThreadPool(Integer.parseInt(args[1]));
        HashMap<String, String[]> problems = new HashMap<>();
        problems.put("kroAB100", new String[]{"kroA100.tsp", "kroB100.tsp"});
        problems.put("kroAC100", new String[]{"kroA100.tsp", "kroC100.tsp"});
        problems.put("kroAD100", new String[]{"kroA100.tsp", "kroD100.tsp"});
        problems.put("kroAE100", new String[]{"kroA100.tsp", "kroE100.tsp"});
        problems.put("kroBC100", new String[]{"kroB100.tsp", "kroC100.tsp"});
        problems.put("kroBD100", new String[]{"kroB100.tsp", "kroD100.tsp"});
        problems.put("kroBE100", new String[]{"kroB100.tsp", "kroE100.tsp"});
        problems.put("kroCD100", new String[]{"kroC100.tsp", "kroD100.tsp"});
        problems.put("kroCE100", new String[]{"kroC100.tsp", "kroE100.tsp"});
        problems.put("kroDE100", new String[]{"kroD100.tsp", "kroE100.tsp"});
        problems.put("kroAB150", new String[]{"kroA150.tsp", "kroB150.tsp"});
        problems.put("kroAB200", new String[]{"kroA200.tsp", "kroB200.tsp"});

        for (Map.Entry<String, String[]> problem : problems.entrySet()) {
            String problemName = problem.getKey();
            String[] problemFiles = problem.getValue();

            MultiobjectiveTSP problemInstace = new MultiobjectiveTSP("problems" + File.separator + problemFiles[0], "problems" + File.separator + problemFiles[1]);

            {
                DynamicNSGAIIBuilder builder = new DynamicNSGAIIBuilder(problemInstace, 50, new PMXCrossover(1.0), new PermutationSwapMutation<>(0.01));
                for (int i = 0; i < executions; i++) {
                    DynamicNSGAII algorithm = builder.build();
                    algorithm.getStoppingConditionImplementation().setStoppingCondition(60000);
                    new File(outputDir + File.separator + problemName + File.separator + "NSGA-II" + File.separator + "EXECUTION_" + i).mkdirs();
                    AlgorithmRunner<PermutationSolution<Integer>> runner = new AlgorithmRunner<>(algorithm, outputDir + File.separator + problemName + File.separator + "NSGA-II" + File.separator + "EXECUTION_" + i, String.valueOf(i));
                    threadPool.submit(runner);
                }
            }

            {
                NSGAIIBuilder builder = new NSGAIIBuilder(problemInstace, new PMXCrossover(1.0), new PermutationSwapMutation(0.01));
                builder.setPopulationSize(50);
                builder.setMaxIterations(60000 / 50);
                for (int i = 0; i < executions; i++) {
                    new File(outputDir + File.separator + problemName + File.separator + "NSGA-II_jMetal" + File.separator + "EXECUTION_" + i).mkdirs();
                    AlgorithmRunner<PermutationSolution<Integer>> runner = new AlgorithmRunner<>(builder.build(), outputDir + File.separator + problemName + File.separator + "NSGA-II_jMetal" + File.separator + "EXECUTION_" + i, String.valueOf(i));
                    threadPool.submit(runner);
                }
            }
        }
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}
