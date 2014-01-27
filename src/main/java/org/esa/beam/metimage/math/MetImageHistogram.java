package org.esa.beam.metimage.math;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.esa.beam.metimage.MetImageConstants;
import org.esa.beam.util.Guardian;
import org.esa.beam.util.math.Histogram;
import org.esa.beam.util.math.IndexValidator;
import org.esa.beam.util.math.Range;

import java.util.Arrays;

/**
 * Histogram class for MetImage purposes
 *
 * @author Marco Zuehlke, Olaf Danne
 */
public class MetImageHistogram extends Histogram {

    private float[] equalBinBorders;      // one element more than number of bins!
    private double[] unequalBinBorders;    // one element more than number of bins!
    private float[] pdf;
    private float[] cdf;
    private int alpha;

    /**
     * Constructs a new instance for the given bin counts and the given value range.
     *
     * @param binCounts the bin counts
     * @param min       the minimum value
     * @param max       the maximum value
     */
    public MetImageHistogram(int[] binCounts, double min, double max, int alpha) {
        super(binCounts, min, max);
        this.cdf = new float[binCounts.length + 1];
        this.pdf = new float[binCounts.length];
        this.alpha = alpha;
        computeEqualBinBorders();
    }

    private void computeEqualBinBorders() {
        equalBinBorders = new float[pdf.length + 1];
        for (int i = 0; i < equalBinBorders.length; i++) {
            equalBinBorders[i] = (float) getMin() + i * ((float) getMax() - (float) getMin()) / (equalBinBorders.length - 1);
        }
    }

    public float[] getEqualBinBorders() {
        return equalBinBorders;
    }

    public double[] getUnequalBinBorders() {
        return unequalBinBorders;
    }

    public void setUnequalBinBorders(double[] unequalBinBorders) {
        this.unequalBinBorders = unequalBinBorders;
    }

    public void computeDensityFunctions() {
        computePdf();
        computeCdf();
    }

    public float[] getPdf() {
        return pdf;
    }

    public float[] getCdf() {
        return cdf;
    }

    public static int findOptimalNumberOfBins(double[] b) {

        Arrays.sort(b);
        Percentile pp = new Percentile();
        final double p75 = pp.evaluate(b, 75.0);
        final double p25 = pp.evaluate(b, 25.0);

        final double pDiff = p75 - p25;
        final double h = 2.0 * pDiff / (Math.pow(b.length, 1. / 3.));

        final double maxB = (new Max()).evaluate(b);
        final double minB = (new Min()).evaluate(b);

        return (int) Math.ceil((maxB - minB) / h);
    }

    public void aggregateUnequalBins(final double[] values,
                                     final IndexValidator validator,
                                     ProgressMonitor pm) {
        Guardian.assertNotNull("validator", validator);
        final Histogram histogram = computeHistogramUnequalBins(values, getUnequalBinBorders(), validator,
                getNumBins(), new Range(getMin(), getMax()),
                null, pm);
        final int[] newCounts = histogram.getBinCounts();
        final int[] thisCounts = getBinCounts();
        for (int i = 0; i < thisCounts.length; i++) {
            thisCounts[i] += newCounts[i];
        }
    }


    private void computePdf() {
        for (int i = 0; i < getBinCounts().length; i++) {
            pdf[i] = (float) getBinCounts()[i];
        }
        normalizeAlpha();
    }

    private void normalizeAlpha() {
        for (int i = 0; i < pdf.length; i++) {
            pdf[i] += alpha * 1.0 / (pdf.length + alpha * MetImageConstants.NUM_BINS);
        }
    }

    private void computeCdf() {
        final int nCdf = cdf.length;
        cdf[nCdf - 1] = getCumulativeSum(pdf, 0, nCdf - 1);
        for (int i = 0; i < nCdf; i++) {
            cdf[i] = getCumulativeSum(pdf, 0, i) / cdf[nCdf - 1];
        }
    }

    private float getCumulativeSum(float[] array, int startIndex, int endIndex) {
        float sum = 0.0f;
        final int start = Math.max(0, startIndex);
        final int end = Math.min(array.length, endIndex);
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

    private Histogram computeHistogramUnequalBins(final double[] values,
                                                  double[] binBorders,
                                                  final IndexValidator validator,
                                                  final int numBins,
                                                  Range range,
                                                  Histogram histogram,
                                                  ProgressMonitor pm) {
        Guardian.assertNotNull("validator", validator);
        final int numValues = values.length;
        final int[] binVals = new int[numBins];
        pm.beginTask("Computing histogram", range == null ? 2 : 1);
        try {
            if (range == null) {
                range = computeRangeDouble(values, validator, range, SubProgressMonitor.create(pm, 1));
            }
            final double min = range.getMin();
            final double max = range.getMax();
            double value;
            int binIndex;
            ProgressMonitor subPm = SubProgressMonitor.create(pm, 1);
            subPm.beginTask("Computing histogram", numValues);
            try {
                for (int i = 0; i < numValues; i++) {
                    if (validator.validateIndex(i)) {
                        value = values[i];
                        if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                            if (value >= min && value <= max) {
                                binIndex = findUnequalBinIndex(value, binBorders);
                                if (binIndex == numBins) {
                                    binIndex = numBins - 1;
                                }
                                binVals[binIndex]++;
                            }
                        }
                    }
                    subPm.worked(1);
                }
            } finally {
                subPm.done();
            }
            if (histogram != null) {
                histogram.setBinCounts(binVals, min, max);
            } else {
                histogram = new Histogram(binVals, min, max);
            }
        } finally {
            pm.done();
        }
        return histogram;
    }

    private int findUnequalBinIndex(double value, double[] binBorders) {
        for (int i = 1; i < binBorders.length; i++) {
            if (value < binBorders[i]) {
                return i - 1;
            }
        }
        return binBorders.length - 1;
    }

}
