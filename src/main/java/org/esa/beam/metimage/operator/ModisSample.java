package org.esa.beam.metimage.operator;

import util.MetImageUtils;

/**
 * todo: add comment
 * To change this template use File | Settings | File Templates.
 * Date: 24.01.14
 * Time: 15:51
 *
 * @author olafd
 */
public class ModisSample {
    private int measureID;
    private String bandName;
    private double[] cloudSamples;
    private double[] noCloudSamples;

    public ModisSample(int measureID) {
        this.measureID = measureID;
    }

    public int getMeasureID() {
        return measureID;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
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
