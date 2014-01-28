package org.esa.beam.metimage.operator;

/**
 * Container holding a MODIS sample with an ID and arrays with 'cloud' and 'noCloud' samples
 *
 * @author Marco Zuehlke, Olaf Danne
 */
public class ModisSample {
    private int measureID;
    private double[] cloudSamples;
    private double[] noCloudSamples;

    public ModisSample(int measureID) {
        this.measureID = measureID;
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
}
