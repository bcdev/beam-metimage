package util;

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
        double[] normArray = new double[cumArray.length+1];
        final int nLength = normArray.length;
        normArray[nLength-1] = MetImageUtils.getCumulativeSum(cumArray, nLength - 1);
        for (int i=0; i<nLength; i++) {
            final double cumulativeSum = MetImageUtils.getCumulativeSum(cumArray, i);
            normArray[i] = cumulativeSum / normArray[nLength-1];
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
}
