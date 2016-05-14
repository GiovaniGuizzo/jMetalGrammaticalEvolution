/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uma.jmetal.measure.statistic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author giovani
 */
public class FriedmanTest {

    public static HashMap<String, HashMap<String, Boolean>> test(HashMap<String, double[]> values) throws IOException, InterruptedException {
        String script = "require(pgirmess)\n";
        script += "ARRAY <- c(";
        int size = 0;
        for (Map.Entry<String, double[]> entrySet : values.entrySet()) {
            double[] keyValues = entrySet.getValue();
            size = keyValues.length;

            for (Double value : keyValues) {
                script += value + ",";
            }
        }
        script = script.substring(0, script.lastIndexOf(",")) + ")";
        script += "\n";

        script += "categs<-as.factor(rep(c(";
        for (Map.Entry<String, double[]> entrySet : values.entrySet()) {
            String key = entrySet.getKey();
            script += "\"" + key.replaceAll("\\\\", "\\\\\\\\") + "\",";
        }
        script = script.substring(0, script.lastIndexOf(","));
        script += "),each=" + size + "));";
        script += "\n";
        script += "blocks <- c(";
        for (Map.Entry<String, double[]> entrySet : values.entrySet()) {
            for (int i = 1; i <= entrySet.getValue().length; i++) {
                script += i + ",";
            }
        }
        script = script.substring(0, script.lastIndexOf(","));
        script += ")\n";
        script += "pos_teste <- friedmanmc(ARRAY,categs,blocks)\n";
        script += "print(pos_teste)";

        File scriptFile = File.createTempFile("script", ".R");
        File outputFile = File.createTempFile("output", ".R");
        scriptFile.deleteOnExit();
        outputFile.deleteOnExit();

        try (FileWriter scriptWriter = new FileWriter(scriptFile)) {
            scriptWriter.append(script);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(System.getProperty("os.name").contains("win") ? "R.exe" : "R", "--slave", "-f", scriptFile.getAbsolutePath());
        processBuilder.redirectOutput(outputFile);

        Process process = processBuilder.start();
        process.waitFor();

        ArrayList<Map.Entry<String, double[]>> entrySets = new ArrayList<>(values.entrySet());

        HashMap<String, HashMap<String, Boolean>> result = new HashMap<>();

        for (int i = 0; i < entrySets.size() - 1; i++) {
            String entry1 = entrySets.get(i).getKey().replaceAll("\\\\", "\\\\\\\\");
            HashMap<String, Boolean> entry1Map = result.get(entry1);
            if (entry1Map == null) {
                entry1Map = new HashMap<>();
                result.put(entry1, entry1Map);
                entry1Map.put(entry1, false);
            }

            for (int j = i + 1; j < entrySets.size(); j++) {
                String entry2 = entrySets.get(j).getKey().replaceAll("\\\\", "\\\\\\\\");
                HashMap<String, Boolean> entry2Map = result.get(entry2);
                if (entry2Map == null) {
                    entry2Map = new HashMap<>();
                    result.put(entry2, entry2Map);
                    entry2Map.put(entry2, false);
                }

                try (Scanner scanner = new Scanner(outputFile)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.contains(entry1 + "-" + entry2)
                                || line.contains(entry2 + "-" + entry1)) {
                            if (line.contains("TRUE")) {
                                entry1Map.put(entry2, true);
                                entry2Map.put(entry1, true);
                                break;
                            } else if (line.contains("FALSE")) {
                                entry1Map.put(entry2, false);
                                entry2Map.put(entry1, false);
                                break;
                            }
                        }
                    }
                }
            }
        }

        scriptFile.delete();
        outputFile.delete();

        return result;
    }

}
