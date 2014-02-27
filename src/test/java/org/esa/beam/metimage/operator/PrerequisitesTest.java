package org.esa.beam.metimage.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.esa.beam.metimage.math.MetImageHistogram;
import org.esa.beam.util.math.IndexValidator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import util.MetImageUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
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
        for (double[] value : values) {
            histogram.aggregate(value, unsigned, IndexValidator.TRUE, ProgressMonitor.NULL);
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
        for (double[] value : values) {
            histogram.aggregate(value, unsigned, IndexValidator.TRUE, ProgressMonitor.NULL);
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

        BetaDistribution beta = new BetaDistribution(2.0, 5.0);
        beta.reseedRandomGenerator(0);
        for (int i = 0; i < b.length; i++) {
            b[i] = beta.sample();
        }
        MetImageHistogram equalizedHistogram = MetImageHistogram.createAggregatedEqualizedHistogram(b);

        final double[] pdf = MetImageUtils.getAsPrimitiveDoubles(equalizedHistogram.getPdf());
        final double stDev = (new StandardDeviation()).evaluate(pdf);
        final double mean = (new Mean()).evaluate(pdf);
        final double min = (new Min()).evaluate(pdf);
        final double max = (new Max()).evaluate(pdf);
        System.out.println("#bins : " + equalizedHistogram.getUnequalBinBorders().length);
        System.out.println("mean = " + mean);
        System.out.println("stDev = " + stDev);
        System.out.println("min = " + min);
        System.out.println("max = " + max);
        // do some reasonable checks for a more or less 'equal' histogram...
        assertTrue(stDev < 0.01*mean);
        assertTrue(min > mean - 3.0*stDev);
        assertTrue(max < mean + 3.0*stDev);
    }

    @Test
    public void testWriteHistogramAsJson() {
        double[] b = new double[25000];

        BetaDistribution beta = new BetaDistribution(2.0, 5.0);
        beta.reseedRandomGenerator(0);
        for (int i = 0; i < b.length; i++) {
            b[i] = beta.sample();
        }
        MetImageHistogram equalizedHistogram = MetImageHistogram.createAggregatedEqualizedHistogram(b);

        final double[] pdf = MetImageUtils.getAsPrimitiveDoubles(equalizedHistogram.getPdf());
        final double stDev = (new StandardDeviation()).evaluate(pdf);
        final double mean = (new Mean()).evaluate(pdf);
        final double min = (new Min()).evaluate(pdf);
        final double max = (new Max()).evaluate(pdf);
        System.out.println("#bins : " + equalizedHistogram.getUnequalBinBorders().length);
        System.out.println("mean = " + mean);
        System.out.println("stDev = " + stDev);
        System.out.println("min = " + min);
        System.out.println("max = " + max);
        // do some reasonable checks for a more or less 'equal' histogram...
        assertTrue(stDev < 0.01*mean);
        assertTrue(min > mean - 3.0*stDev);
        assertTrue(max < mean + 3.0*stDev);


        // test JSON output:
        JSONObject histoObj = new JSONObject();

        JSONArray binBordersJsonArray = new JSONArray();
        JSONArray noCloudHistoJsonArray = new JSONArray();
        JSONArray cloudHistoJsonArray = new JSONArray();
//        final Double[] binBordersArray = new Double[]{0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
        final Double[] binBordersArray = MetImageUtils.getAsDoubles(equalizedHistogram.getUnequalBinBorders());
//        final Double[] noCloudHistoArray = new Double[]{27.0, 31.0, 18.0, 14.0, 67.0, 11.0};
        final Double[] noCloudHistoArray = MetImageUtils.getAsDoubles(equalizedHistogram.getPdf());
//        final Double[] cloudHistoArray = new Double[]{15.0, 22.0, 7.0, 36.0, 41.0, 19.0};
        final Double[] cloudHistoArray = MetImageUtils.getAsDoubles(equalizedHistogram.getPdf());
        for (int i = 0; i < binBordersArray.length-1; i++) {
            binBordersJsonArray.add(binBordersArray[i]);
            noCloudHistoJsonArray.add(noCloudHistoArray[i]);
            cloudHistoJsonArray.add(cloudHistoArray[i]);
        }
        histoObj.put("h_free", noCloudHistoJsonArray);
        histoObj.put("h_cloud", cloudHistoJsonArray);
        histoObj.put("bin_boundaries", binBordersJsonArray);

        try {

            // Writing to a file
            File file=new File("C:\\Users\\olafd\\metimage\\DAY_LAND_MIDLEVEL_N1.json");
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            System.out.println("Writing JSON object to file");
            System.out.println("-----------------------");
            System.out.print(histoObj);

            fileWriter.write(histoObj.toJSONString());
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}