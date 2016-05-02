package org.uma.jmetal.irace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.uma.jmetal.main.ExecuteAlgorithmCITOCommandLine;

public class HookRun {

    public static void main(String[] args) throws IOException {
        args = getNewArgs(args);
        printParams(args);

        ExecuteAlgorithmCITOCommandLine.main(args);
    }

    private static String[] getNewArgs(String[] args) {
        String[] newArgs = new String[args.length + 6];
        newArgs[0] = "--problemPath";
        newArgs[1] = args[0];
        newArgs[2] = "--id";
        newArgs[3] = args[1];
        newArgs[4] = "--outputPath";
        newArgs[5] = "./";
        newArgs[6] = "--maxEvaluations";
        newArgs[7] = "2000";
        System.arraycopy(args, 2, newArgs, 8, args.length - 2);

        int populationSize = 0;

        for (int i = 0; i < newArgs.length; i++) {
            String newArg = newArgs[i];
            if ("--populationSize".equals(newArg)) {
                populationSize = Integer.parseInt(newArgs[i + 1]);
                break;
            }
        }

        for (int i = 0; i < newArgs.length; i++) {
            String newArg = newArgs[i];
            if ("--elitismSize".equals(newArg)) {
                newArgs[i + 1] = String.valueOf((int) (Double.parseDouble(newArgs[i + 1]) * populationSize));
            }
            if ("--archiveSize".equals(newArg)) {
                newArgs[i + 1] = String.valueOf((int) (Double.parseDouble(newArgs[i + 1]) * populationSize));
            }
        }

        return newArgs;
    }

    private static void printParams(String[] args) throws IOException {
        File outputDir = new File(args[5]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(outputDir.getPath() + "/" + args[3] + "_PARAMS.txt");
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        try (FileWriter writer = new FileWriter(outputFile)) {
            String params;

            params = Arrays.stream(args).collect(Collectors.joining(" "));

            writer.write(params);
            writer.write("\n");
        }
    }

}
