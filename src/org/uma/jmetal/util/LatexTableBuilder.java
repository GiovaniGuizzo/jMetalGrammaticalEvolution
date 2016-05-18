package org.uma.jmetal.util;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Map;

public class LatexTableBuilder<T> {

    private String caption = "Table";
    private String label = "tab:Table";
    private String firstColumnHeader = "Instance";
    private boolean centerTable = true;
    private String tablePositioning = "htb";
    private double width = 1.0;
    private char columnPosition = 'c';
    private int fontSize = 0;
    private int fontSpacing = 0;
    private DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
    private Comparator<? super Map.Entry<String, ?>> entrySorter = (entry, entry2) -> entry.getKey().compareTo(entry2.getKey());

    public LatexTableBuilder() {
    }

    public String getCaption() {
        return caption;
    }

    public LatexTableBuilder setCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public LatexTableBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public boolean isCenterTable() {
        return centerTable;
    }

    public LatexTableBuilder setCenterTable(boolean centerTable) {
        this.centerTable = centerTable;
        return this;
    }

    public double getWidth() {
        return width;
    }

    public LatexTableBuilder setWidth(double width) {
        this.width = width;
        return this;
    }

    public char getColumnPosition() {
        return columnPosition;
    }

    public LatexTableBuilder setColumnPosition(char columnPosition) {
        this.columnPosition = columnPosition;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public LatexTableBuilder setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public int getFontSpacing() {
        return fontSpacing;
    }

    public LatexTableBuilder setFontSpacing(int fontSpacing) {
        this.fontSpacing = fontSpacing;
        return this;
    }

    public String getTablePositioning() {
        return tablePositioning;
    }

    public LatexTableBuilder setTablePositioning(String tablePositioning) {
        this.tablePositioning = tablePositioning;
        return this;
    }

    public String getFirstColumnHeader() {
        return firstColumnHeader;
    }

    public LatexTableBuilder setFirstColumnHeader(String firstColumnHeader) {
        this.firstColumnHeader = firstColumnHeader;
        return this;
    }

    public DecimalFormat getDecimalFormatter() {
        return decimalFormatter;
    }

    public LatexTableBuilder setDecimalFormatter(DecimalFormat decimalFormatter) {
        this.decimalFormatter = decimalFormatter;
        return this;
    }

    public Comparator<? super Map.Entry<String, T>> getEntrySorter() {
        return entrySorter;
    }

    public LatexTableBuilder setEntrySorter(Comparator<? super Map.Entry<String, ?>> entrySorter) {
        this.entrySorter = entrySorter;
        return this;
    }

    public String build(Map<String, Map<String, T>> data) {
        StringBuilder tableBuilder = new StringBuilder();

        tableBuilder.append(buildTableHeader(data));
        tableBuilder.append(buildTableCore(data));
        tableBuilder.append(buildTableFooter());

        return tableBuilder.toString();
    }

    public String buildTableHeader(Map<String, Map<String, T>> data) {
        StringBuilder tableBuilder = new StringBuilder();

        tableBuilder.append("\\begin{table}[!").append(tablePositioning).append("]\n");
        if (centerTable) {
            tableBuilder.append("\t\\centering\n");
        }
        if (fontSize > 0 || fontSpacing > 0) {
            tableBuilder
                    .append("\t\\fontsize{")
                    .append(fontSize > 0 ? Integer.valueOf(fontSize) : "10").append("pt}{")
                    .append(fontSpacing > 0 ? Integer.valueOf(fontSpacing) : "10").append("pt}\\selectfont\n");
        }

        tableBuilder.append("\t\\caption{").append(caption).append("}\n")
                .append("\t\\label{").append(label).append("}\n")
                .append("\t\\begin{tabulary}{").append(Double.valueOf(width)).append("\\linewidth}{").append(columnPosition);

        data.values().iterator().next().entrySet().stream().forEach((item) -> {
            tableBuilder.append(columnPosition);
        });
        tableBuilder.append("}\n")
                .append("\t\t\\toprule\n")
                .append("\t\t\\textbf{").append(firstColumnHeader).append("} ");
        data.values().iterator().next().entrySet().stream().sorted(entrySorter).forEach((item) -> {
            tableBuilder.append("& \\textbf{").append(item.getKey().replaceAll("\\_", "\\\\_")).append("} ");
        });
        tableBuilder.append("\\\\\n")
                .append("\t\t\\midrule\n");

        return tableBuilder.toString();
    }

    public String buildTableCore(Map<String, Map<String, T>> data) {
        StringBuilder tableBuilder = new StringBuilder();

        data.entrySet().stream().sorted(entrySorter).forEach((problemEntry) -> {
            String problem = problemEntry.getKey();
            Map<String, T> problemData = problemEntry.getValue();

            tableBuilder.append("\t\t").append(problem.replaceAll("\\_", "\\\\_")).append(" ");

            problemData.entrySet().stream().sorted(entrySorter).forEach((item) -> {
                tableBuilder.append("& ").append(item.getValue() instanceof Double ? decimalFormatter.format((Double) item.getValue()) : item.getValue().toString()).append(" ");
            });
            tableBuilder.append("\\\\\n");
        });

        return tableBuilder.toString();
    }

    public String buildTableFooter() {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("\t\t\\bottomrule\n")
                .append("\t\\end{tabulary}\n")
                .append("\\end{table}\n");
        return tableBuilder.toString();
    }

}
