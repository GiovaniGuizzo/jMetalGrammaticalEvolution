package org.uma.jmetal.irace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.uma.jmetal.main.ComputeHypervolumeCommandLine;
import org.uma.jmetal.measure.qualityindicator.HypervolumeCalculator;

public class HookEvaluate {

    public static void main(String[] args) throws IOException {
        args = getNewArgs(args);

        ComputeHypervolumeCommandLine.main(args);
    }

    private static String[] getNewArgs(String[] args) throws FileNotFoundException, IOException {
        List<String> newArgs = new ArrayList<>();

        newArgs.add("-f");
        newArgs.add("FUN_" + args[0] + ".txt");

        HypervolumeCalculator calculator = new HypervolumeCalculator();
        File dir = new File("./");
        for (File file : dir.listFiles((dirrectory, name) -> name.startsWith("FUN_"))) {
            calculator.addParetoFront(file.getPath());
        }

        newArgs.add("--max");
        Arrays.stream(calculator.getMaximumValues())
                .forEach(doubleValue
                        -> newArgs.add(String.valueOf(doubleValue)));

        newArgs.add("--min");
        Arrays.stream(calculator.getMinimumValues())
                .forEach(doubleValue
                        -> newArgs.add(String.valueOf(doubleValue)));

        return newArgs.toArray(new String[newArgs.size()]);
    }

}
