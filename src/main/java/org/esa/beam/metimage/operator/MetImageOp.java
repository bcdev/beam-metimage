package org.esa.beam.metimage.operator;

import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.metimage.MetImageConstants;
import org.esa.beam.metimage.math.DistinctionSkill;
import org.esa.beam.metimage.math.MetImageHistogram;
import org.esa.beam.util.math.IndexValidator;
import util.MetImageUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Operator for MERIS atmospheric correction with SCAPE-M algorithm.
 *
 * @author Marco Zuehlke, Olaf Danne
 */
@OperatorMetadata(alias = "beam.metimage", version = "1.0-SNAPSHOT",
                  authors = "Rene Preusker, Olaf Danne, Marco Zuehlke",
                  copyright = "(c) 2013/14 FU Berlin, Brockmann Consult",
                  description = "Operator for MetImage Cloud processing.")
public class MetImageOp extends Operator {
    public static final String VERSION = "1.0-SNAPSHOT";

    @SourceProduct(alias = "MODIS_L1b", description = "MODIS L1B  CSV product")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;


    @Parameter(description = "If set, 'cloud' and 'no cloud' histograms will be equalized",
               label = "Equalize histograms",
               defaultValue = "false")
    private boolean equalizeHistograms;

    @Parameter(description = "If set, use GETASSE30 DEM, otherwise get altitudes from product TPGs",
               label = "Use GETASSE30 DEM",
               defaultValue = "20")
    private int numberOfBins;


    private int width;
    private int height;

    private Band tskinBand;   // todo: check how to introduce this here

    private Tile surfaceTypeTile;
    private Tile daytimeTile;
    private Tile rho600Tile;
    private Tile rho860Tile;
    private Tile rho1380Tile;
    private Tile bt3700Tile;
    private Tile bt7300Tile;
    private Tile bt8600Tile;
    private Tile bt11000Tile;
    private Tile bt12000Tile;
    private Rectangle sampleRect;


    @Override
    public void initialize() throws OperatorException {

        width = sourceProduct.getSceneRasterWidth();
        height = sourceProduct.getSceneRasterHeight();

        getSourceTiles();

        ModisSample heritage1Sample = getModisSample(MetImageConstants.MEASURE_HERITAGE_1);
        ModisSample heritage2Sample = getModisSample(MetImageConstants.MEASURE_HERITAGE_2);
        ModisSample heritage3Sample = getModisSample(MetImageConstants.MEASURE_HERITAGE_3);
        ModisSample heritage4Sample = getModisSample(MetImageConstants.MEASURE_HERITAGE_4);
        ModisSample heritage5Sample = getModisSample(MetImageConstants.MEASURE_HERITAGE_5);
        ModisSample heritage6Sample = getModisSample(MetImageConstants.MEASURE_HERITAGE_6);
        ModisSample heritage7Sample = getModisSample(MetImageConstants.MEASURE_HERITAGE_7);

        ModisSample new1Sample = getModisSample(MetImageConstants.MEASURE_NEW_1);
        ModisSample new2Sample = getModisSample(MetImageConstants.MEASURE_NEW_2);
        ModisSample new3Sample = getModisSample(MetImageConstants.MEASURE_NEW_3);
        ModisSample new4Sample = getModisSample(MetImageConstants.MEASURE_NEW_4);
        ModisSample new5Sample = getModisSample(MetImageConstants.MEASURE_NEW_5);
        ModisSample new6Sample = getModisSample(MetImageConstants.MEASURE_NEW_6);
        ModisSample new7Sample = getModisSample(MetImageConstants.MEASURE_NEW_7);

        final double distSkillHeritage1 = getDistinctionSkill(heritage1Sample);
        final double distSkillHeritage2 = getDistinctionSkill(heritage2Sample);
        final double distSkillHeritage3 = getDistinctionSkill(heritage3Sample);
        final double distSkillHeritage4 = getDistinctionSkill(heritage4Sample);
        final double distSkillHeritage5 = getDistinctionSkill(heritage5Sample);
        final double distSkillHeritage6 = getDistinctionSkill(heritage6Sample);
        final double distSkillHeritage7 = getDistinctionSkill(heritage7Sample);

        final double distSkillNew1 = getDistinctionSkill(new1Sample);
        final double distSkillNew2 = getDistinctionSkill(new2Sample);
        final double distSkillNew3 = getDistinctionSkill(new3Sample);
        final double distSkillNew4 = getDistinctionSkill(new4Sample);
        final double distSkillNew5 = getDistinctionSkill(new5Sample);
        final double distSkillNew6 = getDistinctionSkill(new6Sample);
        final double distSkillNew7 = getDistinctionSkill(new7Sample);

        // todo: define a format for output product
        System.out.println("distSkillHeritage1 = " + distSkillHeritage1);
        System.out.println("distSkillHeritage2 = " + distSkillHeritage2);
        System.out.println("distSkillHeritage3 = " + distSkillHeritage3);
        System.out.println("distSkillHeritage4 = " + distSkillHeritage4);
        System.out.println("distSkillHeritage5 = " + distSkillHeritage5);
        System.out.println("distSkillHeritage6 = " + distSkillHeritage6);
        System.out.println("distSkillHeritage7 = " + distSkillHeritage7);

        System.out.println("distSkillNew1 = " + distSkillNew1);
        System.out.println("distSkillNew2 = " + distSkillNew2);
        System.out.println("distSkillNew3 = " + distSkillNew3);
        System.out.println("distSkillNew4 = " + distSkillNew4);
        System.out.println("distSkillNew5 = " + distSkillNew5);
        System.out.println("distSkillNew6 = " + distSkillNew6);
        System.out.println("distSkillNew7 = " + distSkillNew7);

        setTargetProduct(new Product("dummy", "dummy", 0, 0));
    }

