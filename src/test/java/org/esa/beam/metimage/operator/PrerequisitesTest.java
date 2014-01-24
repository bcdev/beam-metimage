package org.esa.beam.metimage.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.esa.beam.metimage.MetImageConstants;
import org.esa.beam.metimage.math.DistinctionSkill;
import org.esa.beam.metimage.math.MetImageHistogram;
import org.esa.beam.util.math.IndexValidator;
import org.junit.Before;
import org.junit.Test;

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

        assertEquals(7, histogram.getBinBorders().length);
        final float[] expectedBinBorders = new float[]{-2.0f, -1.1666667f, -0.3333333f, 0.5f, 1.333333f, 2.166666f, 3.0f};
        for (int i = 0; i < expectedBinBorders.length; i++) {
            assertEquals(expectedBinBorders[i], histogram.getBinBorders()[i], 1.E-6);
        }

        final float[] expectedPdf = new float[]{2.0f, 1.0f, 3.0f, 2.0f, 3.0f, 1.0f};
        for (int i = 0; i < expectedPdf.length; i++) {
            assertEquals(expectedPdf[i], histogram.getPdf()[i], 1.E-6);
        }

        final float[] expectedCdf = new float[]{0.0f, 0.18181818f, 0.272727f, 0.545454f, 0.727272f, 1.0f};
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

        assertEquals(7, histogram.getBinBorders().length);
        final float[] expectedBinBorders = new float[]{-2.0f, -1.1666667f, -0.3333333f, 0.5f, 1.333333f, 2.166666f, 3.0f};
        for (int i = 0; i < expectedBinBorders.length; i++) {
            assertEquals(expectedBinBorders[i], histogram.getBinBorders()[i], 1.E-6);
        }

        final float[] expectedPdf = new float[]{2.038461f, 1.038461f, 3.038461f, 2.038461f, 3.038461f, 1.038461f};
        for (int i = 0; i < expectedPdf.length; i++) {
            assertEquals(expectedPdf[i], histogram.getPdf()[i], 1.E-6);
        }

        final float[] expectedCdf = new float[]{0.0f, 0.1821305f, 0.274914f, 0.5463917f, 0.728522f, 1.0f};
        for (int i = 0; i < expectedCdf.length; i++) {
            assertEquals(expectedCdf[i], histogram.getCdf()[i], 1.E-6);
        }
    }

}