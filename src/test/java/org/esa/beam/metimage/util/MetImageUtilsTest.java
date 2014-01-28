package org.esa.beam.metimage.util;

import org.esa.beam.metimage.operator.ModisMeasures;
import org.junit.Before;
import org.junit.Test;
import util.MetImageUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class MetImageUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testConvertModisEmissiveRadianceToTemperature() {
        // this test follows Python test 'test_it()' in modis_l1b.py provided by RP
        int bandnumber = 20;  // 3785nm
        double radiance = 0.388822; //  W/(m2 sr um)
        assertEquals(294.72, ModisMeasures.convertModisEmissiveRadianceToTemperature(radiance, bandnumber), 1.E-2);

        bandnumber = 24;  // 4472nm
        radiance = 0.834006; //  W/(m2 sr um)
        assertEquals(284.96, ModisMeasures.convertModisEmissiveRadianceToTemperature(radiance, bandnumber), 1.E-2);

        bandnumber = 27;  // 6766nm
        radiance = 3.683158; //  W/(m2 sr um)
        assertEquals(274.92, ModisMeasures.convertModisEmissiveRadianceToTemperature(radiance, bandnumber), 1.E-2);

        bandnumber = 31;  // 11012nm
        radiance = 4.405360; //  W/(m2 sr um)
        assertEquals(254.98, ModisMeasures.convertModisEmissiveRadianceToTemperature(radiance, bandnumber), 1.E-2);

        bandnumber = 36;  // 14193nm
        radiance = 4.61078; //  W/(m2 sr um)
        assertEquals(265.0, ModisMeasures.convertModisEmissiveRadianceToTemperature(radiance, bandnumber), 1.E-2);
    }

}
