package org.esa.beam.metimage.operator;

/**
 * MODIS measures for cloud tests as proposed by R.Preusker, FUB
 *
 * @author M. Zuehlke, O. Danne
 */
public class ModisMeasures {

    private boolean isLand;
    private boolean isNight;

    private double rho600;     // RefSB.13 (667nm) // todo: take lo or hi ?
    private double rho860;     // RefSB.16 (869.5nm)
    private double rho1380;    // RefSB.26 (1375nm)
    private double bt3700;     // Emissive.20 (3750nm)
    private double bt7300;     // Emissive.28 (7325nm)
    private double bt8600;     // Emissive.29 (8550nm)
    private double bt11000;     // Emissive.31 (11030nm)
    private double bt12000;     // Emissive.32 (12020nm)
    private double tSkin;
    private double bt11Sample3x3;
    private double diffBt11Bt37Sample3x3;

    public ModisMeasures() {
    }

    public ModisMeasures(boolean isLand, boolean isNight,
                         double rho600, double rho860, double rho1380, double bt3700,
                         double bt7300, double bt8600, double bt11000, double bt12000,
                         double tSkin, double bt11Sample3x3, double diffBt11Bt37Sample3x3) {
        this.isLand = isLand;
        this.isNight = isNight;
        this.rho600 = rho600;
        this.rho860 = rho860;
        this.rho1380 = rho1380;
        this.bt3700 = bt3700;
        this.bt7300 = bt7300;
        this.bt8600 = bt8600;
        this.bt11000 = bt11000;
        this.bt12000 = bt12000;
        this.tSkin = tSkin;
        this.bt11Sample3x3 = bt11Sample3x3;
        this.diffBt11Bt37Sample3x3 = diffBt11Bt37Sample3x3;
    }

    /**
     * heritage tests **
     */

    // heritage 1/7
    public double heritageMeasureBT11() {
        return convertToBrightnessTemperature(bt11000) - tSkin;
    }
    public static double heritageMeasureBT11(double bt11000, double tSkin) {
        return convertToBrightnessTemperature(bt11000) - tSkin;
    }

    // heritage 2/7
    public double heritageMeasureSplitWindow() {
        return bt11000 - bt12000;
    }
    public static double heritageMeasureSplitWindow(double bt11000, double bt12000) {
        return bt11000 - bt12000;
    }

    // heritage 3/7
    public double heritageMeasureNegativeBT37minusBT11Night() {
        if (isNight) {
            return bt3700 - bt11000;
        } else {
            return Double.NaN;
        }
    }
    public static double heritageMeasureNegativeBT37minusBT11Night(double bt3700, double bt11000, boolean isNight) {
        if (isNight) {
            return bt3700 - bt11000;
        } else {
            return Double.NaN;
        }
    }

    // heritage 4/7
    // same as 3/7, but use as 'broken cloud' criterion
    public double heritageMeasurePositiveBT37minusBT11NightMixedScene() {
        return heritageMeasureNegativeBT37minusBT11Night();
    }
    public static double heritageMeasurePositiveBT37minusBT11NightMixedScene(double bt3700, double bt11000, boolean isNight) {
        return heritageMeasureNegativeBT37minusBT11Night(bt3700, bt11000, isNight);
    }

    // heritage 5/7
    public double heritageMeasureSolarBrightnessThresholdsOcean() {
        if (isLand) {
            return Double.NaN;
        } else {
            return rho860;
        }
    }
    public static double heritageMeasureSolarBrightnessThresholdsOcean(double rho860, boolean isLand) {
        if (isLand) {
            return Double.NaN;
        } else {
            return rho860;
        }
    }

    // heritage 6/7
    public double heritageMeasureSolarBrightnessThresholdsLand() {
        if (isLand) {
            return rho600;
        } else {
            return Double.NaN;
        }
    }
    public static double heritageMeasureSolarBrightnessThresholdsLand(double rho600, boolean isLand) {
        if (isLand) {
            return rho600;
        } else {
            return Double.NaN;
        }
    }

    // heritage 7/7
    public double heritageMeasureUniformity() {
        return bt11Sample3x3;
    }
    public static double heritageMeasureUniformity(double bt11Sample3x3) {
        return bt11Sample3x3;
    }


    /**
     * new tests **
     */

    // new 1/7
    public double newMeasureR138WaterVapour() {
        return rho1380;
    }
    public static double newMeasureR138WaterVapour(double rho1380) {
        return rho1380;
    }

    // new 2/7
    public double newMeasureBT11() {
        if (isLand) {
            return bt7300 - bt8600;
        } else {
            return bt7300 - bt11000;
        }
    }
    public static double newMeasureBT11(double bt7300, double bt8600, double bt11000, boolean isLand) {
        if (isLand) {
            return bt7300 - bt8600;
        } else {
            return bt7300 - bt11000;
        }
    }

    // new 3/7
    // cannot be implemented within this project!!
    public static double newMeasureCO2() {
        return Double.NaN;
    }

    // new 4/7
    public double newMeasureBT37minusBT87Deserts() {
        return bt3700 - bt8600;
    }
    public static double newMeasureBT37minusBT87Deserts(double bt3700, double bt8600) {
        return bt3700 - bt8600;
    }

    // new 5/7
    public double newMeasurePositiveBT37minusBT11Day06Glint() {
        return (bt3700 - bt11000) / rho600;
    }
    public static double newMeasurePositiveBT37minusBT11Day06Glint(double bt3700, double bt11000, double rho600) {
        return (bt3700 - bt11000) / rho600;
    }

    // new 6/7
    // todo: take Idepix scaling height
    public double newMeasureO2Absorption() {
        return 0.0;
    }
    public static double newMeasureO2Absorption(double scalingHeight) {
        return scalingHeight;
    }

    // new 7/7
    public double newMeasureUniformityTwoChannels() {
        if (isNight) {
            return combine(bt11Sample3x3, diffBt11Bt37Sample3x3);
        } else {
            return combine(bt11Sample3x3, rho600);
        }
    }
    public static double newMeasureUniformityTwoChannels(double bt11Sample3x3, double diffBt11Bt37Sample3x3, double rho600, boolean isNight) {
        if (isNight) {
            return combine(bt11Sample3x3, diffBt11Bt37Sample3x3);
        } else {
            return combine(bt11Sample3x3, rho600);
        }
    }


    private static double convertToBrightnessTemperature(double emissiveValue) {
        return 288.0; // todo implement
    }

    private static double combine(double A, double B) {
        return A * B; // todo: define this measure
    }

}
