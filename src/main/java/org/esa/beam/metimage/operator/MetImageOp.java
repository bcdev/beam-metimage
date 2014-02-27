package org.esa.beam.metimage.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductWriter;
import org.esa.beam.framework.datamodel.*;
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
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.math.IndexValidator;
import util.MetImageUtils;

import java.awt.*;
import java.io.*;
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

    @SourceProduct(alias = "tskin", description = "Tskin auxdata product")
    private Product tskinProduct;

    @TargetProduct
    private Product targetProduct;


    @Parameter(alias = "csv", description = "The target file for ASCII output.", notNull = true)
    File outputAsciiFile;

    @Parameter(description = "If set, 'cloud' and 'no cloud' histograms will be equalized",
               label = "Equalize histograms",
               defaultValue = "false")
    private boolean equalizeHistograms;

    @Parameter(description = "If set, use GETASSE30 DEM, otherwise get altitudes from product TPGs",
               label = "Use GETASSE30 DEM",
               defaultValue = "20")
    private int numberOfBins;

    private String daytime;
    private String nonCloudySurface;
    private String cloudtype;


    private int width;
    private int height;

    private Tile surfaceTypeTile;
    private Tile daytimeTile;
    private Tile cloudHeightTile;
    private Tile latitudeTile;
    private Tile longitudeTile;
    private Tile rho600Tile;
    private Tile rho860Tile;
    private Tile rho645Tile; // refSB1
    private Tile rho469Tile; // refSB3
    private Tile rho555Tile; // refSB4
    private Tile rho1240Tile;
    private Tile rho1380Tile;  // refSB5
    private Tile rho2130Tile;  // refSB7
    private Tile bt3700Tile;
    private Tile bt4050Tile;   // emiss23
    private Tile bt4515Tile;   // emiss25
    private Tile bt7300Tile;
    private Tile bt8600Tile;
    private Tile bt11000Tile;
    private Tile bt12000Tile;
    private Tile bt13000Tile;

    private Tile tskinTile;

    private Rectangle sampleRect;

    private double[][][] hCloudArray;
    private double[][][] hNoCloudArray;
    private double[][][] nCloudArray;
    private double[][][] nNoCloudArray;


    @Override
    public void initialize() throws OperatorException {
        width = sourceProduct.getSceneRasterWidth();
        height = sourceProduct.getSceneRasterHeight();

        try {
            targetProduct = createTargetProduct();
        } catch (IOException e) {
            throw new OperatorException("Cannot create target product: " + e.getMessage());
        }

        getSourceTiles();

        PrintStream csvOutputStream;
        try {
            csvOutputStream = new PrintStream(new FileOutputStream(outputAsciiFile));
            csvOutputStream.print("Daytime" + "\t" + "Non-cloudy Surface" + "\t" + "Cloud Type" + "\t");
            for (int i = 1; i <= MetImageConstants.NUM_TESTS; i++) {
                csvOutputStream.print("clouds_H" + i + "\t");
                csvOutputStream.print("noClouds_H" + i + "\t");
                csvOutputStream.print("Skill_H" + i + "\t");
            }
            for (int i = 1; i < MetImageConstants.NUM_TESTS; i++) {
                csvOutputStream.print("clouds_N" + i + "\t");
                csvOutputStream.print("noClouds_N" + i + "\t");
                csvOutputStream.print("Skill_N" + i + "\t");
            }
            csvOutputStream.print("clouds_N7" + "\t");
            csvOutputStream.print("noClouds_N7" + "\t");
            csvOutputStream.println("Skill_N7");
        } catch (FileNotFoundException e) {
            throw new OperatorException("Unable to write distinction skill ASCII file: " + e.getMessage());
        }

        for (int ii = 0; ii < MetImageConstants.DAYTIME_FILTER_ID.length; ii++) {
            daytime = MetImageConstants.DAYTIME_FILTER_ID[ii];
            for (int jj = 0; jj < MetImageConstants.SURFACE_FILTER_ID.length; jj++) {
                nonCloudySurface = MetImageConstants.SURFACE_FILTER_ID[jj];
                for (int kk = 0; kk < MetImageConstants.CLOUDTYPE_FILTER_ID.length; kk++) {
                    cloudtype = MetImageConstants.CLOUDTYPE_FILTER_ID[kk];

                    System.out.println("daytime, surface, cloudtype = " +
                                               daytime + ", " + nonCloudySurface + ", " + cloudtype);

                    hCloudArray = new double[MetImageConstants.NUM_TESTS][width][height];
                    hNoCloudArray = new double[MetImageConstants.NUM_TESTS][width][height];
                    nCloudArray = new double[MetImageConstants.NUM_TESTS][width][height];
                    nNoCloudArray = new double[MetImageConstants.NUM_TESTS][width][height];

                    for (int i = 0; i < MetImageConstants.NUM_TESTS; i++) {
                        hCloudArray[i] = new double[width][height];
                        hNoCloudArray[i] = new double[width][height];
                        nCloudArray[i] = new double[width][height];
                        nNoCloudArray[i] = new double[width][height];
                        for (int j = 0; j < width; j++) {
                            for (int k = 0; k < height; k++) {
                                hCloudArray[i][j][k] = Double.NaN;
                                hNoCloudArray[i][j][k] = Double.NaN;
                                nCloudArray[i][j][k] = Double.NaN;
                                nNoCloudArray[i][j][k] = Double.NaN;
                            }
                        }
                    }

                    ModisSample[] heritageSample = new ModisSample[MetImageConstants.NUM_TESTS];
                    ModisSample[] newSample = new ModisSample[MetImageConstants.NUM_TESTS];
                    double[] distSkillHeritage = new double[MetImageConstants.NUM_TESTS];
                    double[] distSkillNew = new double[MetImageConstants.NUM_TESTS];

                    for (int i = 0; i < MetImageConstants.NUM_TESTS; i++) {
                        heritageSample[i] = getModisSample(MetImageConstants.MEASURE_HERITAGE[i]);
                        distSkillHeritage[i] = getDistinctionSkill(heritageSample[i]);
                        newSample[i] = getModisSample(MetImageConstants.MEASURE_NEW[i]);
                        distSkillNew[i] = getDistinctionSkill(newSample[i]);
                    }

                    csvOutputStream.print(daytime + "\t" + nonCloudySurface + "\t" + cloudtype + "\t");
                    for (int i = 1; i <= MetImageConstants.NUM_TESTS; i++) {
                        String s = String.format("%d\t", heritageSample[i - 1].getCloudSamples().length);
                        csvOutputStream.print(s);
                        s = String.format("%d\t", heritageSample[i - 1].getNoCloudSamples().length);
                        csvOutputStream.print(s);
                        s = String.format("%6f\t", distSkillHeritage[i - 1]);
                        csvOutputStream.print(s);

                    }
                    for (int i = 1; i < MetImageConstants.NUM_TESTS; i++) {
                        String s = String.format("%d\t", newSample[i - 1].getCloudSamples().length);
                        csvOutputStream.print(s);
                        s = String.format("%d\t", newSample[i - 1].getNoCloudSamples().length);
                        csvOutputStream.print(s);
                        s = String.format("%6f\t", distSkillNew[i - 1]);
                        csvOutputStream.print(s);
                    }
                    String s = String.format("%d\t", newSample[MetImageConstants.NUM_TESTS - 1].getCloudSamples().length);
                    csvOutputStream.print(s);
                    s = String.format("%d\t", newSample[MetImageConstants.NUM_TESTS - 1].getNoCloudSamples().length);
                    csvOutputStream.print(s);
                    s = String.format("%6f", distSkillNew[MetImageConstants.NUM_TESTS - 1]);
                    csvOutputStream.println(s);
                }
            }
        }
        csvOutputStream.close();
    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        final Rectangle rectangle = targetTile.getRectangle();
        for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
            for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

                if (targetBand.getName().equals(MetImageConstants.MODIS_CSV_PRODUCT_DAYTIME_BAND_NAME)) {
                    targetTile.setSample(x, y, daytimeTile.getSampleInt(x, y));
                }
                if (targetBand.getName().equals(MetImageConstants.MODIS_CSV_PRODUCT_SURFACETYPE_BAND_NAME)) {
                    targetTile.setSample(x, y, surfaceTypeTile.getSampleInt(x, y));
                }
                if (targetBand.getName().equals(MetImageConstants.MODIS_CSV_PRODUCT_CLOUDHEIGHT_BAND_NAME)) {
                    targetTile.setSample(x, y, cloudHeightTile.getSampleInt(x, y));
                }

                for (int i = 1; i <= 7; i++) {
                    if (targetBand.getName().equals("H" + i + "_cloud")) {
                        targetTile.setSample(x, y, hCloudArray[i - 1][x][y]);
                    }
                    if (targetBand.getName().equals("H" + i + "_nocloud")) {
                        targetTile.setSample(x, y, hNoCloudArray[i - 1][x][y]);
                    }
                    if (targetBand.getName().equals("N" + i + "_cloud")) {
                        targetTile.setSample(x, y, nCloudArray[i - 1][x][y]);
                    }
                    if (targetBand.getName().equals("N" + i + "_nocloud")) {
                        targetTile.setSample(x, y, nNoCloudArray[i - 1][x][y]);
                    }
                }
            }
        }

    }

    private Product createTargetProduct() throws IOException {
        Product metimageProduct = new Product("METIMAGE", "METIMAGE", width, height);

        ProductUtils.copyGeoCoding(sourceProduct, metimageProduct);
        addTargetBands(metimageProduct);

        ProductWriter productWriter = ProductIO.getProductWriter("BEAM-DIMAP");
        productWriter.writeProductNodes(metimageProduct, "metimage.dim");

        return metimageProduct;
    }

    private void addTargetBands(Product product) {

        Band daytimeBand = new Band(MetImageConstants.MODIS_CSV_PRODUCT_DAYTIME_BAND_NAME, ProductData.TYPE_INT32, width, height);
        daytimeBand.setNoDataValue(Double.NaN);
        daytimeBand.setNoDataValueUsed(true);
        product.addBand(daytimeBand);

        Band surfaceBand = new Band(MetImageConstants.MODIS_CSV_PRODUCT_SURFACETYPE_BAND_NAME, ProductData.TYPE_INT32, width, height);
        surfaceBand.setNoDataValue(Double.NaN);
        surfaceBand.setNoDataValueUsed(true);
        product.addBand(surfaceBand);

        Band cloudtypeBand = new Band(MetImageConstants.MODIS_CSV_PRODUCT_CLOUDHEIGHT_BAND_NAME, ProductData.TYPE_INT32, width, height);
        cloudtypeBand.setNoDataValue(Double.NaN);
        cloudtypeBand.setNoDataValueUsed(true);
        product.addBand(cloudtypeBand);

        for (int i = 1; i <= MetImageConstants.NUM_TESTS; i++) {
            Band hCloudBand = new Band("H" + i + "_cloud", ProductData.TYPE_FLOAT64, width, height);
            hCloudBand.setNoDataValue(Double.NaN);
            hCloudBand.setNoDataValueUsed(true);
            product.addBand(hCloudBand);
            Band hNoCloudBand = new Band("H" + i + "_nocloud", ProductData.TYPE_FLOAT64, width, height);
            hNoCloudBand.setNoDataValue(Double.NaN);
            hNoCloudBand.setNoDataValueUsed(true);
            product.addBand(hNoCloudBand);
        }
        for (int i = 1; i <= MetImageConstants.NUM_TESTS; i++) {
            Band nCloudBand = new Band("N" + i + "_cloud", ProductData.TYPE_FLOAT64, width, height);
            nCloudBand.setNoDataValue(Double.NaN);
            nCloudBand.setNoDataValueUsed(true);
            product.addBand(nCloudBand);
            Band nNoCloudBand = new Band("N" + i + "_nocloud", ProductData.TYPE_FLOAT64, width, height);
            nNoCloudBand.setNoDataValue(Double.NaN);
            nNoCloudBand.setNoDataValueUsed(true);
            product.addBand(nNoCloudBand);
        }
    }

    private void getSourceTiles() {
        final Band daytimeBand = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_DAYTIME_BAND_NAME);
        final Band cloudHeightBand = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_CLOUDHEIGHT_BAND_NAME);
        final Band latitudeBand = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_LATITUDE_BAND_NAME);
        final Band longitudeBand = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_LONGITUDE_BAND_NAME);
        final Band surfaceTypeBand = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_SURFACETYPE_BAND_NAME);
        if (surfaceTypeBand == null) {
            throw new OperatorException("No cloud cover information available from input product - cannot proceed.");
        }
        final Band rho469Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO645_BAND_NAME);
        final Band rho645Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO469_BAND_NAME);
        final Band rho555Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO555_BAND_NAME);
        final Band rho1240Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO1240_BAND_NAME);
        final Band rho2130Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO2130_BAND_NAME);

        final Band rho600Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO600_BAND_NAME);
        final Band rho860Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO860_BAND_NAME);
        final Band rho1380Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_RHO1380_BAND_NAME);
        final Band bt3700Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT3700_BAND_NAME);
        final Band bt4050Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT4050_BAND_NAME);
        final Band bt4515Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT4515_BAND_NAME);
        final Band bt7300Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT7300_BAND_NAME);
        final Band bt8600Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT8600_BAND_NAME);
        final Band bt11000Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT11000_BAND_NAME);
        final Band bt12000Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT12000_BAND_NAME);
        final Band bt13000Band = sourceProduct.getBand(MetImageConstants.MODIS_CSV_PRODUCT_BT13000_BAND_NAME);

        sampleRect = new Rectangle(width, height);

        surfaceTypeTile = getSourceTile(surfaceTypeBand, sampleRect);
        daytimeTile = getSourceTile(daytimeBand, sampleRect);
        latitudeTile = getSourceTile(latitudeBand, sampleRect);
        longitudeTile = getSourceTile(longitudeBand, sampleRect);
        rho645Tile = getSourceTile(rho645Band, sampleRect);
        rho469Tile = getSourceTile(rho469Band, sampleRect);
        rho555Tile = getSourceTile(rho555Band, sampleRect);
        rho1240Tile = getSourceTile(rho1240Band, sampleRect);
        rho2130Tile = getSourceTile(rho2130Band, sampleRect);
        rho600Tile = getSourceTile(rho600Band, sampleRect);
        rho860Tile = getSourceTile(rho860Band, sampleRect);
        rho1380Tile = getSourceTile(rho1380Band, sampleRect);
        bt3700Tile = getSourceTile(bt3700Band, sampleRect);
        bt4050Tile = getSourceTile(bt4050Band, sampleRect);
        bt4515Tile = getSourceTile(bt4515Band, sampleRect);
        bt7300Tile = getSourceTile(bt7300Band, sampleRect);
        bt8600Tile = getSourceTile(bt8600Band, sampleRect);
        cloudHeightTile = getSourceTile(cloudHeightBand, sampleRect);
        bt11000Tile = getSourceTile(bt11000Band, sampleRect);
        bt12000Tile = getSourceTile(bt12000Band, sampleRect);
        bt13000Tile = getSourceTile(bt13000Band, sampleRect);

        String tskinBandName = "skt_time" + findTskinTimeIndex();
        final Band tskinBand = tskinProduct.getBand(tskinBandName);
        final int tskinWidth = tskinProduct.getSceneRasterWidth();
        final int tskinHeight = tskinProduct.getSceneRasterHeight();
        tskinTile = getSourceTile(tskinBand, new Rectangle(tskinWidth, tskinHeight));

    }

    private String findTskinTimeIndex() {
        // todo: define reasonable method to find correct day/night skin temperatures
        return "730";
    }

    private double getDistinctionSkill(ModisSample modisSample) {
        final double[] cloudSamples = modisSample.getCloudSamples();
        final int numCloud = cloudSamples.length;
        final double[] noCloudSamples = modisSample.getNoCloudSamples();
        final int numNoCloud = noCloudSamples.length;


        if (numCloud == 0 || numNoCloud == 0) {
            System.out.println("MeasureID '" + modisSample.getMeasureID() +
                                       "' : One or both cloud/noCloud sample arrays empty - cannot compute distinction skill.");
            return Double.NaN;
        }
        if (numCloud < MetImageConstants.MIN_SAMPLES_PER_HISTOGRAM ||
                numNoCloud < MetImageConstants.MIN_SAMPLES_PER_HISTOGRAM) {
            System.out.println("MeasureID '" + modisSample.getMeasureID() +
                                       "' : One or both cloud/noCloud sample arrays too small - cannot compute distinction skill.");
            return Double.NaN;
        }

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
                try {
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
                } catch (Exception e) {
                    System.out.println("Cannot perform equalization for measure ID '" + modisSample.getMeasureID() +
                                               "': " + e.getMessage());
                    System.out.println(" --> compute distinction skill without equalization.");
                    cloudHisto = MetImageHistogram.createAggregatedHistogram(cloudSamples, numberOfBins, min, max);
                    noCloudHisto = MetImageHistogram.createAggregatedHistogram(noCloudSamples, numberOfBins, min, max);
                }
            } else {
                System.out.println("Cannot perform equalization for measure ID '" + modisSample.getMeasureID() +
                                           "' (no valid 'optimal bins' found) - compute distinction skill without equalization.");
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

        List<Double> cloudSampleList = new ArrayList<>();
        List<Double> noCloudSampleList = new ArrayList<>();

        if (measureId == MetImageConstants.MEASURE_HERITAGE_7 || measureId == MetImageConstants.MEASURE_NEW_7) {
            int index = 0;
            SummaryStatistics rho600Stats = new SummaryStatistics();
            SummaryStatistics bt11000Stats = new SummaryStatistics();
            SummaryStatistics bt12000Minus3700Stats = new SummaryStatistics();
            for (int y = sampleRect.y; y < sampleRect.y + sampleRect.height; y++) {
                for (int x = sampleRect.x; x < sampleRect.x + sampleRect.width; x++) {
                    // take the standard deviation of measures over 3x3 pixels
                    if (measureId == MetImageConstants.MEASURE_HERITAGE_7) {
                        if (bt11000Tile.getSampleDouble(x, y) < MetImageConstants.UPPER_LIM_BT11000) {
                            final double bt11000Value =
                                    ModisMeasures.convertModisEmissiveRadianceToTemperature(bt11000Tile.getSampleDouble(x, y), 31);
                            bt11000Stats.addValue(bt11000Value);
                        }
                    } else {
                        final boolean rho600Valid =
                                isSampleNight(daytimeTile, x, y) || rho600Tile.getSampleDouble(x, y) < MetImageConstants.UPPER_LIM_RHO600;
                        if (rho600Valid &&
                                bt3700Tile.getSampleDouble(x, y) < MetImageConstants.UPPER_LIM_BT3700 &&
                                bt11000Tile.getSampleDouble(x, y) < MetImageConstants.UPPER_LIM_BT11000) {
                            final double rho600Value = rho600Tile.getSampleDouble(x, y);
                            if (!Double.isNaN(rho600Value)) {
                                rho600Stats.addValue(rho600Value);
                            }
                            final double bt11000Value =
                                    ModisMeasures.convertModisEmissiveRadianceToTemperature(bt11000Tile.getSampleDouble(x, y), 31);
                            if (!Double.isNaN(bt11000Value)) {
                                bt11000Stats.addValue(bt11000Value);
                            }

                            if (!Double.isNaN(bt12000Tile.getSampleDouble(x, y)) && !Double.isNaN(bt3700Tile.getSampleDouble(x, y))) {
                                final double bt12000Value =
                                        ModisMeasures.convertModisEmissiveRadianceToTemperature(bt12000Tile.getSampleDouble(x, y), 32);
                                final double bt3700Value =
                                        ModisMeasures.convertModisEmissiveRadianceToTemperature(bt3700Tile.getSampleDouble(x, y), 20);
                                bt12000Minus3700Stats.addValue(bt12000Value - bt3700Value);
                            }
                        }
                    }

                    if ((index + 1) % 9 == 0) {
                        double measure;
                        if (measureId == MetImageConstants.MEASURE_HERITAGE_7) {
                            measure = getMeasureHeritage7(bt11000Stats.getStandardDeviation());
                            fillMeasureOutputArray(hCloudArray[6], hNoCloudArray[6], y, x, measure);
                        } else {
                            measure = getMeasureNew7(bt11000Stats.getStandardDeviation(),
                                                     bt12000Minus3700Stats.getStandardDeviation(),
                                                     rho600Stats.getStandardDeviation(), y, x);
                            fillMeasureOutputArray(nCloudArray[6], nNoCloudArray[6], y, x, measure);
                        }
                        if (considerMeasure(measure, x, y)) {
                            if (isSampleCloud(surfaceTypeTile, x, y)) {
                                cloudSampleList.add(measure);
                            } else if (isSampleNoCloud(surfaceTypeTile, x, y)) {
                                noCloudSampleList.add(measure);
                            }
                        }

                        rho600Stats = new SummaryStatistics();
                        bt11000Stats = new SummaryStatistics();
                        bt12000Minus3700Stats = new SummaryStatistics();
                    }
                    index++;
                }
            }
        } else {
            int index = 0;
            for (int y = sampleRect.y; y < sampleRect.y + sampleRect.height; y++) {
                for (int x = sampleRect.x; x < sampleRect.x + sampleRect.width; x++) {
                    // take the center of the measures over 3x3 pixels
                    if ((index + 5) % 9 == 0) {
                        final double measure = getMeasureById(measureId, y, x);
                        if (considerMeasure(measure, x, y)) {
                            if (isSampleCloud(surfaceTypeTile, x, y)) {
                                cloudSampleList.add(measure);
                            } else if (isSampleNoCloud(surfaceTypeTile, x, y)) {
                                noCloudSampleList.add(measure);
                            }
                        }
                    }
                    index++;
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

    private boolean considerMeasure(double measure, int x, int y) {
        final boolean isNotNan = !Double.isNaN(measure);

        final boolean daytimeOK = daytime.equals("ALL") ||
                (daytime.equals("DAY") && isSampleDay(daytimeTile, x, y)) ||
                (daytime.equals("NIGHT") && isSampleNight(daytimeTile, x, y)) ||
                (daytime.equals("TWILIGHT") && isSampleTwilight(daytimeTile, x, y));

        final boolean surfaceOK = nonCloudySurface.equals("ALL") ||
                isSampleCloud(surfaceTypeTile, x, y) ||  // we have to let these pass
                (nonCloudySurface.equals("LAND") && isSampleLand(surfaceTypeTile, x, y)) ||
                (nonCloudySurface.equals("SEA") && isSampleOcean(surfaceTypeTile, x, y)) ||
                (nonCloudySurface.equals("ICE") && isSampleIce(surfaceTypeTile, x, y));

        final boolean cloudtypeOK = cloudtype.equals("ALL") ||
                !isSampleCloud(surfaceTypeTile, x, y) ||    // we have to let these pass
                (cloudtype.equals("LOW") && isSampleLowCloud(cloudHeightTile, x, y)) ||
                (cloudtype.equals("MIDLEVEL") && isSampleMidlevelCloud(cloudHeightTile, x, y)) ||
                (cloudtype.equals("HIGH") && isSampleHighCloud(cloudHeightTile, x, y) ||
                        (cloudtype.equals("SEMITRANSPARENT") && isSampleSemitransparentCloud(surfaceTypeTile, x, y)));


        return isNotNan && daytimeOK && surfaceOK && cloudtypeOK;
    }

    private double getMeasureById(int measureId, int y, int x) {
        double measure;
        switch (measureId) {
            case MetImageConstants.MEASURE_HERITAGE_1:
                measure = ModisMeasures.heritageMeasureBT11(bt11000Tile.getSampleDouble(x, y), getTskin(x, y));
                fillMeasureOutputArray(hCloudArray[0], hNoCloudArray[0], y, x, measure);
                break;
            case MetImageConstants.MEASURE_HERITAGE_2:
                measure = ModisMeasures.heritageMeasureSplitWindow(bt11000Tile.getSampleDouble(x, y),
                                                                   bt12000Tile.getSampleDouble(x, y));
                fillMeasureOutputArray(hCloudArray[1], hNoCloudArray[1], y, x, measure);
                break;
            case MetImageConstants.MEASURE_HERITAGE_3:
                measure = ModisMeasures.heritageMeasureNegativeBT37minusBT11Night(bt3700Tile.getSampleDouble(x, y),
                                                                                  bt11000Tile.getSampleDouble(x, y),
                                                                                  isSampleNight(daytimeTile, x, y));
                fillMeasureOutputArray(hCloudArray[2], hNoCloudArray[2], y, x, measure);
                break;
            case MetImageConstants.MEASURE_HERITAGE_4:
                measure = ModisMeasures.heritageMeasurePositiveBT37minusBT11NightMixedScene(bt3700Tile.getSampleDouble(x, y),
                                                                                            bt12000Tile.getSampleDouble(x, y),
                                                                                            isSampleNight(daytimeTile, x, y));
                fillMeasureOutputArray(hCloudArray[3], hNoCloudArray[3], y, x, measure);
                break;
            case MetImageConstants.MEASURE_HERITAGE_5:
                measure = ModisMeasures.heritageMeasureSolarBrightnessThresholdsOcean(rho860Tile.getSampleDouble(x, y),
                                                                                      isSampleLand(surfaceTypeTile, x, y));
                fillMeasureOutputArray(hCloudArray[4], hNoCloudArray[4], y, x, measure);
                break;
            case MetImageConstants.MEASURE_HERITAGE_6:
                measure = ModisMeasures.heritageMeasureSolarBrightnessThresholdsLand(rho600Tile.getSampleDouble(x, y),
                                                                                     isSampleOcean(surfaceTypeTile, x, y));
                fillMeasureOutputArray(hCloudArray[5], hNoCloudArray[5], y, x, measure);
                break;

            case MetImageConstants.MEASURE_NEW_1:
                measure = ModisMeasures.newMeasureR138WaterVapour(rho1380Tile.getSampleDouble(x, y));

//                measure = ModisMeasures.newMeasureRhoSB_3_5_7_add(rho469Tile.getSampleDouble(x, y),     // test MP
//                                                                  rho1240Tile.getSampleDouble(x, y),
//                                                                  rho2130Tile.getSampleDouble(x, y));

//                measure = ModisMeasures.newMeasureRhoSB_3_5_7_log_multiply(rho469Tile.getSampleDouble(x, y),     // test MP
//                                                                           rho1240Tile.getSampleDouble(x, y),
//                                                                           rho2130Tile.getSampleDouble(x, y),
//                                                                           isSampleDay(daytimeTile, x, y));

//                measure = ModisMeasures.newMeasureRhoSB_1_3_4(rho645Tile.getSampleDouble(x, y),     // test MP  2
//                                                              rho469Tile.getSampleDouble(x, y),
//                                                              rho555Tile.getSampleDouble(x, y));
//
//                measure = ModisMeasures.newMeasureLogEmiss_25_32_23(bt4515Tile.getSampleDouble(x, y),     // test MP  3
//                                                                    bt12000Tile.getSampleDouble(x, y),
//                                                                    bt4050Tile.getSampleDouble(x, y));

                fillMeasureOutputArray(nCloudArray[0], nNoCloudArray[0], y, x, measure);
                break;
            case MetImageConstants.MEASURE_NEW_2:
                measure = ModisMeasures.newMeasureBT11(bt3700Tile.getSampleDouble(x, y),
                                                       bt7300Tile.getSampleDouble(x, y),
                                                       bt8600Tile.getSampleDouble(x, y),
                                                       bt11000Tile.getSampleDouble(x, y),
                                                       isSampleLand(surfaceTypeTile, x, y), isSampleNight(daytimeTile, x, y));
                fillMeasureOutputArray(nCloudArray[1], nNoCloudArray[1], y, x, measure);
                break;
            case MetImageConstants.MEASURE_NEW_3:
                measure = ModisMeasures.newMeasureCO2(bt13000Tile.getSampleDouble(x, y),
                                                      bt11000Tile.getSampleDouble(x, y));
                fillMeasureOutputArray(nCloudArray[2], nNoCloudArray[2], y, x, measure);
                break;
            case MetImageConstants.MEASURE_NEW_4:
                measure = ModisMeasures.newMeasureBT37minusBT87Deserts(bt3700Tile.getSampleDouble(x, y),
                                                                       bt8600Tile.getSampleDouble(x, y), isSampleNight(daytimeTile, x, y));
                fillMeasureOutputArray(nCloudArray[3], nNoCloudArray[3], y, x, measure);
                break;
            case MetImageConstants.MEASURE_NEW_5:
                measure = ModisMeasures.newMeasurePositiveBT37minusBT11Day06Glint(bt3700Tile.getSampleDouble(x, y),
                                                                                  bt11000Tile.getSampleDouble(x, y),
                                                                                  rho600Tile.getSampleDouble(x, y));
                fillMeasureOutputArray(nCloudArray[4], nNoCloudArray[4], y, x, measure);
                break;
            case MetImageConstants.MEASURE_NEW_6:
                measure = ModisMeasures.newMeasureO2Absorption(cloudHeightTile.getSampleInt(x, y));
                fillMeasureOutputArray(nCloudArray[5], nNoCloudArray[5], y, x, measure);
                break;

            default:
                throw new OperatorException("invalid measure ID " + measureId + " - cannot proceed.");
        }
        return measure;
    }

    private void fillMeasureOutputArray(double[][] cloudArray, double[][] noCloudArray, int y, int x, double measure) {
        if (isSampleCloud(surfaceTypeTile, x, y)) {
            cloudArray[x][y] = measure;
        } else if (isSampleNoCloud(surfaceTypeTile, x, y)) {
            noCloudArray[x][y] = measure;
        }
    }

    private double getMeasureHeritage7(double bt11000Sample3x3) {
        return ModisMeasures.heritageMeasureUniformity(bt11000Sample3x3);
    }

    private double getMeasureNew7(double bt11000Sigma3x3, double diffBt12Bt3700Sigma3x3, double rho600Sigma3x3,
                                  int y, int x) {
        return ModisMeasures.newMeasureUniformityTwoChannels(bt11000Sigma3x3,
                                                             diffBt12Bt3700Sigma3x3,
                                                             rho600Sigma3x3,
                                                             isSampleNight(daytimeTile, x, y));
    }

    private double getTskin(int x, int y) {
        final float lat = latitudeTile.getSampleFloat(x, y);
        final float lon = longitudeTile.getSampleFloat(x, y);
        final GeoCoding tskinGeoCoding = tskinProduct.getGeoCoding();
        final PixelPos pixelPos = tskinGeoCoding.getPixelPos(new GeoPos(lat, lon), null);
        int px = (int) pixelPos.getX();
        px = Math.min(px, tskinProduct.getSceneRasterWidth() - 1);
        int py = (int) pixelPos.getY();
        py = Math.min(py, tskinProduct.getSceneRasterHeight() - 1);
        return tskinTile.getSampleDouble(px, py);
    }

    private boolean isSampleCloud(Tile surfaceTile, int x, int y) {
        return surfaceTile.getSampleInt(x, y) == 0 || isSampleSemitransparentCloud(surfaceTile, x, y);
    }

    private boolean isSampleSemitransparentCloud(Tile surfaceTile, int x, int y) {
        return surfaceTile.getSampleInt(x, y) == 1;
    }

    private boolean isSampleNoCloud(Tile surfaceTile, int x, int y) {
        return (surfaceTile.getSampleInt(x, y) >= 2 && surfaceTile.getSampleInt(x, y) <= 4);
    }

    private boolean isSampleOcean(Tile surfaceTile, int x, int y) {
        return surfaceTile.getSampleInt(x, y) == 2 || surfaceTile.getSampleInt(x, y) == 5;
    }

    private boolean isSampleLand(Tile surfaceTile, int x, int y) {
        return surfaceTile.getSampleInt(x, y) == 3 || surfaceTile.getSampleInt(x, y) == 6;
    }

    private boolean isSampleIce(Tile surfaceTile, int x, int y) {
        return surfaceTile.getSampleInt(x, y) == 4 || surfaceTile.getSampleInt(x, y) == 7;
    }

    private boolean isSampleDay(Tile daytimeTile, int x, int y) {
        return daytimeTile.getSampleInt(x, y) == 1;
    }

    private boolean isSampleNight(Tile daytimeTile, int x, int y) {
        return daytimeTile.getSampleInt(x, y) == 2;
    }

    private boolean isSampleTwilight(Tile daytimeTile, int x, int y) {
        return daytimeTile.getSampleInt(x, y) == 3;
    }

    private boolean isSampleLowCloud(Tile cloudHeightTile, int x, int y) {
        return cloudHeightTile.getSampleInt(x, y) == 1;
    }

    private boolean isSampleMidlevelCloud(Tile cloudHeightTile, int x, int y) {
        return cloudHeightTile.getSampleInt(x, y) == 2;
    }

    private boolean isSampleHighCloud(Tile cloudHeightTile, int x, int y) {
        return cloudHeightTile.getSampleInt(x, y) == 3;
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MetImageOp.class);
        }
    }
}
