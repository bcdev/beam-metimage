package org.esa.beam.metimage.math;

import org.esa.beam.util.math.Histogram;

/**
 * Histogram class for MetImage purposes
 *
 * @authors Marco Zuehlke, Olaf Danne
 */
public class MetImageHistogram extends Histogram {

    private float[] pdf;
    private float[] equalizedPdf;
    private float[] cdf;             // todo: check if needed

    /**
     * Constructs a new instance for the given bin counts and the given value range.
     *
     * @param binCounts the bin counts
     * @param min       the minimum value
     * @param max       the maximum value
     */
    public MetImageHistogram(int[] binCounts, double min, double max) {
        super(binCounts, min, max);
        this.pdf = new float[binCounts.length];
        this.equalizedPdf = new float[binCounts.length];
        this.cdf = new float[binCounts.length];
    }

    public void computeEqualizedPdf() {
        equalizedPdf = normalizePdf(pdf);

        // todo: check what we really need to do here, then activate
//        float[] normPdf = normalizePdf(pdf);
//        float sumr, sumrx;
//        sumr = sumrx = 0;
//        for (int i = 0; i < pdf.length; i++) {
//            sumr += pdf[i];
//            sumrx = (pdf.length-1) * sumr;
//            int valr = (int) (sumrx);
//            equalizedPdf[i] = valr;
//        }
    }

    public float[] getEqualizedPdf() {
        return equalizedPdf;
    }

    public void computePdf() {
        for (int i = 0; i < getBinCounts().length; i++) {
            pdf[i] = (float) getBinCounts()[i];
        }
    }

    private static float[] normalizePdf(float[] inputPdf) {
        float[] normalizedPdf = new float[inputPdf.length];
        float sumV = 0.0f;
        for (int i = 0; i < inputPdf.length; i++) {
            sumV = sumV + inputPdf[i];
        }
        for (int i = 0; i < inputPdf.length; i++) {
            normalizedPdf[i] = inputPdf[i]/sumV;
        }
        return normalizedPdf;
    }
}
