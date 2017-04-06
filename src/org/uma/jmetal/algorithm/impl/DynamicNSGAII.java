package org.uma.jmetal.algorithm.impl;

import java.util.List;
import org.uma.jmetal.algorithm.components.impl.diversity.CrowdingDistance;
import org.uma.jmetal.algorithm.components.impl.initialization.RandomInitialization;
import org.uma.jmetal.algorithm.components.impl.progress.EvaluationsCountProgress;
import org.uma.jmetal.algorithm.components.impl.ranking.DominanceDepth;
import org.uma.jmetal.algorithm.components.impl.replacement.RankingAndDiversityReplacement;
import org.uma.jmetal.algorithm.components.impl.reproduction.GenerationalTwoChildrenReproduction;
import org.uma.jmetal.algorithm.components.impl.selection.OnlyPopulationSelection;
import org.uma.jmetal.algorithm.components.impl.stoppingcondition.MaxEvaluationsCondition;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

public class DynamicNSGAII<S extends Solution<?>> extends DefaultDynamicGeneticAlgorithm<S> {

    public DynamicNSGAII(Problem<S> problem, int maxEvaluations, int populationSize,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
            SelectionOperator<List<S>, List<S>> selectionOperator, SolutionListEvaluator<S> evaluator) {
        super(problem,
                populationSize,
                new EvaluationsCountProgress(),
                new MaxEvaluationsCondition(maxEvaluations),
                new RandomInitialization<>(),
                new OnlyPopulationSelection<>(),
                new GenerationalTwoChildrenReproduction<>(),
                new RankingAndDiversityReplacement<>(new DominanceDepth<S>(), new CrowdingDistance<S>()),
                null,
                selectionOperator,
                crossoverOperator,
                mutationOperator,
                evaluator);
    }

}
