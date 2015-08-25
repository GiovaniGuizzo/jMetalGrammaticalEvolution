package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.operator.impl.mutation.DuplicationMutation;
import org.uma.jmetal.operator.impl.mutation.PruneMutation;
import org.uma.jmetal.solution.impl.VariableIntegerSolution;

/**
 * Created by ajnebro on 26/10/14.
 */
public abstract class AbstractGrammaticalEvolutionAlgorithm<S extends VariableIntegerSolution, R> extends AbstractGeneticAlgorithm<VariableIntegerSolution, R> {

    protected PruneMutation pruneMutationOperator;
    protected DuplicationMutation duplicationMutationOperator;

}
