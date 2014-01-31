package org.esa.beam.metimage.operator;

import org.esa.beam.metimage.MetImageConstants;

/**
 * MODIS measures for cloud tests as proposed by R.Preusker, FUB
 *
 * @author M. Zuehlke, O. Danne
 */
public class ModisMeasures {

    // quantities:
    // RefSB.13 (667nm)
    // RefSB.16 (869.5nm)
    // RefSB.26 (1375nm)
    // Emissive.20 (3750nm)
    // Emissive.28 (7325nm)
    // Emissive.29 (8550nm)
    // Emissive.31 (11030nm)
    // Emissive.32 (12020nm)

    public ModisMeasures() {
    }

    /**
     * heritage tests **
     */

    // heritage 1/7
    public static double heritageMeasureBT11(double bt11000, double tSkin) {
        if (bt11000 < MetImageConstants.UPPER_LIM_BT11000) {
            return convertModisEmissiveRadianceToTemperature(bt11000, 31) - tSkin;
        } else {
            return Double.NaN;
        }
    }

    // heritage 2/7
    public static double heritageMeasureSplitWindow(double bt11000, double bt12000) {
        if (bt11000 < MetImageConstants.UPPER_LIM_BT11000 && bt12000 < MetImageConstants.UPPER_LIM_BT12000) {
            return bt11000 - bt12000;
        } else {
            return Double.NaN;
        }
    }

    // heritage 3/7
    public static double heritageMeasureNegativeBT37minusBT11Night(double bt3700, double bt11000, boolean isNight) {
        if (isNight && bt3700 < MetImageConstants.UPPER_LIM_BT3700 && bt11000 < MetImageConstants.UPPER_LIM_BT11000) {
            return bt3700 - bt11000;
        } else {
            return Double.NaN;
        }
    }

    // heritage 4/7
    // same as 3/7, but use as 'broken cloud' criterion
    public static double heritageMeasurePositiveBT37minusBT11NightMixedScene(double bt3700, double bt11000, boolean isNight) {
        return heritageMeasureNegativeBT37minusBT11Night(bt3700, bt11000, isNight);
    }

    // heritage 5/7
    public static double heritageMeasureSolarBrightnessThresholdsOcean(double rho860, boolean isLand) {
        if (isLand || rho860 >= MetImageConstants.UPPER_LIM_RHO860) {
            return Double.NaN;
        } else {
            return rho860;
        }
    }

    // heritage 6/7
    public static double heritageMeasureSolarBrightnessThresholdsLand(double rho600, boolean isOcean) {
        if (isOcean || rho600 >= MetImageConstants.UPPER_LIM_RHO600) {
            return Double.NaN;
        } else {
            return rho600;
        }
    }

    // heritage 7/7
    public static double heritageMeasureUniformity(double bt11Sample3x3) {
        return bt11Sample3x3;
    }


    /**
     * new tests **
     */

    // new 1/7
    public static double newMeasureR138WaterVapour(double rho1380) {
        if (rho1380 > 0.0 && rho1380 < MetImageConstants.UPPER_LIM_RHO1380) {
            return rho1380;
        } else {
            return Double.NaN;
        }
    }

    // new 2/7
    public static double newMeasureBT11(double bt7300, double bt8600, double bt11000, boolean isLand) {
        if (isLand) {
            if (bt7300 < MetImageConstants.UPPER_LIM_BT7300 && bt8600 < MetImageConstants.UPPER_LIM_BT8600) {
                return bt7300 - bt8600;
            } else {
                return Double.NaN;
            }
        } else {
            if (bt7300 < MetImageConstants.UPPER_LIM_BT7300 && bt11000 < MetImageConstants.UPPER_LIM_BT11000) {
                return bt7300 - bt11000;
            } else {
                return Double.NaN;
            }
        }
    }

    // new 3/7
    // cannot be implemented within this project!!
    public static double newMeasureCO2() {
        return Double.NaN;
    }

    // new 4/7
    public static double newMeasureBT37minusBT87Deserts(double bt3700, double bt8600) {
        if (bt3700 < MetImageConstants.UPPER_LIM_BT3700 && bt8600 < MetImageConstants.UPPER_LIM_BT8600) {
            return bt3700 - bt8600;
        } else {
            return Double.NaN;
        }
    }

    // new 5/7
    public static double newMeasurePositiveBT37minusBT11Day06Glint(double bt3700, double bt11000, double rho600) {
        if (bt3700 < MetImageConstants.UPPER_LIM_BT3700 && bt11000 < MetImageConstants.UPPER_LIM_BT11000 &&
                rho600 < MetImageConstants.UPPER_LIM_RHO600) {
            return (bt3700 - bt11000) / rho600;
        } else {
            return Double.NaN;
        }
    }

    // new 6/7
    public static double newMeasureO2Absorption(int cloudHeightID) {
        // first guess: we don't have anything better than this
        return MetImageConstants.cloudHeights[cloudHeightID];
    }

    // new 7/7
    public static double newMeasureUniformityTwoChannels(double bt11Sample3x3, double diffBt11Bt37Sample3x3, double rho600, boolean isNight) {
        if (isNight) {
            return combine(bt11Sample3x3, diffBt11Bt37Sample3x3);
        } else {
            return combine(bt11Sample3x3, rho600);
        }
    }

    public static double convertModisEmissiveRadianceToTemperature(double radiance, int emissiveBandNumber) {

        final int wvlIndex = emissiveBandNumber - 20;
        final double c1 = 2.0 * MetImageConstants.PLANCK_CONSTANT *
                Math.pow(MetImageConstants.VACUUM_LIGHT_SPEED, 2.0);

        final double c2 = MetImageConstants.PLANCK_CONSTANT * MetImageConstants.VACUUM_LIGHT_SPEED /
                MetImageConstants.BOLTZMANN_CONSTANT;

        // use metres in units:
        final double wvlMetres = MetImageConstants.MODIS_EMISSIVE_WAVELENGTHS[wvlIndex] / 1.E9;  // input is in microns!
        final double radMetres = radiance * 1.E6;

        double temperature = c2 / (wvlMetres * Math.log(c1 / (radMetres * Math.pow(wvlMetres, 5.0)) + 1.0));
        temperature = (temperature - MetImageConstants.TCI[wvlIndex]) / MetImageConstants.TCS[wvlIndex];

        return temperature;
    }


    private static double combine(double A, double B) {
        return A * B; // todo: RP to define this measure
    }

}
