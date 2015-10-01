package org.uma.jmetal.algorithm.components.impl.replacement;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uma.jmetal.algorithm.components.ReplacementImplementation;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

public class OutputReplacementProxy<S extends Solution<?>> implements ReplacementImplementation<S> {

    private final ReplacementImplementation<S> proxy;
    private int count;
    private final String outputFolder;
    private int current;

    public OutputReplacementProxy(ReplacementImplementation<S> proxy, String outputFolder) {
        this.proxy = proxy;
        this.count = 0;
        this.outputFolder = outputFolder;
        this.current = 0;
    }

    @Override
    public List<S> replacement(List<S> population, List<S> offspringPopulation, int populationSize) {
        List<S> replacement = proxy.replacement(population, offspringPopulation, populationSize);

        if (current == 0 && count == 0) {
            try {
                SolutionSetOutput.printObjectivesToFile(population, outputFolder + "/" + (count) + ".txt");
            } catch (IOException ex) {
                Logger.getLogger(OutputReplacementProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        current += populationSize;
        if (current > 1000) {
            current %= 1000;
            try {
                SolutionSetOutput.printObjectivesToFile(replacement, outputFolder + "/" + (++count) + ".txt");
            } catch (IOException ex) {
                Logger.getLogger(OutputReplacementProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return replacement;
    }

}
