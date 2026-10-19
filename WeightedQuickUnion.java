
public class WeightedQuickUnion implements UnionFind {

    private int count;
    private final int[] parent;
    private final int[] sz;

    public WeightedQuickUnion(int n) {
        this.count = n;
        this.parent = new int[n];
        this.sz = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            sz[i] = 1;
        }
    }

    @Override
    public int find(int a) {
        validate(a);
        while (parent[a] != a) {
            parent[a] = parent[parent[a]];
            a = parent[a];
        }

        return a;
    }

    @Override
    public void union(int a, int b) {
        validate(a);
        validate(b);

        int rootA = find(a);
        int rootB = find(b);

        if (rootA == rootB) return;

        if (sz[rootA] > sz[rootB]) {
            parent[rootB] = rootA;
            sz[rootA] += sz[rootB];
        } else {
            parent[rootA] = rootB;
            sz[rootB] += sz[rootA];
        }

        count--;
    }

    @Override 
    public boolean connected(int a, int b) {
        validate(a);
        validate(b);

        return find(a) == find(b);
    }

    @Override
    public int count() {
        return this.count;
    }

    private void validate(int a) {
        if (a >= parent.length || a < 0) {
            throw new IllegalArgumentException("Index " + a + " out of range 0 to " + (parent.length - 1));
        }
    }
}
