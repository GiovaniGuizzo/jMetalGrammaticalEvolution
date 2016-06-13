package org.uma.jmetal.grammaticalevolution.mapper.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.uma.jmetal.algorithm.AbstractDynamicGeneticAlgorithm;
import org.uma.jmetal.algorithm.builder.GeneratedDynamicGeneticAlgorithmBuilder;
import org.uma.jmetal.algorithm.components.factory.ReproductionImplementationFactory;
import org.uma.jmetal.grammaticalevolution.mapper.AbstractGrammarMapper;
import org.uma.jmetal.grammaticalevolution.representation.Expression;
import org.uma.jmetal.grammaticalevolution.representation.Node;
import org.uma.jmetal.solution.Solution;

public class GeneticAlgorithmExpressionMapper<S extends Solution<?>> extends AbstractGrammarMapper<AbstractDynamicGeneticAlgorithm<S>> {

    protected int currentIndex;
    protected int populationSize;
    protected int numberOfObjectives;
    protected int bisections;

    public GeneticAlgorithmExpressionMapper(int numberOfObjectives, int bisections) {
        this(numberOfObjectives, null, bisections);
    }

    public GeneticAlgorithmExpressionMapper(int numberOfObjectives, Node rootNode, int bisections) {
        super(rootNode);
        this.currentIndex = 0;
        this.numberOfObjectives = numberOfObjectives;
        this.bisections = bisections;
    }

    private Expression selectExpression(Node node, List<Integer> grammarInstance) {
        Expression expression = node.getExpressions().get(grammarInstance.get(currentIndex % grammarInstance.size()) % node.getExpressions().size());
        if (node.getExpressions().size() > 1) {
            currentIndex++;
        }
        return expression;
    }

