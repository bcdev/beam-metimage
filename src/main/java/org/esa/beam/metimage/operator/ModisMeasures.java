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


    /*** heritage tests ***/

    public double heritageMeasureBT11() {
        // heritage 1/7
        return bt11000 - tSkin;
    }

    public double heritageMeasureSplitWindow() {
        // heritage 2/7
        return bt11000 - bt12000;
    }

    public double heritageMeasureNegativeBT37minusBT11Night() {
        // heritage 3/7
        if (isNight) {
            return bt3700 - bt11000;
        } else {
            return Double.NaN;
        }
    }

    public double heritageMeasurePositiveBT37minusBT11NightMixedScene() {
        // heritage 4/7
        // same as 3/7, but use as 'broken cloud' criterion
        return heritageMeasureNegativeBT37minusBT11Night();
    }

    public double heritageMeasureSolarBrightnessThresholdsOcean() {
        // heritage 5/7
        if (isLand) {
            return Double.NaN;
        } else {
            return rho860;
        }
    }

    public double heritageMeasureSolarBrightnessThresholdsLand() {
        // heritage 6/7
        if (isLand) {
            return rho600;
        } else {
            return Double.NaN;
        }
    }

    public double heritageMeasureUniformity() {
        // heritage 7/7
        return bt11Sample3x3;
    }


    /*** new tests ***/

    public double newMeasureR138WaterVapour() {
        // new 1/7
        return rho1380;
    }

    public double newMeasureBT11() {
        // new 2/7
        if (isLand) {
            return bt7300 - bt8600;
        } else {
            return bt7300 - bt11000;
        }
    }

    public double newMeasureCO2() {
        // new 3/7
        // cannot be implemented within this project!!
        return Double.NaN;
    }

    public double newMeasureBT37minusBT87Deserts() {
        // new 4/7
        return bt3700 - bt8600;
    }

    public double newMeasurePositiveBT37minusBT11Day06Glint() {
        // new 5/7
        return (bt3700 - bt11000) / rho600;
    }

    public double newMeasureO2Absorption() {
        // new 6/7
        // todo: take Idepix scaling height
        return 0.0;
    }

    public double newMeasureUniformityTwoChannels() {
        // new 7/7
        if (isNight) {
            return combine(bt11Sample3x3, diffBt11Bt37Sample3x3);
        } else {
            return combine(bt11Sample3x3, rho600);
        }
    }


    /**
     * setters **
     */

    public void setIsLand(boolean isLand) {
        this.isLand = isLand;
    }

    public void setIsNight(boolean isNight) {
        this.isNight = isNight;
    }

    public void setBt11000(double bt11000) {
        this.bt11000 = bt11000;
    }

    public void setBt12000(double bt12000) {
        this.bt12000 = bt12000;
    }

    public void setBt3700(double bt3700) {
        this.bt3700 = bt3700;
    }

    public void setBt7300(double bt7300) {
        this.bt7300 = bt7300;
    }

    public void setBt8600(double bt8600) {
        this.bt8600 = bt8600;
    }

    public void setTSkin(double tSkin) {
        this.tSkin = tSkin;
    }

    public void setRho860(double rho860) {
        this.rho860 = rho860;
    }

    public void setRho600(double rho600) {
        this.rho600 = rho600;
    }

    public void setBt11Sample3x3(double bt11Sample3x3) {
        this.bt11Sample3x3 = bt11Sample3x3;
    }

    public void setRho1380(double rho1380) {
        this.rho1380 = rho1380;
    }

    public void setDiffBt11Bt37Sample3x3(double diffBt11Bt37Sample3x3) {
        this.diffBt11Bt37Sample3x3 = diffBt11Bt37Sample3x3;
    }


    private static double combine(double A, double B) {
        return A * B; // todo: define this measure
    }

}
