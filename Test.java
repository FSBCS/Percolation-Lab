/*
* filename: Test.java
* author: Joel Hammer
*
* compilation: javac test.java
* execution: java test [n]
*
*
*/

/**
 * A little timed test for the three UF classes. Given a size n as a command
 * line paramenter, generates an instance of QuickFind, QuickUnion, and 
 * WeightedQuickUnion of size n and performs n random unions, checking if
 * the random nodes are connected first. Prints the total milliseconds each
 * class took to perform the operations.
 */
public class Test {
    private static int n;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage is: java Test [n]");
            System.exit(1);
        }
        n = Integer.parseInt(args[0]);

        UnionFind qf = new QuickFind(n);
        UnionFind qu = new QuickUnion(n);
        UnionFind wqu = new WeightedQuickUnion(n);

        System.out.print("\nWQU ");
        testUF(wqu, n);

        System.out.print("QU ");
        testUF(qu, n);

        System.out.print("QF ");
        testUF(qf, n);

    }

    private static void testUF(UnionFind uf, int ops) {
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            int a = (int) (Math.random() * n);
            int b = (int) (Math.random() * n);

            if (!uf.connected(a, b)) {
                uf.union(a, b);
            }
        }

        long stop = System.nanoTime();
        long totalTime = stop - start;
        double totalTimeMillis = totalTime / 1000000.0;

        System.out.println("Total time: " + totalTimeMillis + " ms");
    }
}
