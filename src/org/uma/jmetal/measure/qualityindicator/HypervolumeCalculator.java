package org.uma.jmetal.measure.qualityindicator;

import java.io.FileNotFoundException;
import java.util.List;
import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;

public class HypervolumeCalculator extends Hypervolume<List<? extends Solution<?>>> {

    private final Hypervolume hypervolume;
    private double[] maximumValues;
    private double[] minimumValues;

    public HypervolumeCalculator() {
        this.hypervolume = new Hypervolume();
        this.maximumValues = null;
        this.minimumValues = null;
    }

    public double[] getMaximumValues() {
        return maximumValues;
    }

    public double[] getMinimumValues() {
        return minimumValues;
    }

    public void addParetoFront(Front front) {
        double[] tempMinimum = FrontUtils.getMinimumValues(front);
        double[] tempMaximum = FrontUtils.getMaximumValues(front);

        if (maximumValues == null) {
            maximumValues = tempMaximum;
        } else {
            for (int i = 0; i < tempMaximum.length; i++) {
                maximumValues[i] = Double.max(tempMaximum[i], maximumValues[i]);
            }
        }
        if (minimumValues == null) {
            minimumValues = tempMinimum;
        } else {
            for (int i = 0; i < tempMinimum.length; i++) {
                minimumValues[i] = Double.min(tempMinimum[i], minimumValues[i]);
            }
        }
    }

    public void addParetoFront(List<? extends Solution<?>> front) {
        addParetoFront(new ArrayFront(front));
    }

    public void addParetoFront(String path) throws FileNotFoundException {
        addParetoFront(new ArrayFront(path));
    }

    public void clear() {
        this.minimumValues = null;
        this.maximumValues = null;
    }

    public double calculateHypervolume(String frontPath) throws FileNotFoundException {
        return calculateHypervolume(new ArrayFront(frontPath));
    }

    public double calculateHypervolume(List<? extends Solution<?>> front) {
        return calculateHypervolume(new ArrayFront(front));
    }

    public double calculateHypervolume(Front front) {
        return calculateHypervolume(front, maximumValues, minimumValues);
    }

    public double calculateHypervolume(String frontPath, double[] maximumValues, double[] minimumValues) throws FileNotFoundException {
        return calculateHypervolume(new ArrayFront(frontPath), maximumValues, minimumValues);
    }

    public double calculateHypervolume(List<? extends Solution<?>> front, double[] maximumValues, double[] minimumValues) {
        return calculateHypervolume(new ArrayFront(front), maximumValues, minimumValues);
    }

    public double calculateHypervolume(Front front, double[] maximumValues, double[] minimumValues) {
        if (maximumValues != null && minimumValues != null) {
            Front normalizedFront = FrontUtils.getNormalizedFront(front, maximumValues, minimumValues);
            return hypervolume.calculateHypervolume(FrontUtils.convertFrontToArray(FrontUtils.getInvertedFront(normalizedFront)), front.getNumberOfPoints(), front.getPointDimensions());
        }
        return 0D;
    }

}
