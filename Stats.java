
/**
 * Utility methods for calculating descriptive statistics on arrays of
 * {@code double} or {@code int} values.
 *
 * <p>This class cannot be instantiated. Statistical methods return
 * {@link Double#NaN} for {@code null} or empty arrays. Sample variance and
 * standard deviation also return {@code NaN} when fewer than two values are
 * provided.</p>
 */
public class Stats {

    private Stats() {
    }

    /**
     * Finds the smallest value in an array.
     *
     * @param arr the values to inspect
     * @return the smallest value, or {@link Double#NaN} if {@code arr} is
     *         {@code null}, empty, or contains {@code NaN}
     */
    public static double min(double[] arr) {
        if (!isValid(arr)) return Double.NaN;

        double min = arr[0];

        for (int i = 1; i < arr.length; i++) {
            min = Math.min(min, arr[i]);
        }

        return min;
    }

    /**
     * Finds the smallest value in an integer array.
     *
     * @param arr the values to inspect
     * @return the smallest value, or {@link Double#NaN} if {@code arr} is
     *         {@code null} or empty
     */
    public static double min(int[] arr) {
        if (!isValid(arr)) return Double.NaN;

        int min = arr[0];

        for (int i = 1; i < arr.length; i++) {
            min = Math.min(min, arr[i]);
        }

        return min;
    }

    /**
     * Finds the largest value in an array.
     *
     * @param arr the values to inspect
     * @return the largest value, or {@link Double#NaN} if {@code arr} is
     *         {@code null}, empty, or contains {@code NaN}
     */
    public static double max(double[] arr) {
        if (!isValid(arr)) return Double.NaN;

        double max = arr[0];

        for (int i = 1; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }

    /**
     * Finds the largest value in an integer array.
     *
     * @param arr the values to inspect
     * @return the largest value, or {@link Double#NaN} if {@code arr} is
     *         {@code null} or empty
     */
    public static double max(int[] arr) {
        if (!isValid(arr)) return Double.NaN;

        int max = arr[0];

        for (int i = 1; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }

    /**
     * Calculates the arithmetic mean of an array.
     *
     * @param arr the values to average
     * @return the arithmetic mean, or {@link Double#NaN} if {@code arr} is
     *         {@code null}, empty, or contains {@code NaN}
     */
    public static double mean(double[] arr) {
        if (!isValid(arr)) return Double.NaN;

        double avg = 0;

        for (int i = 0; i < arr.length; i++) {
            avg += (arr[i] - avg) / (i + 1);
        }

        return avg;
    }

    /**
     * Calculates the arithmetic mean of an integer array.
     *
     * @param arr the values to average
     * @return the arithmetic mean, or {@link Double#NaN} if {@code arr} is
     *         {@code null} or empty
     */
    public static double mean(int[] arr) {
        if (!isValid(arr)) return Double.NaN;

        double avg = 0;

        for (int i = 0; i < arr.length; i++) {
            avg += ((double) arr[i] - avg) / (i + 1);
        }

        return avg;
    }

    /**
     * Calculates the sample variance of an array, dividing the sum of squared
     * deviations from the mean by {@code arr.length - 1}.
     *
     * @param arr the values to analyze
     * @return the sample variance, or {@link Double#NaN} if {@code arr} is
     *         {@code null}, contains fewer than two values, or contains
     *         {@code NaN}
     */
    public static double var(double[] arr) {
        if (!isValid(arr) || arr.length < 2) return Double.NaN;

        double avg = mean(arr);
        double sum = 0;

        for (int i = 0; i < arr.length; i++) {
            sum += (arr[i] - avg) * (arr[i] - avg);
        }

        return sum / (arr.length - 1);
    }

    /**
     * Calculates the sample variance of an integer array, dividing the sum of
     * squared deviations from the mean by {@code arr.length - 1}.
     *
     * @param arr the values to analyze
     * @return the sample variance, or {@link Double#NaN} if {@code arr} is
     *         {@code null} or contains fewer than two values
     */
    public static double var(int[] arr) {
        if (!isValid(arr) || arr.length < 2) return Double.NaN;

        double avg = mean(arr);
        double sum = 0;

        for (int value : arr) {
            double difference = value - avg;
            sum += difference * difference;
        }

        return sum / (arr.length - 1);
    }

    /**
     * Calculates the sample standard deviation, the square root of the sample
     * variance.
     *
     * @param arr the values to analyze
     * @return the sample standard deviation, or {@link Double#NaN} if
     *         {@code arr} is {@code null}, contains fewer than two values, or
     *         contains {@code NaN}
     */
    public static double std(double[] arr) {
        if (!isValid(arr) || arr.length < 2) return Double.NaN;

        return Math.sqrt(var(arr));
    }

    /**
     * Calculates the sample standard deviation of an integer array, the square
     * root of its sample variance.
     *
     * @param arr the values to analyze
     * @return the sample standard deviation, or {@link Double#NaN} if
     *         {@code arr} is {@code null} or contains fewer than two values
     */
    public static double std(int[] arr) {
        if (!isValid(arr) || arr.length < 2) return Double.NaN;

        return Math.sqrt(var(arr));
    }

    private static boolean isValid(double[] arr) {
        return arr != null && arr.length > 0;
    }

    private static boolean isValid(int[] arr) {
        return arr != null && arr.length > 0;
    }

    /**
     * Entry point for running this class as a Java application.
     *
     * <p>This method currently performs no actions.</p>
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        
    }
}
