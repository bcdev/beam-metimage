package org.esa.beam.metimage.operator;

import java.util.ArrayList;
import java.util.List;

/**
 * Container holding a MODIS sample with an ID and arrays with 'cloud' and 'noCloud' samples
 *
 * @author Marco Zuehlke, Olaf Danne
 */
public class ModisSample {
    private int measureID;
    private double[] cloudSamples;
    private double[] noCloudSamples;
    private List<Double> cloudSampleList;
    private List<Double> noCloudSampleList;


    public ModisSample(int measureID) {
        this.measureID = measureID;
        cloudSampleList = new ArrayList<Double>();
        noCloudSampleList = new ArrayList<Double>();
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

    public List<Double> getCloudSampleList() {
        return cloudSampleList;
    }

    public List<Double> getNoCloudSampleList() {
        return noCloudSampleList;
    }
}