    @Override
    protected synchronized AbstractDynamicGeneticAlgorithm<S> hookInterpret(List<Integer> grammarInstance) {
        currentIndex = 0;
        GeneratedDynamicGeneticAlgorithmBuilder<S> builder = new GeneratedDynamicGeneticAlgorithmBuilder<>();

        Node gaNode = nonTerminalNodes.get("GA"); //ou rootNode
        try {
            builder.setBisections(bisections);
            builder.setNumberOfObjectives(numberOfObjectives);
            interpretPopulationSize(gaNode.getExpressions().get(0).getNodes().get(0), grammarInstance, builder);
            interpretInitialization(gaNode.getExpressions().get(0).getNodes().get(1), grammarInstance, builder);
            interpretSelection(gaNode.getExpressions().get(0).getNodes().get(2), grammarInstance, builder);
            interpretMating(gaNode.getExpressions().get(0).getNodes().get(3), grammarInstance, builder);
            interpretReplacement(gaNode.getExpressions().get(0).getNodes().get(4), grammarInstance, builder);
            interpretArchive(gaNode.getExpressions().get(0).getNodes().get(5), grammarInstance, builder);
        } catch (ScriptException ex) {
            Logger.getLogger(GeneticAlgorithmExpressionMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return builder.buildAlgorithm();
    }

    //population size
    private void interpretPopulationSize(Node populationSizeNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(populationSizeNode, grammarInstance);
        setPopulationSize(Integer.parseInt(selectedExpression.getNodes().get(0).getName()));
        builder.setPopulationSize(getPopulationSize());
    }

    //initialization
    private void interpretInitialization(Node initializationNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(initializationNode, grammarInstance);
        builder.setInitialization(selectedExpression.getNodes().get(0).getName());
    }

    //selection
    private void interpretSelection(Node selectionNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        interpretSelectionOperator(selectionNode.getExpressions().get(0).getNodes().get(0), grammarInstance, builder);
        interpretSource(selectionNode.getExpressions().get(0).getNodes().get(1), grammarInstance, builder);
        interpretSelectionFitnessAssignment(selectionNode.getExpressions().get(0).getNodes().get(2), grammarInstance, builder);
    }

    private void interpretSelectionOperator(Node selectionOperatorNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(selectionOperatorNode, grammarInstance);

        builder.setSelectionOperator(selectedExpression.getNodes().get(0).getName());
        if (selectedExpression.getNodes().size() == 2) {
            Node tournamentSizeNode = selectedExpression.getNodes().get(1);
            Node size = selectExpression(tournamentSizeNode, grammarInstance).getNodes().get(0);
            builder.setTournamentSize(Integer.parseInt(size.getName()));
        }
    }

    private void interpretSource(Node sourceNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(sourceNode, grammarInstance);
        builder.setSelectionSource(selectedExpression.getNodes().get(0).getName());
    }

    private void interpretSelectionFitnessAssignment(Node fitnessAssignmentNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        builder.setSelectionRanking(interpretRankingStrategy(fitnessAssignmentNode.getExpressions().get(0).getNodes().get(0), grammarInstance));
        builder.setSelectionDiversity(interpretDiversityStrategy(fitnessAssignmentNode.getExpressions().get(0).getNodes().get(1), grammarInstance));
    }

    //mating
    private void interpretMating(Node matingNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        interpretMatingOperators(matingNode.getExpressions().get(0).getNodes().get(0), grammarInstance, builder);
        interpretMatingStrategy(matingNode.getExpressions().get(0).getNodes().get(1), grammarInstance, builder);
    }

    private void interpretMatingOperators(Node matingOperatorsNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        interpretCrossoverOperator(matingOperatorsNode.getExpressions().get(0).getNodes().get(0), grammarInstance, builder);
        interpretCrossoverProbability(matingOperatorsNode.getExpressions().get(0).getNodes().get(1), grammarInstance, builder);
        interpretMutationOperator(matingOperatorsNode.getExpressions().get(0).getNodes().get(2), grammarInstance, builder);
        interpretMutationProbability(matingOperatorsNode.getExpressions().get(0).getNodes().get(3), grammarInstance, builder);
    }

    //crossover
    private void interpretCrossoverOperator(Node crossoverOperatorNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(crossoverOperatorNode, grammarInstance);
        builder.setCrossoverOperator(selectedExpression.getNodes().get(0).getName());
    }

    private void interpretCrossoverProbability(Node crossoverProbabilityNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(crossoverProbabilityNode, grammarInstance);
        builder.setCrossoverProbability(Double.parseDouble(selectedExpression.getNodes().get(0).getName()));
    }

    //mutation
    private void interpretMutationOperator(Node mutationOperatorNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(mutationOperatorNode, grammarInstance);
        builder.setMutationOperator(selectedExpression.getNodes().get(0).getName());
    }

    private void interpretMutationProbability(Node mutationProbabilityNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(mutationProbabilityNode, grammarInstance);
        builder.setMutationProbability(Double.parseDouble(selectedExpression.getNodes().get(0).getName()));
    }

    private void interpretMatingStrategy(Node matingStrategyNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) {
        Expression selectedExpression = selectExpression(matingStrategyNode, grammarInstance);
        String matingStrategy = selectedExpression.getNodes().get(0).getName();
        builder.setReproduction(matingStrategy);

        int solutionsToSelect = 0;
        switch (matingStrategy) {
            case ReproductionImplementationFactory.STEADY_STATE:
                solutionsToSelect = 2;
                break;
            case ReproductionImplementationFactory.GENERATIONAL_TWO_CHILDREN:
                solutionsToSelect = getPopulationSize();
                break;
            case ReproductionImplementationFactory.GENERATIONAL_ONE_CHILD:
                solutionsToSelect = getPopulationSize() * 2;
                break;
        }
        builder.setSolutionsToSelectAtEachGeneration(solutionsToSelect);
    }

    //replacement
    private void interpretReplacement(Node replacementNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) throws ScriptException {
        Expression selectedExpression = selectExpression(replacementNode, grammarInstance);
        String replacement = selectedExpression.getNodes().get(0).getName();
        builder.setReplacement(replacement);
        if (replacement.equals("Generational")) {
            interpretElitism(selectedExpression.getNodes().get(1), grammarInstance, builder);
            interpretReplacementFitnessAssignment(selectedExpression.getNodes().get(2), grammarInstance, builder);
        } else {
            interpretReplacementFitnessAssignment(selectedExpression.getNodes().get(1), grammarInstance, builder);
        }
    }

    private void interpretElitism(Node elitismNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) throws ScriptException {
        Expression selectedExpression = selectExpression(elitismNode, grammarInstance);
        String elitismSize = selectedExpression.getNodes().get(0).getName();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        String expression = elitismSize.replaceAll("N", String.valueOf(getPopulationSize()) + ".0");
        double size = Double.valueOf(engine.eval(expression).toString());
        builder.setElitismSize((int) size);
    }

    private void interpretReplacementFitnessAssignment(Node fitnessAssignmentNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) throws ScriptException {
        builder.setReplacementRanking(interpretRankingStrategy(fitnessAssignmentNode.getExpressions().get(0).getNodes().get(0), grammarInstance));
        builder.setReplacementDiversity(interpretDiversityStrategy(fitnessAssignmentNode.getExpressions().get(0).getNodes().get(1), grammarInstance));
    }

    private void interpretArchive(Node archivingNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) throws ScriptException {
        interpretArchiveFitnessAssignment(archivingNode.getExpressions().get(0).getNodes().get(0), grammarInstance, builder);
        interpretArchiveSize(archivingNode.getExpressions().get(0).getNodes().get(1), grammarInstance, builder);
    }

    private void interpretArchiveFitnessAssignment(Node fitnessAssignmentNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) throws ScriptException {
        builder.setArchiveRanking(interpretRankingStrategy(fitnessAssignmentNode.getExpressions().get(0).getNodes().get(0), grammarInstance));
        builder.setArchiveDiversity(interpretDiversityStrategy(fitnessAssignmentNode.getExpressions().get(0).getNodes().get(1), grammarInstance));
    }

    private void interpretArchiveSize(Node archiveSizeNode, List<Integer> grammarInstance, GeneratedDynamicGeneticAlgorithmBuilder<S> builder) throws ScriptException {
        Expression selectedExpression = selectExpression(archiveSizeNode, grammarInstance);

        String name = selectedExpression.getNodes().get(0).getName();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        String expression = name.replaceAll("N", String.valueOf(getPopulationSize()) + ".0");
        double archiveSize = Double.valueOf(engine.eval(expression).toString());
        builder.setArchiveSize((int) archiveSize);
    }

    //fitness assignment
    private String interpretRankingStrategy(Node rankingStrategyNode, List<Integer> grammarInstance) {
        Expression selectedExpression = selectExpression(rankingStrategyNode, grammarInstance);
        return selectedExpression.getNodes().get(0).getName();
    }

    private String interpretDiversityStrategy(Node diversityStrategyNode, List<Integer> grammarInstance) {
        Expression selectedExpression = selectExpression(diversityStrategyNode, grammarInstance);
        return selectedExpression.getNodes().get(0).getName();
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }

    public void setNumberOfObjectives(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }

    public int getBisections() {
        return bisections;
    }

    public void setBisections(int bisections) {
        this.bisections = bisections;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GeneticAlgorithmExpressionMapper mapper = new GeneticAlgorithmExpressionMapper(2, 5);
        mapper.loadGrammar("grammar.bnf");
        List<Integer> grammarInstance = new ArrayList<>();
        //grammarInstance.add(202);
        grammarInstance.add(301);
//        grammarInstance.add(405);
//        grammarInstance.add(500);
//        grammarInstance.add(602);
        AbstractDynamicGeneticAlgorithm interpret = mapper.hookInterpret(grammarInstance);
    }

}
