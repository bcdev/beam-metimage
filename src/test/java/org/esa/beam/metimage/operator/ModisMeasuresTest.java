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
        double bt11000 = 12.0;
        double bt12000 = 15.0;
        double tSkin = 288.0;
        double bt11Sample3x3 = 12.0;
        double diffBt11Bt37Sample3x3 = 9.0;

        ModisMeasures measures = new ModisMeasures(true, true,
                                                   rho600, rho860, rho1380, bt3700,
                                                   bt7300, bt8600, bt11000, bt12000,
                                                   tSkin, bt11Sample3x3, diffBt11Bt37Sample3x3);

        // at night, over land:
        assertEquals(0.0, measures.heritageMeasureBT11(), 1.E-6);                                    // bt11000 - tSkin
        assertEquals(-3.0, measures.heritageMeasureSplitWindow(), 1.E-6);                            // bt11000 - bt12000
        assertEquals(-11.25, measures.heritageMeasureNegativeBT37minusBT11Night(), 1.E-6);           // bt3700 - bt11000 at night
        assertEquals(-11.25, measures.heritageMeasurePositiveBT37minusBT11NightMixedScene(), 1.E-6); // bt3700 - bt11000 at night
        assertTrue(Double.isNaN(measures.heritageMeasureSolarBrightnessThresholdsOcean()));          // NaN (ocean test)
        assertEquals(0.1, measures.heritageMeasureSolarBrightnessThresholdsLand(), 1.E-6);           // rho600 over land
        assertEquals(12.0, measures.heritageMeasureUniformity(), 1.E-6);                             // bt11Sample3x3

        assertEquals(0.25, measures.newMeasureR138WaterVapour(), 1.E-6);                           // rho1380
        assertEquals(-6.0, measures.newMeasureBT11(), 1.E-6);                                      // bt7300 - bt8600 over land
        assertTrue(Double.isNaN(measures.newMeasureCO2()));                                        // NaN (ocean test)
        assertEquals(-8.25, measures.newMeasureBT37minusBT87Deserts(), 1.E-6);                     // bt3700 - bt8600
        assertEquals(-112.5, measures.newMeasurePositiveBT37minusBT11Day06Glint(), 1.E-6);         // (bt3700 - bt11000) / rho600
        assertEquals(0.0, measures.newMeasureO2Absorption(), 1.E-6);    // todo: fill method       // 0.0
        assertEquals(108.0, measures.newMeasureUniformityTwoChannels(), 1.E-6); // todo: define 'combine' // bt11Sample3x3 * diffBt11Bt37Sample3x3

        // at night, over ocean:
        measures = new ModisMeasures(false, true,
                                     rho600, rho860, rho1380, bt3700,
                                     bt7300, bt8600, bt11000, bt12000,
                                     tSkin, bt11Sample3x3, diffBt11Bt37Sample3x3);
        assertTrue(Double.isNaN(measures.heritageMeasureSolarBrightnessThresholdsLand()));       // NaN (land test)
        assertEquals(0.15, measures.heritageMeasureSolarBrightnessThresholdsOcean(), 1.E-6);     // rho860 over ocean
        assertEquals(-9.0, measures.newMeasureBT11(), 1.E-6);                                    // bt7300 - bt11000 over ocean

        // now assume day:
        measures = new ModisMeasures(false, false,
                                     rho600, rho860, rho1380, bt3700,
                                     bt7300, bt8600, bt11000, bt12000,
                                     tSkin, bt11Sample3x3, diffBt11Bt37Sample3x3);
        assertTrue(Double.isNaN(measures.heritageMeasureNegativeBT37minusBT11Night()));           // NaN (night test)
        assertTrue(Double.isNaN(measures.heritageMeasurePositiveBT37minusBT11NightMixedScene())); // NaN (night test)
        assertEquals(1.2, measures.newMeasureUniformityTwoChannels(), 1.E-6); // todo: define 'combine' // bt11Sample3x3 * rho600
    }

}