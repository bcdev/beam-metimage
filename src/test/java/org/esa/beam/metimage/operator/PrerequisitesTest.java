package org.esa.beam.metimage.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.esa.beam.metimage.MetImageConstants;
import org.esa.beam.metimage.math.MetImageHistogram;
import org.esa.beam.util.math.IndexValidator;
import org.junit.Before;
import org.junit.Test;
import util.MetImageUtils;

import static junit.framework.Assert.assertEquals;
import static org.esa.beam.GlobalTestTools.equal;

public class PrerequisitesTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testComputeHistogramZeroAlpha() {
        final double[][] values = new double[][]{
                new double[]{-3, -2, -1, 0, 1, 2, 3, 4},
                new double[]{-2, 0, 2, 4},
                new double[]{0, 1, 2}
        };
        final boolean unsigned = false;
        final int alpha = 0;

        final MetImageHistogram histogram = new MetImageHistogram(new int[6], -2, 3, alpha);
        for (int i = 0; i < values.length; i++) {
            histogram.aggregate(values[i], unsigned, IndexValidator.TRUE, ProgressMonitor.NULL);
        }
        histogram.computeDensityFunctions();

        final int[] expectedBinCounts = new int[]{2, 1, 3, 2, 3, 1};
        assertEquals("", equal(expectedBinCounts, histogram.getBinCounts()));
        assertEquals(6, histogram.getNumBins());

        assertEquals(7, histogram.getEqualBinBorders().length);
        final float[] expectedBinBorders = new float[]{-2.0f, -1.1666667f, -0.3333333f, 0.5f, 1.333333f, 2.166666f, 3.0f};
        for (int i = 0; i < expectedBinBorders.length; i++) {
            assertEquals(expectedBinBorders[i], histogram.getEqualBinBorders()[i], 1.E-6);
        }

        final float[] expectedPdf = new float[]{2.0f, 1.0f, 3.0f, 2.0f, 3.0f, 1.0f};
        for (int i = 0; i < expectedPdf.length; i++) {
            assertEquals(expectedPdf[i], histogram.getPdf()[i], 1.E-6);
        }

        final float[] expectedCdf = new float[]{0.0f, 0.166666f, 0.25f, 0.5f, 0.6666666f, 0.916666f, 1.0f};
        for (int i = 0; i < expectedCdf.length; i++) {
            assertEquals(expectedCdf[i], histogram.getCdf()[i], 1.E-6);
        }
    }

    @Test
    public void testComputeHistogramNonzeroAlpha() {
        final double[][] values = new double[][]{
                new double[]{-3, -2, -1, 0, 1, 2, 3, 4},
                new double[]{-2, 0, 2, 4},
                new double[]{0, 1, 2}
        };
        final boolean unsigned = false;
        final int alpha = 1;

        final MetImageHistogram histogram = new MetImageHistogram(new int[6], -2, 3, alpha);
        for (int i = 0; i < values.length; i++) {
            histogram.aggregate(values[i], unsigned, IndexValidator.TRUE, ProgressMonitor.NULL);
        }
        histogram.computeDensityFunctions();

        final int[] expectedBinCounts = new int[]{2, 1, 3, 2, 3, 1};
        assertEquals("", equal(expectedBinCounts, histogram.getBinCounts()));
        assertEquals(6, histogram.getNumBins());

        assertEquals(7, histogram.getEqualBinBorders().length);
        final float[] expectedBinBorders = new float[]{-2.0f, -1.1666667f, -0.3333333f, 0.5f, 1.333333f, 2.166666f, 3.0f};
        for (int i = 0; i < expectedBinBorders.length; i++) {
            assertEquals(expectedBinBorders[i], histogram.getEqualBinBorders()[i], 1.E-6);
        }

        final float[] expectedPdf = new float[]{2.038461f, 1.038461f, 3.038461f, 2.038461f, 3.038461f, 1.038461f};
        for (int i = 0; i < expectedPdf.length; i++) {
            assertEquals(expectedPdf[i], histogram.getPdf()[i], 1.E-6);
        }

        final float[] expectedCdf = new float[]{0.0f, 0.1666666f, 0.251572f, 0.5f, 0.6666666f, 0.915094f, 1.0f};
        for (int i = 0; i < expectedCdf.length; i++) {
            assertEquals(expectedCdf[i], histogram.getCdf()[i], 1.E-6);
        }
    }

    @Test
    public void testComputeHistogramEqualized() {
        double[] b = new double[25000];

        BetaDistribution beta=new BetaDistribution(2.0, 5.0);
        for(int i=0;i<b.length;i++) {
            b[i]=beta.sample();
        }

        int nBins = MetImageHistogram.findOptimalNumberOfBins(b);
        System.out.println("nBins = " + nBins);
//        assertEquals(57, nBins);

        // first get 'normal' histo with 1000 equally spaced bins...
        final double maxB = (new Max()).evaluate(b);
        final double minB = (new Min()).evaluate(b);
        final MetImageHistogram histo1 = new MetImageHistogram(new int[1000], minB, maxB, MetImageConstants.ALPHA);
        histo1.aggregate(b, false, IndexValidator.TRUE, ProgressMonitor.NULL);
        histo1.computeDensityFunctions();

        LinearInterpolator interpolator = new LinearInterpolator();

        final PolynomialSplineFunction histo1Func = interpolator.interpolate(MetImageUtils.getAsDoubles(histo1.getCdf()),
                                                                             MetImageUtils.getAsDoubles(histo1.getEqualBinBorders()));
        double[] equalBins = new double[nBins];
        double[] newBins = new double[nBins];
        for (int i = 0; i < newBins.length; i++) {
            equalBins[i] = i*1.0/(newBins.length-1);
            newBins[i] =  histo1Func.value(equalBins[i]);
        }

        // now get the unequal spaced histogram...
        final MetImageHistogram histo2 = new MetImageHistogram(new int[nBins-1], minB, maxB, MetImageConstants.ALPHA);
        histo2.setUnequalBinBorders(newBins);
        histo2.aggregateUnequalBins(b, false, IndexValidator.TRUE, ProgressMonitor.NULL);
        histo2.computeDensityFunctions();
        System.out.println();
        // todo: add reasonable assertions
    }


}