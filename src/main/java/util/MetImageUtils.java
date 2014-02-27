package util;

import org.esa.beam.metimage.math.MetImageHistogram;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * MetImage utility class
 *
 * @author M. Zuehlke, O. Danne
 */
public class MetImageUtils {

    public static double getCumulativeSum(double[] array, int numElemsToSum) {
        double sum = 0.0;
        final int end = Math.min(array.length, numElemsToSum);
        for (int i = 0; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static double[] getCumulativeSumAndNormalize(double[] cumArray) {
        double[] normArray = new double[cumArray.length + 1];
        final int nLength = normArray.length;
        normArray[nLength - 1] = MetImageUtils.getCumulativeSum(cumArray, nLength - 1);
        for (int i = 0; i < nLength; i++) {
            final double cumulativeSum = MetImageUtils.getCumulativeSum(cumArray, i);
            normArray[i] = cumulativeSum / normArray[nLength - 1];
        }
        return normArray;
    }


    public static double[] getAsPrimitiveDoubles(float[] fArr) {
        double[] dArr = new double[fArr.length];
        for (int i = 0; i < dArr.length; i++) {
            dArr[i] = (double) fArr[i];
        }
        return dArr;
    }

    public static double[] getAsPrimitiveDoubles(Double[] dArr) {
        double[] dPrimitiveArr = new double[dArr.length];
        for (int i = 0; i < dPrimitiveArr.length; i++) {
            dPrimitiveArr[i] = dArr[i];
        }
        return dPrimitiveArr;
    }

    public static Double[] getAsDoubles(float[] fArr) {
        Double[] dArr = new Double[fArr.length];
        for (int i = 0; i < fArr.length; i++) {
            dArr[i] = new Double(fArr[i]);
        }
        return dArr;
    }

    public static Double[] getAsDoubles(double[] srcArr) {
        Double[] dArr = new Double[srcArr.length];
        for (int i = 0; i < srcArr.length; i++) {
            dArr[i] = new Double(srcArr[i]);
        }
        return dArr;
    }

    public static Double[] getAsDoublesF6(float[] fArr) {
        Double[] dArr = new Double[fArr.length];
        for (int i = 0; i < fArr.length; i++) {
            final String s = String.format("%6f\t", fArr[i]);
            dArr[i] = new Double(s);
        }
        return dArr;
    }

    public static Integer[] getAsIntegers(int[] srcArr) {
        Integer[] dArr = new Integer[srcArr.length];
        for (int i = 0; i < srcArr.length; i++) {
            dArr[i] = new Integer(srcArr[i]);
        }
        return dArr;
    }


    public static void writeHistogramsAsJson(String jsonDir,
                                             String daytimeString,
                                             String surfaceString,
                                             String cloudTypeString,
                                             String testId,
                                             MetImageHistogram cloudHisto,
                                             MetImageHistogram noCloudHisto) {

        // todo: test this method!

        JSONObject histoObj = new JSONObject();

        JSONArray binBordersJsonArray = new JSONArray();
        JSONArray noCloudHistoJsonArray = new JSONArray();
        JSONArray cloudHistoJsonArray = new JSONArray();
        final Double[] binBordersArray = getAsDoublesF6(cloudHisto.getEqualBinBorders());
        final Integer[] noCloudHistoArray = getAsIntegers(noCloudHisto.getBinCounts());
        final Integer[] cloudHistoArray = getAsIntegers(cloudHisto.getBinCounts());
        for (int i = 0; i < binBordersArray.length; i++) {
            binBordersJsonArray.add(binBordersArray[i]);
        }
        for (int i = 0; i < binBordersArray.length - 1; i++) {
            noCloudHistoJsonArray.add(noCloudHistoArray[i]);
            cloudHistoJsonArray.add(cloudHistoArray[i]);
        }
        histoObj.put("h_free", noCloudHistoJsonArray);
        histoObj.put("h_cloud", cloudHistoJsonArray);
        histoObj.put("bin_boundaries", binBordersJsonArray);

        // Writing to file
        File file = new File(jsonDir + File.separator +
                daytimeString + "_" + surfaceString + "_" + cloudTypeString + "_"
                + testId + ".json");
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            System.out.println("Writing JSON histograms to file '" + file.getAbsolutePath() + "'.");
            fileWriter.write(histoObj.toJSONString());
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            System.out.println("WARNING: Could not write to JSON file '" +
                    file.getAbsolutePath() + "' - skipping. Reason: " + e.getMessage());
        }
    }
}
