package org.uma.jmetal.irace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        for (File file : dir.listFiles((dirrectory, name) -> name.startsWith("FUN_") || name.equals("MAX_MIN.txt"))) {
            calculator.addParetoFront(file.getPath());
        }

        try (FileWriter maxMinWriter = new FileWriter("MAX_MIN.txt")) {
            newArgs.add("--max");
            Arrays.stream(calculator.getMaximumValues())
                    .forEach(doubleValue -> {
                        newArgs.add(String.valueOf(doubleValue));
                        try {
                            maxMinWriter.write(String.valueOf(doubleValue) + " ");
                        } catch (IOException ex) {
                            Logger.getLogger(HookEvaluate.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
            maxMinWriter.write("\n");

            newArgs.add("--min");
            Arrays.stream(calculator.getMinimumValues())
                    .forEach(doubleValue -> {
                        newArgs.add(String.valueOf(doubleValue));
                        try {
                            maxMinWriter.write(String.valueOf(doubleValue) + " ");
                        } catch (IOException ex) {
                            Logger.getLogger(HookEvaluate.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
            maxMinWriter.write("\n");
        }

        return newArgs.toArray(new String[newArgs.size()]);
    }

}
