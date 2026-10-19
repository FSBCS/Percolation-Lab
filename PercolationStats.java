/*
* filename: PercolationStats.java
*
*
*/
import java.util.Random;

/**
 * This class performs a monte carlo simulation of the Latice Percolation
 * problem. A lattice is an n x n square of cells. During the simulation,
 * individual cells are set to open until the complete lattic percolates
 * from top to bottom. The fraction of open sites needed to percolate is
 * collected during each trial and analyzed in the accompanying methods
 * below.
 */
public class PercolationStats {

    private final double[] results;
    private Double mean = null;
    private Double std = null;
    private final int n, t;

    /**
     * Performs a monte carlo simulation of the specified number of trials on an
     * n x n percolation gride, opening sites until the system percolates. Once
     * constructed, basic statistical methods are available to analyze the
     * results of these trials.
     *
     * @param n the size of the Percolation grid (total size is n x x)
     * @param trials the number of trials to perform.
     */
    public PercolationStats(int n, int trials) {
        this.n = n;
        this.t = trials;

        results = new double[t];

        for (int i = 0; i < trials; i++) {
            results[i] = runTrial();
        }
    }

    /* Runs a percolation trial and returns the percentage opened. */
    private double runTrial() {
        Percolation p = new Percolation(n);
        int[] order = randomOrder();
        int count = 0;

        while (!p.percolates()) {
            int row = order[count] / n;
            int col = order[count] % n;
            p.open(row, col);
            count++;
        }

        return ((double) count) / (n * n);
    }

    /* Generate a random order of sites to open in the percolation. */
    private int[] randomOrder() {
        Random rand = new Random();

        int[] order = new int[n * n];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }

        for (int i = order.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = order[i];
            order[i] = order[j];
            order[j] = temp;
        }

        return order;
    }

    /**
     * Returns the mean ratio of sites opened in each percolation before it
     * percolates.
     *
     * @return mean ratio of open sites
     */
    public double mean() {
        if (this.mean != null) {
            return this.mean;
        }

        this.mean = Stats.mean(this.results);
        return this.mean();
    }

    /**
     * Returns the standard deviation of the ratio of sites opened in each
     * percolation before it percolates.
     *
     * @return standard deviation of the ratios of open sites
     */
    public double stdDev() {
        if (this.std != null) {
            return this.std;
        }

        this.std = Stats.std(this.results);
        return this.std;
    }

    /**
     * Returns the lower bound of the 95% confidence interval of the ratio
     * of sites opened in each percolation before it percolates.
     *
     * @return 95% confidence interval lower bound
     */
    public double confidenceLo() {
        return this.mean - (1.96 * stdDev()) / Math.sqrt(t);
    }

    /**
     * Returns the lower bound of the 95% confidence interval of the ratio
     * of sites opened in each percolation before it percolates.
     *
     * @return 95% confidence interval lower bound
     */
    public double confidenceHi() {
        return this.mean + (1.96 * stdDev()) / Math.sqrt(t);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage is: java PercolationStats [size] [number of trials]");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);
        int t = Integer.parseInt(args[1]);

        PercolationStats ps = new PercolationStats(n, t);

        System.out.println("mean                    = " + ps.mean());
        System.out.println("stddev:                 = " + ps.stdDev());
        System.out.println("95% confidence interval = [" + ps.confidenceLo() + ", " + ps.confidenceHi() + "]");
    }
}
