
public class QuickUnion implements UnionFind {

    private int count;
    private final int[] parent;

    public QuickUnion(int n) {
        this.count = n;
        this.parent = new int[n];

        // Initialize every node as its own parent.
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }
    }

    @Override
    public int find(int a) {
        validate(a);

        while (parent[a] != a) {
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

        if (rootA == rootB) {
            return;
        }

        parent[rootA] = rootB;
        count--;
    }

    @Override
    public boolean connected(int a, int b) {
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
