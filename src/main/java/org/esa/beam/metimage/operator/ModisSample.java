package org.esa.beam.metimage.operator;

/**
 * Container holding a MODIS sample with an ID and arrays with 'cloud' and 'noCloud' samples
 *
 * @author Marco Zuehlke, Olaf Danne
 */
public class ModisSample {
    private int measureID;
    private String measureName;
    private double[] cloudSamples;
    private double[] noCloudSamples;


    public ModisSample(int measureID) throws IllegalArgumentException {
        this.measureID = measureID;
        setMeasureName();
    }

    private void setMeasureName() throws IllegalArgumentException {
        if (measureID > 0 && measureID <= 7) {
            measureName = "H" + measureID;
        } else if (measureID > 7 && measureID <= 14) {
            measureName = "N" + (measureID - 7);
        } else {
            throw new IllegalArgumentException("measureId: " + measureID);
        }
    }

    public int getMeasureID() {
        return measureID;
    }

    public double[] getCloudSamples() {
        return cloudSamples;
    }

    public void setCloudSamples(double[] cloudSamples) {
        this.cloudSamples = cloudSamples;
    }

    public double[] getNoCloudSamples() {
        return noCloudSamples;
    }

    public void setNoCloudSamples(double[] noCloudSamples) {
        this.noCloudSamples = noCloudSamples;
    }

    public String getMeasureName() {
        return measureName;
    }
}
