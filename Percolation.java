
public class Percolation {

    private final UnionFind ufTop, ufAll;
    private final int n, top, bottom;
    private final boolean[][] isOpen;
    private int openSites;

    public Percolation(int n) {
        this.n = n;
        this.top = n * n;
        this.bottom = n * n + 1;

        ufTop = new WeightedQuickUnion(n * n + 1);
        ufAll = new WeightedQuickUnion(n * n + 2);

        isOpen = new boolean[n][n];
    }

    public void open(int row, int col) {
        int i = toAbs(row, col);

        if (isOpen(row, col)) {
            return;
        }

        isOpen[row][col] = true;

        if (row == 0) {
            ufTop.union(i, top);
            ufAll.union(i, top);
        }
        if (row == n - 1) {
            ufAll.union(i, bottom);
        }

        if (row > 0 && isOpen(row - 1, col)) {
            ufTop.union(i, toAbs(row - 1, col));
            ufAll.union(i, toAbs(row - 1, col));
        }

        if (row < n - 1 && isOpen(row + 1, col)) {
            ufTop.union(i, toAbs(row + 1, col));
            ufAll.union(i, toAbs(row + 1, col));
        }

        if (col > 0 && isOpen(row, col - 1)) {
            ufTop.union(i, toAbs(row, col - 1));
            ufAll.union(i, toAbs(row, col - 1));
        }

        if (col < n - 1 && isOpen(row, col + 1)) {
            ufTop.union(i, toAbs(row, col + 1));
            ufAll.union(i, toAbs(row, col + 1));
        }

        openSites++;
    }

    public boolean isOpen(int row, int col) {
        return isOpen[row][col];
    }

    public boolean isFull(int row, int col) {
        int i = toAbs(row, col);

        return ufTop.connected(i, top);
    }

    public int numberOfOpenSites() {
        return openSites;
    }

    public boolean percolates() {
        return ufAll.connected(top, bottom);
    }

    private int toAbs(int row, int col) {
        return row * n + col;
    }

    public static void main(String[] args) {
        Percolation p = new Percolation(10);
        p.open(5, 5);
        System.out.println(p.isOpen(5, 5));

    }
}
