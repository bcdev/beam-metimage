package org.esa.beam.metimage.math;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.esa.beam.metimage.MetImageConstants;
import util.MetImageUtils;

/**
 * Class providing the distinction skill computation as proposed by R.Preusker, FUB
 *
 * @author M. Zuehlke, O. Danne
 */
public class DistinctionSkill {

    public static double computeDistinctionSkillFromCramerMisesAndersonMetric(MetImageHistogram noCloudHisto,
                                                                              MetImageHistogram cloudHisto,
                                                                              int numA, int numB) {
        final double[] pdfA = MetImageUtils.getAsDoubles(noCloudHisto.getPdf());
        final double[] pdfB = MetImageUtils.getAsDoubles(cloudHisto.getPdf());
        final double[] cdfA = MetImageUtils.getAsDoubles(noCloudHisto.getCdf());
        final double[] cdfB = MetImageUtils.getAsDoubles(cloudHisto.getCdf());
        final double[] bins = MetImageUtils.getAsDoubles(noCloudHisto.getEqualBinBorders());
        return computeDistinctionSkillFromCramerMisesAndersonMetric(pdfA, pdfB, cdfA, cdfB, bins, numA, numB);
    }


    /**
     * Computes the distinction skill based on Cramer-v.Mises-Anderson Metric following R.Preusker, FUB
     * (Python implementation: MI_tools.py)
     *
     * @param pdfa - empirical pdf A
     * @param pdfb - empirical pdf B
     * @param bins - the bins (one element more than the pdfs!)
     * @param numA - number of samples of pdfa
     * @param numB - number of samples of pdfb  (Needed to calculate the empirical pdf of the two functions together.
                     If not given, equal numbers are assumed.)
     * @return double - the distinction skill
     */
    public static double computeDistinctionSkillFromCramerMisesAndersonMetric(double[] pdfa, double[] pdfb, double[] bins,
                                                                              int numA, int numB) {

        final int nPdfa = pdfa.length;
        final int nPdfb = pdfb.length;
        final int nBins = bins.length;

        checkHistogramDimensions(nPdfa, nPdfb, nBins);

        double[] cdfa = new double[nPdfa+1];
        double[] cdfb = new double[nPdfb+1];
        final int nCdfa = cdfa.length;
        final int nCdfb = cdfb.length;

        MetImageUtils.getCumulativeSumAndNormalize(pdfa, cdfa, nCdfa);
        MetImageUtils.getCumulativeSumAndNormalize(pdfb, cdfb, nCdfb);

        return computeDistinctionSkillFromCramerMisesAndersonMetric(pdfa, pdfb, cdfa, cdfb, bins, numA, numB);
    }

    /**
     * Computes the distinction skill based on Cramer-v.Mises-Anderson Metric following R.Preusker, FUB
     * (Python implementation: MI_tools.py)
     *
     * @param pdfa - empirical pdf A
     * @param pdfb - empirical pdf B
     * @param cdfa - empirical cdf A
     * @param cdfb - empirical cdf B
     * @param bins - the bins (one element more than the pdfs!)
     * @param numA - number of samples of pdfa
     * @param numB - number of samples of pdfb  (Needed to calculate the empirical pdf of the two functions together.
    If not given, equal numbers are assumed.)
     * @return double - the distinction skill
     */
    public static double computeDistinctionSkillFromCramerMisesAndersonMetric(double[] pdfa, double[] pdfb,
                                                                              double[] cdfa, double[] cdfb,
                                                                              double[] bins,
                                                                              int numA, int numB) {

        final int nPdfa = pdfa.length;
        final int nPdfb = pdfb.length;
        final int nBins = bins.length;

        checkHistogramDimensions(nPdfa, nPdfb, nBins);

        final double[] cdfab = getCdfAb(cdfa, cdfb, numA, numB);

        LinearInterpolator interpolator = new LinearInterpolator();
        final PolynomialSplineFunction fCdfa = interpolator.interpolate(bins, cdfa);
        final PolynomialSplineFunction fCdfb = interpolator.interpolate(bins, cdfb);
        final PolynomialSplineFunction fCdfab = interpolator.interpolate(bins, cdfab);

        double[] quantiles = new double[MetImageConstants.NUM_QUANTILES];
        double[] distances = new double[MetImageConstants.NUM_QUANTILES];
        final double increment = (bins[nBins-1] - bins[0])/quantiles.length;
        for (int i = 0; i < quantiles.length; i++) {
            quantiles[i] = bins[0] + i*increment;
            final double fcdfaValue = fCdfa.value(quantiles[i]);
            final double fcdfbValue = fCdfb.value(quantiles[i]);
            distances[i] = (fcdfaValue-fcdfbValue) * (fcdfaValue-fcdfbValue);
        }

        return 3.0 * trapezIntegration(distances, fCdfab, quantiles);
    }

    private static void checkHistogramDimensions(int nPdfa, int nPdfb, int nBins) {
        if (nPdfa != nPdfb) {
            throw new IllegalArgumentException("Histograms have unequal length - cannot proceed.");
        }

        if (nBins != nPdfa + 1) {
            throw new IllegalArgumentException("Number of histogram bins does not match - cannot proceed.");
        }
    }

    private static double[] getCdfAb(double[] cdfa, double[] cdfb, int numCloud, int numNoCloud) {
        double[] cdfAb = new double[cdfa.length];
        for (int i=0; i<cdfa.length; i++) {
            cdfAb[i] = (cdfa[i]*numCloud + cdfb[i]*numNoCloud)/(numCloud + numNoCloud);
        }
        return cdfAb;
    }

    private static double trapezIntegration(double[] distances, PolynomialSplineFunction fCdfab, double[] quantiles) {

        //   x  : fCdfab(quantiles[])
        // f(x) : double[] distances

        // non-uniform spacing: integral = sum_k=0,N-1 [ (x_k+1 - x_k)*(f(x_k+1) + f(x_k)) ]
        // see: http://en.wikipedia.org/wiki/Trapezoidal_rule

        double integral = 0.0;
        for (int k = 0; k < quantiles.length-1; k++) {
            final double xk1 = fCdfab.value(quantiles[k+1]);
            final double xk = fCdfab.value(quantiles[k]);
            final double fk1 = distances[k+1];
            final double fk = distances[k];
            final double sumK = (xk1 - xk) * (fk1 + fk);
            integral += sumK;
        }
        integral /= 2.0;

        return integral;
    }

}
