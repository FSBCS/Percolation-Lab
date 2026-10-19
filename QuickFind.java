public class QuickFind implements UnionFind {
    private final int[] id;
    private int count;

    public QuickFind(int n) {
        this.id = new int[n];
        this.count = n;

        for (int i = 0; i < id.length; i++) {
            id[i] = i;
        }
    }

    /**    (non-Javadoc)
     * Places the connected component of a into the connected component of b.
     * @see UnionFind#union(int, int)
     */
    @Override
    public void union(int a, int b) {
        validate(a);
        validate(b);

        int idA = id[a];
        int idB = id[b];

        if (idA == idB) return;

        for (int i = 0; i < id.length; i++) {
            if (id[i] == idA) {
                id[i] = idB;
            }
        }

        count--;
    }

    @Override
    public int find(int a) {
        validate(a);

        return id[a];
    }

    @Override
    public boolean connected(int a, int b) {
        validate(a);
        validate(b);

        return find(a) == find(b);
    }

    @Override
    public int count() {
        return count;
    }

    private void validate(int a) {
        if (a >= id.length || a < 0) {
            throw new IllegalArgumentException("Index " + a + " out of range 0 to " + (id.length - 1));
        }
    }

    public static void main(String[] args) {
        UnionFind uf = new QuickFind(10);
        System.out.println(uf.connected(1, 2));
        System.out.println(uf.connected(0, 9));

        uf.union(1, 0);
        uf.union(0, 2);
        System.out.println(uf.connected(1, 2));
        uf.union(9, 8);
        uf.union(9, 2);
        System.out.println(uf.connected(0, 9));
    }

}
