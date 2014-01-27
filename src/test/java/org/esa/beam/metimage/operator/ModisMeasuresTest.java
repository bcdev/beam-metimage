package org.esa.beam.metimage.operator;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ModisMeasuresTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testModisMeasures() {
        double rho600 = 0.1;
        double rho860 = 0.15;
        double rho1380 = 0.25;
        double bt3700 = 0.75;
        double bt7300 = 3.0;
        double bt8600 = 9.0;
        double bt11000 = 4.405360;
        double bt12000 = 15.0;
        double tSkin = 284.98;
        double bt11Sample3x3 = 12.0;
        double diffBt11Bt37Sample3x3 = 9.0;

        // at night, over land:
        assertEquals(-30.0, ModisMeasures.heritageMeasureBT11(bt11000, tSkin), 1.E-2);                                    // bt11000 - tSkin
        bt11000 = 12.0;
        assertEquals(-3.0, ModisMeasures.heritageMeasureSplitWindow(bt11000, bt12000), 1.E-6);                            // bt11000 - bt12000
        assertEquals(-11.25, ModisMeasures.heritageMeasureNegativeBT37minusBT11Night(bt3700, bt11000, true), 1.E-6);           // bt3700 - bt11000 at night
        assertEquals(-11.25, ModisMeasures.heritageMeasurePositiveBT37minusBT11NightMixedScene(bt3700, bt11000, true), 1.E-6); // bt3700 - bt11000 at night
        assertTrue(Double.isNaN(ModisMeasures.heritageMeasureSolarBrightnessThresholdsOcean(rho860, true)));          // NaN (ocean test)
        assertEquals(0.1, ModisMeasures.heritageMeasureSolarBrightnessThresholdsLand(rho600, true), 1.E-6);           // rho600 over land
        assertEquals(12.0, ModisMeasures.heritageMeasureUniformity(bt11Sample3x3), 1.E-6);                             // bt11Sample3x3

        assertEquals(0.25, ModisMeasures.newMeasureR138WaterVapour(rho1380), 1.E-6);                           // rho1380
        assertEquals(-6.0, ModisMeasures.newMeasureBT11(bt7300, bt8600, bt11000, true), 1.E-6);                                      // bt7300 - bt8600 over land
        assertTrue(Double.isNaN(ModisMeasures.newMeasureCO2()));                                        // NaN (ocean test)
        assertEquals(-8.25, ModisMeasures.newMeasureBT37minusBT87Deserts(bt3700, bt8600), 1.E-6);                     // bt3700 - bt8600
        assertEquals(-112.5, ModisMeasures.newMeasurePositiveBT37minusBT11Day06Glint(bt3700, bt11000, rho600), 1.E-6);         // (bt3700 - bt11000) / rho600
        assertEquals(0.0, ModisMeasures.newMeasureO2Absorption(0.0), 1.E-6);    // todo: fill method       // 0.0
        assertEquals(108.0, ModisMeasures.newMeasureUniformityTwoChannels(bt11Sample3x3, diffBt11Bt37Sample3x3, rho600, true), 1.E-6); // todo: define 'combine' // bt11Sample3x3 * diffBt11Bt37Sample3x3

        // at night, over ocean:
        assertTrue(Double.isNaN(ModisMeasures.heritageMeasureSolarBrightnessThresholdsLand(rho600, false)));       // NaN (land test)
        assertEquals(0.15, ModisMeasures.heritageMeasureSolarBrightnessThresholdsOcean(rho860, false), 1.E-6);     // rho860 over ocean
        assertEquals(-9.0, ModisMeasures.newMeasureBT11(bt7300, bt8600, bt11000, false), 1.E-6);                                    // bt7300 - bt11000 over ocean

        // now assume day:
        assertTrue(Double.isNaN(ModisMeasures.heritageMeasureNegativeBT37minusBT11Night(bt3700, bt11000, false)));           // NaN (night test)
        assertTrue(Double.isNaN(ModisMeasures.heritageMeasurePositiveBT37minusBT11NightMixedScene(bt3700, bt11000, false))); // NaN (night test)
        assertEquals(1.2, ModisMeasures.newMeasureUniformityTwoChannels(bt11Sample3x3, diffBt11Bt37Sample3x3, rho600, false), 1.E-6); // todo: define 'combine' // bt11Sample3x3 * rho600
    }

}