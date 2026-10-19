public interface UnionFind {
    public void union(int a, int b);
    public int find(int node);
    public int count();
    public boolean connected(int a, int b);
}