package org.uma.jmetal.measure.statistic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class VarghaDelaneyEffectSize {

    public static final String NEGLIGIBLE = "negligible";
    public static final String SMALL = "small";
    public static final String MEDIUM = "medium";
    public static final String LARGE = "large";

    public static HashMap<String, HashMap<String, Double>> computeEffectSize(HashMap<String, Double[]> values) throws IOException, InterruptedException {
        HashMap<String, HashMap<String, Double>> result = new HashMap<>();

        StringBuilder script = new StringBuilder();
        script.append("require(\"effsize\")\n");

        for (Map.Entry<String, Double[]> entrySet : values.entrySet()) {
            String group = entrySet.getKey();
            Double[] groupValues = entrySet.getValue();

            script.append("\"").append(group).append("\" <- c(");
            for (Double value : groupValues) {
                script.append(value).append(",");
            }
            script.deleteCharAt(script.length() - 1).append(")\n");
        }

        String[] groupArray = (String[]) values.keySet().toArray(new String[0]);

        for (int i = 0; i < groupArray.length - 1; i++) {
            String groupA = groupArray[i];
            for (int j = i + 1; j < groupArray.length; j++) {
                String groupB = groupArray[j];
                script.append("VD.A(get(\"").append(groupA).append("\"),get(\"").append(groupB).append("\"))\n");
            }
        }
        script.append("q()");

        File inputFile = File.createTempFile("script", ".R");
        File outputFile = File.createTempFile("output", ".R");
        inputFile.deleteOnExit();
        outputFile.deleteOnExit();

        new FileWriter(inputFile).append(script).flush();

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(System.getProperty("os.name").contains("win") ? "R.exe" : "R", "--slave", "-f", inputFile.getAbsolutePath());
        builder.redirectOutput(outputFile);
        builder.start().waitFor();

        Scanner scanner = new Scanner(outputFile);

        List<Double> comparisonValues = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("A estimate:")) {
                String[] split = line.split(" ");

                Double doubleValue = Double.parseDouble(split[2]);
                if (doubleValue.isNaN()) {
                    doubleValue = 0.0;
                }

                comparisonValues.add(doubleValue);
            }
        }

        Iterator<Double> iterator = comparisonValues.iterator();
        for (int i = 0; i < groupArray.length - 1; i++) {
            String groupA = groupArray[i];
            for (int j = i + 1; j < groupArray.length; j++) {
                String groupB = groupArray[j];
                HashMap<String, Double> groupAMap = result.get(groupA);
                if (groupAMap == null) {
                    groupAMap = new HashMap<>();
                    result.put(groupA, groupAMap);
                }
                HashMap<String, Double> groupBMap = result.get(groupB);
                if (groupBMap == null) {
                    groupBMap = new HashMap<>();
                    result.put(groupB, groupBMap);
                }
                Double value = iterator.next();
                groupAMap.put(groupB, value);
                groupBMap.put(groupA, 1 - value);
            }
        }

        inputFile.delete();
        outputFile.delete();

        return result;
    }

    public static String interpretEffectSize(double effectSize) {
        effectSize = StrictMath.abs(effectSize - 0.5) * 2;
        if (effectSize < 0.147) {
            return NEGLIGIBLE;
        } else if (effectSize < 0.33) {
            return SMALL;
        } else if (effectSize < 0.474) {
            return MEDIUM;
        } else {
            return LARGE;
        }
    }
}
