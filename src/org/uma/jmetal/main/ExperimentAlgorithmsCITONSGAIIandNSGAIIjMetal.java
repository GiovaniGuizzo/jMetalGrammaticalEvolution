package org.uma.jmetal.main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.uma.jmetal.algorithm.AbstractDynamicGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.experiment.impl.AlgorithmRunner;
import org.uma.jmetal.grammaticalevolution.mapper.impl.GeneticAlgorithmExpressionMapper;
import org.uma.jmetal.operator.impl.crossover.PMXCrossover;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.problem.impl.CITOProblem;
import org.uma.jmetal.solution.PermutationSolution;

public class ExperimentAlgorithmsCITONSGAIIandNSGAIIjMetal {

    public static void main(String[] args) throws IOException, InterruptedException {
        int executions = 30;
        String inputDir = args[0];
        String outputDir = args[1];
        new File(outputDir).mkdirs();
        ExecutorService threadPool = Executors.newFixedThreadPool(Integer.parseInt(args[2]));
        String[] problems = {"OO_MyBatis",
            "OA_AJHsqldb",
            "OA_AJHotDraw",
            "OO_BCEL",
            "OO_JHotDraw",
            "OA_HealthWatcher",
            "OA_TollSystems",
            "OO_JBoss"};
        for (String problem : problems) {
            CITOProblem cito = new CITOProblem("problems/" + problem + ".txt");

            {
                GeneticAlgorithmExpressionMapper<?> mapper = new GeneticAlgorithmExpressionMapper(2, 5);
                mapper.loadGrammar("grammar_nsgaii.bnf");

                List<Integer> grammarInstance = new ArrayList<>();
                Scanner scanner = new Scanner(new FileReader(inputDir + "VAR_NSGA-II.txt"));
                while (scanner.hasNextInt()) {
                    grammarInstance.add(scanner.nextInt());
                }

                for (int i = 0; i < executions; i++) {
                    AbstractDynamicGeneticAlgorithm algorithm = mapper.interpret(grammarInstance);
                    algorithm.setProblem(cito);
                    algorithm.getStoppingConditionImplementation().setStoppingCondition(60000);
                    new File(outputDir + "/" + problem + "/NSGA-II/EXECUTION_" + i).mkdirs();
                    AlgorithmRunner<PermutationSolution<Integer>> runner = new AlgorithmRunner<>(algorithm, outputDir + "/" + problem + "/NSGA-II/EXECUTION_" + i, String.valueOf(i));
                    threadPool.submit(runner);
                }
            }

            {
                NSGAIIBuilder builder = new NSGAIIBuilder(cito, new PMXCrossover(1.0), new PermutationSwapMutation(0.01));
                builder.setPopulationSize(50);
                builder.setMaxIterations(60000 / 50);
                for (int i = 0; i < executions; i++) {
                    new File(outputDir + "/" + problem + "/NSGA-II_jMetal/EXECUTION_" + i).mkdirs();
                    AlgorithmRunner<PermutationSolution<Integer>> runner = new AlgorithmRunner<>(builder.build(), outputDir + "/" + problem + "/NSGA-II_jMetal/EXECUTION_" + i, String.valueOf(i));
                    threadPool.submit(runner);
                }
            }
        }
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}
