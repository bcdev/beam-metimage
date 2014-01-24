package org.esa.beam.metimage.math;

import org.esa.beam.metimage.MetImageConstants;
import org.esa.beam.util.math.Histogram;

/**
 * Histogram class for MetImage purposes
 *
 * @authors Marco Zuehlke, Olaf Danne
 */
public class MetImageHistogram extends Histogram {

    private float[] binBorders;      // one element more than number of bins!
    private float[] pdf;
    private float[] equalizedPdf;
    private float[] cdf;             // todo: check if needed
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
        this.cdf = new float[binCounts.length];
        this.pdf = new float[binCounts.length];
        this.equalizedPdf = new float[binCounts.length];
        this.alpha = alpha;
        computeBinBorders();
    }

    private void computeBinBorders() {
        binBorders = new float[pdf.length+1];
        for (int i = 0; i < binBorders.length; i++) {
            binBorders[i] = (float) getMin() + i*((float) getMax() - (float) getMin())/(binBorders.length-1);
        }
    }

    public float[] getBinBorders() {
        return binBorders;
    }

    public void computeDensityFunctions() {
        computePdf();
        computeEqualizedPdf();
        computeCdf();
    }

    public float[] getPdf() {
        return pdf;
    }

    public float[] getEqualizedPdf() {
        return equalizedPdf;
    }

    public float[] getCdf() {
        return cdf;
    }


    private void computePdf() {
        for (int i = 0; i < getBinCounts().length; i++) {
            pdf[i] = (float) getBinCounts()[i];
        }
        normalizeAlpha();
    }

    private void normalizeAlpha() {
        for (int i = 0; i < pdf.length; i++) {
            pdf[i] += alpha*1.0/(pdf.length + alpha* MetImageConstants.NUM_BINS);
        }
    }

    private void computeEqualizedPdf() {
        equalizedPdf = pdf;

        // todo: check what we really need to do here, then activate
//        float[] normPdf = normalizeDist(pdf);
//        float sumr, sumrx;
//        sumr = sumrx = 0;
//        for (int i = 0; i < pdf.length; i++) {
//            sumr += pdf[i];
//            sumrx = (pdf.length-1) * sumr;
//            int valr = (int) (sumrx);
//            equalizedPdf[i] = valr;
//        }
    }


    private void computeCdf() {
        final int nCdf = cdf.length;
        cdf[nCdf-1] = getCumulativeSum(pdf, 0, nCdf-1);
        for (int i=0; i<nCdf; i++) {
            cdf[i] = getCumulativeSum(pdf, 0, i) / cdf[nCdf-1];
        }
    }

//    private static float[] normalizeDist(float[] inputDist) {
//        float[] normalizedPdf = new float[inputDist.length];
//        float sumV = 0.0f;
//        for (int i = 0; i < inputDist.length; i++) {
//            sumV = sumV + inputDist[i];
//        }
//        for (int i = 0; i < inputDist.length; i++) {
//            normalizedPdf[i] = inputDist[i]/sumV;
//        }
//        return normalizedPdf;
//    }

    private float getCumulativeSum(float[] array, int startIndex, int endIndex) {
        float sum = 0.0f;
        final int start = Math.max(0, startIndex);
        final int end = Math.min(array.length, endIndex);
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

}
