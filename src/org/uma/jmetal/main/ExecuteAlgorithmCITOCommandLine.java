package org.uma.jmetal.main;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.builder.GeneratedDynamicGeneticAlgorithmBuilder;
import org.uma.jmetal.algorithm.components.factory.ReproductionImplementationFactory;
import org.uma.jmetal.algorithm.components.factory.SelectionOperatorFactory;
import org.uma.jmetal.experiment.impl.AlgorithmRunner;
import org.uma.jmetal.problem.impl.CITOProblem;

@Parameters(separators = " =")
public class ExecuteAlgorithmCITOCommandLine {

    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help;

    @Parameter(names = "--problemPath", description = "Path for the CITO problem data set (String)", required = true)
    private String problemPath;

    @Parameter(names = "--outputPath", description = "Output path (directory) for printing the results. The files are named FUN_<id>, VAR_<id> and TIME_<id>. (String)", required = true)
    private String outputPath;

    @Parameter(names = "--id", description = "ID of this execution. Used for differentiating the output files. (int)", required = true)
    private int id;

    @ParametersDelegate
    private final GeneratedDynamicGeneticAlgorithmBuilder builder = new GeneratedDynamicGeneticAlgorithmBuilder();

    public static void main(String[] args) {
        ExecuteAlgorithmCITOCommandLine cmd = new ExecuteAlgorithmCITOCommandLine();
        JCommander jCommander = new JCommander(cmd);
        jCommander.setAllowParameterOverwriting(true);

        jCommander.parse(args);
        jCommander.setDefaultProvider(new DefaultProvider(cmd));
        jCommander.parse(args);

        jCommander.setProgramName(cmd.getClass().getSimpleName());
        if (cmd.help) {
            jCommander.usage();
            return;
        }

        CITOProblem problem = new CITOProblem(cmd.problemPath);
        cmd.builder.setProblem(problem);

        if (cmd.builder.getSelectionOperator().equals(SelectionOperatorFactory.K_TOURNAMENT)
                && cmd.builder.getTournamentSize() == 0) {
            throw new ParameterException("The following option is required: --tournamentSize");
        }

        if ((cmd.builder.getCrossoverOperator() != null && !cmd.builder.getCrossoverOperator().trim().isEmpty())
                && cmd.builder.getCrossoverProbability() == 0.0) {
            System.out.println("Valor do parametro: -" + cmd.builder.getCrossoverOperator().trim() + "-");
            throw new ParameterException("The following option is required: --crossoverProbability");
        }

        if ((cmd.builder.getMutationOperator() != null && !cmd.builder.getMutationOperator().trim().isEmpty())
                && cmd.builder.getMutationProbability() == 0.0) {
            throw new ParameterException("The following option is required: --mutationProbability");
        }

        new File(cmd.outputPath).mkdirs();

        Algorithm algorithm = cmd.builder.buildAlgorithm();
        AlgorithmRunner runner = new AlgorithmRunner<>(algorithm, cmd.outputPath, String.valueOf(cmd.id));
        runner.run();
    }

    public static class DefaultProvider implements IDefaultProvider {

        private final ExecuteAlgorithmCITOCommandLine cmd;

        public DefaultProvider(ExecuteAlgorithmCITOCommandLine cmd) {
            this.cmd = cmd;
        }

        @Override
        public String getDefaultValueFor(String string) {
            switch (string) {
                case "--solutionsToSelectAtEachGeneration":
                    int solutionsToSelect = 0;
                    if (cmd.builder.getReproduction() != null) {
                        switch (cmd.builder.getReproduction()) {
                            case ReproductionImplementationFactory.STEADY_STATE:
                                solutionsToSelect = 2;
                                break;
                            case ReproductionImplementationFactory.GENERATIONAL_TWO_CHILDREN:
                                solutionsToSelect = cmd.builder.getPopulationSize();
                                break;
                            case ReproductionImplementationFactory.GENERATIONAL_ONE_CHILD:
                                solutionsToSelect = cmd.builder.getPopulationSize() * 2;
                                break;
                        }
                    }
                    return String.valueOf(solutionsToSelect);
                case "--bisections":
                    return "5";
                case "--numberOfObjectives":
                    return "2";
            }
            return null;
        }

    }

}
