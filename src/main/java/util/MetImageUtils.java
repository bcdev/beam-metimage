package util;

/**
 * MetImage utility class
 *
 * @author M. Zuehlke, O. Danne
 */
public class MetImageUtils {

    public static double getCumulativeSum(double[] array, int startIndex, int endIndex) {
        double sum = 0.0;
        final int start = Math.max(0, startIndex);
        final int end = Math.min(array.length, endIndex);
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static void getCumulativeSumAndNormalize(double[] cumArray, double[] normArray, int N) {
        normArray[N-1] = MetImageUtils.getCumulativeSum(cumArray, 0, N - 1);
        for (int i=0; i<N; i++) {
            normArray[i] = MetImageUtils.getCumulativeSum(cumArray, 0, i) / normArray[N-1];
        }
    }


    public static double[] getAsDoubles(float[] fArr) {
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

}