    private void getSourceTiles() {
        final Band daytimeBand = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_DAYTIME_BAND_NAME);
        final Band surfaceTypeBand = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_CLOUD_BAND_NAME);
        if (surfaceTypeBand == null) {
            throw new OperatorException("No cloud cover information available from input product - cannot proceed.");
        }
        final Band rho600Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO600_BAND_NAME);
        final Band rho860Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO860_BAND_NAME);
        final Band rho1380Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO1380_BAND_NAME);
        final Band bt3700Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT3700_BAND_NAME);
        final Band bt7300Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT7300_BAND_NAME);
        final Band bt8600Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT8600_BAND_NAME);
        final Band bt11000Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT11000_BAND_NAME);
        final Band bt12000Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT12000_BAND_NAME);

        sampleRect = new Rectangle(width, height);
        surfaceTypeTile = getSourceTile(surfaceTypeBand, sampleRect);
        daytimeTile = getSourceTile(daytimeBand, sampleRect);
        rho600Tile = getSourceTile(rho600Band, sampleRect);
        rho860Tile = getSourceTile(rho860Band, sampleRect);
        rho1380Tile = getSourceTile(rho1380Band, sampleRect);
        bt3700Tile = getSourceTile(bt3700Band, sampleRect);
        bt7300Tile = getSourceTile(bt7300Band, sampleRect);
        bt8600Tile = getSourceTile(bt8600Band, sampleRect);
        bt11000Tile = getSourceTile(bt11000Band, sampleRect);
        bt12000Tile = getSourceTile(bt12000Band, sampleRect);
    }

    private double getDistinctionSkill(ModisSample modisSample) {
        final double[] cloudSamples = modisSample.getCloudSamples();
        final int numCloud = cloudSamples.length;
        final double[] noCloudSamples = modisSample.getNoCloudSamples();
        final int numNoCloud = noCloudSamples.length;

        final double cloudSampleMax = (new Max()).evaluate(cloudSamples);
        final double cloudSampleMin = (new Min()).evaluate(cloudSamples);

        final double noCloudSampleMax = (new Max()).evaluate(noCloudSamples);
        final double noCloudSampleMin = (new Min()).evaluate(noCloudSamples);

        final double min = Math.min(cloudSampleMin, noCloudSampleMin);
        final double max = Math.max(cloudSampleMax, noCloudSampleMax);

        MetImageHistogram cloudHisto;
        MetImageHistogram noCloudHisto;
        double distSkill;

        if (equalizeHistograms) {
            final double[] allSamples = mergeSamples(cloudSamples, numCloud, noCloudSamples, numNoCloud);
            final int nBins = MetImageHistogram.findOptimalNumberOfBins(allSamples);
            if (nBins > 0) {
                final MetImageHistogram allSamplesEqualizedHisto = MetImageHistogram.createAggregatedEqualizedHistogram(allSamples);
                final double[] unequalBins = allSamplesEqualizedHisto.getUnequalBinBorders();

                // now get the unequal spaced cloud and noCloudhistograms...
                cloudHisto = new MetImageHistogram(new int[unequalBins.length - 1], min, max, MetImageConstants.ALPHA);
                cloudHisto.setUnequalBinBorders(unequalBins);
                cloudHisto.aggregateUnequalBins(cloudSamples, IndexValidator.TRUE);
                cloudHisto.computeDensityFunctions();
                noCloudHisto = new MetImageHistogram(new int[unequalBins.length - 1], min, max, MetImageConstants.ALPHA);
                noCloudHisto.setUnequalBinBorders(unequalBins);
                noCloudHisto.aggregateUnequalBins(noCloudSamples, IndexValidator.TRUE);
                noCloudHisto.computeDensityFunctions();
            } else {
                System.out.println("Cannot perform equalization for measure ID '" + modisSample.getMeasureID() +
                                           "' (no valid bins found) - compute distinction skill without equalization.");
                cloudHisto = MetImageHistogram.createAggregatedHistogram(cloudSamples, numberOfBins, min, max);
                noCloudHisto = MetImageHistogram.createAggregatedHistogram(noCloudSamples, numberOfBins, min, max);
            }
        } else {
            cloudHisto = MetImageHistogram.createAggregatedHistogram(cloudSamples, numberOfBins, min, max);
            noCloudHisto = MetImageHistogram.createAggregatedHistogram(noCloudSamples, numberOfBins, min, max);
        }
        distSkill = DistinctionSkill.computeDistinctionSkillFromCramerMisesAndersonMetric(noCloudHisto,
                                                                                          cloudHisto,
                                                                                          numNoCloud,
                                                                                          numCloud);

        return distSkill;
    }

    private double[] mergeSamples(double[] cloudSamples, int numCloud, double[] noCloudSamples, int numNoCloud) {
        double[] allSamples = new double[numCloud + numNoCloud];
        System.arraycopy(cloudSamples, 0, allSamples, 0, numCloud);
        System.arraycopy(noCloudSamples, 0, allSamples, numCloud, numNoCloud);
        return allSamples;
    }

    // provides an object holding cloudy and non-cloudy arrays of a measure with given ID
    private ModisSample getModisSample(int measureId) {
        ModisSample sample = new ModisSample(measureId);

        List<Double> cloudSampleList = new ArrayList<Double>();
        List<Double> noCloudSampleList = new ArrayList<Double>();

        for (int y = sampleRect.y; y < sampleRect.y + sampleRect.height; y++) {
            for (int x = sampleRect.x; x < sampleRect.x + sampleRect.width; x++) {
                final double measure = getMeasureById(measureId, y, x);
                if (isSampleCloudy(surfaceTypeTile, x, y)) {
                    cloudSampleList.add(measure);
                } else {
                    noCloudSampleList.add(measure);
                }
            }
        }

        final double[] cloudSampleArray =
                MetImageUtils.getAsPrimitiveDoubles(cloudSampleList.toArray(new Double[cloudSampleList.size()]));
        final double[] noCloudSampleArray =
                MetImageUtils.getAsPrimitiveDoubles(noCloudSampleList.toArray(new Double[noCloudSampleList.size()]));

        sample.setCloudSamples(cloudSampleArray);
        sample.setNoCloudSamples(noCloudSampleArray);

        return sample;
    }

    private double getMeasureById(int measureId, int y, int x) {
        double measure;
        switch (measureId) {
            case MetImageConstants.MEASURE_HERITAGE_1:
                measure = ModisMeasures.heritageMeasureBT11(bt11000Tile.getSampleDouble(x, y), 0.0); // todo: get Tskin!
                break;
            case MetImageConstants.MEASURE_HERITAGE_2:
                measure = ModisMeasures.heritageMeasureSplitWindow(bt11000Tile.getSampleDouble(x, y),
                                                                   bt12000Tile.getSampleDouble(x, y));
                break;
            case MetImageConstants.MEASURE_HERITAGE_3:
                measure = ModisMeasures.heritageMeasureNegativeBT37minusBT11Night(bt3700Tile.getSampleDouble(x, y),
                                                                                  bt11000Tile.getSampleDouble(x, y),
                                                                                  isSampleNight(daytimeTile, x, y));
                break;
            case MetImageConstants.MEASURE_HERITAGE_4:
                measure = ModisMeasures.heritageMeasurePositiveBT37minusBT11NightMixedScene(bt3700Tile.getSampleDouble(x, y),
                                                                                            bt11000Tile.getSampleDouble(x, y),
                                                                                            isSampleNight(daytimeTile, x, y));
                break;
            case MetImageConstants.MEASURE_HERITAGE_5:
                measure = ModisMeasures.heritageMeasureSolarBrightnessThresholdsOcean(rho860Tile.getSampleDouble(x, y),
                                                                                      isSampleLand(surfaceTypeTile, x, y));
                break;
            case MetImageConstants.MEASURE_HERITAGE_6:
                measure = ModisMeasures.heritageMeasureSolarBrightnessThresholdsLand(rho600Tile.getSampleDouble(x, y),
                                                                                     isSampleLand(surfaceTypeTile, x, y));
                break;
            case MetImageConstants.MEASURE_HERITAGE_7:
                measure = ModisMeasures.heritageMeasureUniformity(Double.NaN);     // todo!
                break;
            case MetImageConstants.MEASURE_NEW_1:
                measure = ModisMeasures.newMeasureR138WaterVapour(rho1380Tile.getSampleDouble(x, y));
                break;
            case MetImageConstants.MEASURE_NEW_2:
                measure = ModisMeasures.newMeasureBT11(bt7300Tile.getSampleDouble(x, y),
                                                       bt8600Tile.getSampleDouble(x, y),
                                                       bt11000Tile.getSampleDouble(x, y),
                                                       isSampleLand(surfaceTypeTile, x, y));
                break;
            case MetImageConstants.MEASURE_NEW_3:
                measure = ModisMeasures.newMeasureCO2();
                break;
            case MetImageConstants.MEASURE_NEW_4:
                measure = ModisMeasures.newMeasureBT37minusBT87Deserts(bt3700Tile.getSampleDouble(x, y),
                                                                       bt8600Tile.getSampleDouble(x, y));
                break;
            case MetImageConstants.MEASURE_NEW_5:
                measure = ModisMeasures.newMeasurePositiveBT37minusBT11Day06Glint(bt3700Tile.getSampleDouble(x, y),
                                                                                  bt11000Tile.getSampleDouble(x, y),
                                                                                  rho600Tile.getSampleDouble(x, y));
                break;
            case MetImageConstants.MEASURE_NEW_6:
                measure = ModisMeasures.newMeasureO2Absorption(Double.NaN);  // todo !
                break;
            case MetImageConstants.MEASURE_NEW_7:
                measure = ModisMeasures.newMeasureUniformityTwoChannels(Double.NaN, Double.NaN,        // todo !
                                                                        rho600Tile.getSampleDouble(x, y),
                                                                        isSampleNight(daytimeTile, x, y));
                break;
            default:
                throw new OperatorException("invalid measure ID " + measureId + " - cannot proceed.");
        }
        return measure;
    }

    private boolean isSampleCloudy(Tile surfaceTile, int x, int y) {
        return surfaceTile.getSampleInt(x, y) == 0;
    }

    private boolean isSampleLand(Tile surfaceTile, int x, int y) {
        return surfaceTile.getSampleInt(x, y) == 2 || surfaceTile.getSampleInt(x, y) == 5;
    }

    private boolean isSampleNight(Tile daytimeTile, int x, int y) {
        return daytimeTile.getSampleInt(x, y) == 2;
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(org.esa.beam.metimage.operator.MetImageOp.class);
        }
    }
}
