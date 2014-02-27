package org.esa.beam.metimage.util;

import org.junit.Before;
import org.junit.Test;
import util.MetImageUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MetImageUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetCumulativeSum() throws Exception {

        double[] dArray = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        double cumulativeSum = MetImageUtils.getCumulativeSum(dArray, 4);
        assertEquals(10.0, cumulativeSum, 1.E-6);
        cumulativeSum = MetImageUtils.getCumulativeSum(dArray, 5);
        assertEquals(15.0, cumulativeSum, 1.E-6);
        cumulativeSum = MetImageUtils.getCumulativeSum(dArray, 0);
        assertEquals(0.0, cumulativeSum, 1.E-6);
        cumulativeSum = MetImageUtils.getCumulativeSum(dArray, 10);
        assertEquals(15.0, cumulativeSum, 1.E-6);
    }

    @Test
    public void testGetCumulativeSumAndNormalize() throws Exception {

        double[] dArray = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        double[] normArray = MetImageUtils.getCumulativeSumAndNormalize(dArray);
        assertNotNull(normArray);
        assertEquals(6, normArray.length, 1.E-6);
        // 0/15, 1/15, (1+2)/15, (1+2+3)/15, (1+2+3+4)/15, (1+2+3+4+5)/15:
        assertEquals(0.0, normArray[0], 1.E-6);
        assertEquals(0.0666667, normArray[1], 1.E-6);
        assertEquals(0.2, normArray[2], 1.E-6);
        assertEquals(0.4, normArray[3], 1.E-6);
        assertEquals(0.6666667, normArray[4], 1.E-6);
        assertEquals(1.0, normArray[5], 1.E-6);
    }

    @Test
    public void testGetAsDoubles() throws Exception {
        float[] fArr = new float[]{1.0f, 2.0f};
        final double[] dArr = MetImageUtils.getAsPrimitiveDoubles(fArr);
        assertNotNull(dArr);
        assertEquals("[D", dArr.getClass().getName());
        assertEquals(1.0, dArr[0], 1.E-6);
        assertEquals(2.0, dArr[1], 1.E-6);
    }

    @Test
    public void testGetAsPrimitiveDoubles() throws Exception {
        Double[] dArr = new Double[]{1.0, 2.0};
        final double[] dPrimArr = MetImageUtils.getAsPrimitiveDoubles(dArr);
        assertNotNull(dPrimArr);
        assertEquals("[D", dPrimArr.getClass().getName());
        assertEquals(1.0, dPrimArr[0], 1.E-6);
        assertEquals(2.0, dPrimArr[1], 1.E-6);
    }
}
