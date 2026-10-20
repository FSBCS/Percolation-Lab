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

    public PercolationStats(int n, int trials) {

    }


    public double mean() {

    }


    public double stdDev() {

    }


    public double confidenceLo() {

    }


    public double confidenceHi() {

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
