package org.uma.jmetal.util;

import java.util.Comparator;
import java.util.Map;

public class MapToLatexConverter {

    private static final Comparator<Map.Entry<String, ? extends Object>> DEFAULT_ENTRY_COMPARATOR = (entry, entry2) -> entry.getKey().compareTo(entry2.getKey());

    public static <T> String convert(String title, String label, Map<String, Map<String, T>> data) {
        StringBuilder tableBuilder = new StringBuilder();

        tableBuilder.append(composeTableHeader(title, label, data));
        tableBuilder.append(composeTableCore(data));
        tableBuilder.append(composeTableFooter());

        return tableBuilder.toString();
    }

    public static <T> String composeTableHeader(String title, String label, Map<String, Map<String, T>> data) {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder
                .append("\\begin{table}[!htb]\n")
                .append("\t\\centering\n")
                .append("\t\\fontsize{7pt}{8pt}\\selectfont\n")
                .append("\t\\caption{").append(title).append("}\n")
                .append("\t\\label{tab:").append(label).append("}\n")
                .append("\t\\begin{tabulary}{\\linewidth}{c");

        data.values().iterator().next().entrySet().stream().forEach((item) -> {
            tableBuilder.append("c");
        });
        tableBuilder.append("}\n")
                .append("\t\t\\toprule\n")
                .append("\t\t\\textbf{System} ");
        data.values().iterator().next().entrySet().stream().sorted(DEFAULT_ENTRY_COMPARATOR).forEach((item) -> {
            tableBuilder.append("& \\textbf{").append(item.getKey().replaceAll("\\_", "\\\\_")).append("} ");
        });
        tableBuilder.append("\\\\\n")
                .append("\t\t\\midrule\n");

        return tableBuilder.toString();
    }

    public static <T> String composeTableCore(Map<String, Map<String, T>> data) {
        StringBuilder tableBuilder = new StringBuilder();

        data.entrySet().stream().sorted(DEFAULT_ENTRY_COMPARATOR).forEach((problemEntry) -> {
            String problem = problemEntry.getKey();
            Map<String, T> problemData = problemEntry.getValue();

            tableBuilder.append("\t\t").append(problem.replaceAll("\\_", "\\\\_")).append(" ");

            problemData.entrySet().stream().sorted(DEFAULT_ENTRY_COMPARATOR).forEach((item) -> {
                tableBuilder.append("& ").append(item.getValue() instanceof Double ? String.format("%.2f", (Double) item.getValue()) : item.getValue().toString()).append(" ");
            });
            tableBuilder.append("\\\\\n");
        });

        return tableBuilder.toString();
    }

    public static String composeTableFooter() {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("\t\t\\bottomrule\n")
                .append("\t\\end{tabulary}\n")
                .append("\\end{table}\n");
        return tableBuilder.toString();
    }

}
