/**
 * This interface represents an abstract implementation of the union-find
 * data type for solving the dynamic connectivity problem. In brief, given
 * a system of nodes, we successively connect pairs of nodes by a direct
 * path. We also say that nodes between which a path of connected nodes 
 * exists are also connected. Therefore, the relation "connected" is an
 * equivalence relation:
 * - a is connected to a (all nodes are connected to themselves)
 * - if a is connected to b, b is connected to a
 * - if a and b are connected, then a is connected to any c for which b
 *   is connected to c.
 * 
 * The maximal set {x | x is connected to A} is called the connected
 * component of A.
 * 
 * The UnionFind abstract data type implements this class by providing two
 * key methods: union(a, b) and find(). The function, union(a, b), connects
 * the nodes a and b by merging their equivalence classes into a single class.
 * The function find() returns some unique identifier (an integer). Most
 * commonly this is some element of the class. Note however, that while it
 * may be any element from the class it must be the same element for the
 * entire class. Thus, we can define an additional function connected(a,b)
 * which simply checks that a and b have the same indentifier returned by
 * find.
 */
public interface UnionFind {
    /**
     * Connects the nodes a and b, merging their connected components
     * into a single component.
     * @param a the first node to connect
     * @param b the second node to connect.
     */
    public void union(int a, int b);

    /**
     * Returns the id of the connected component of a. Usually this is
     * some element of the component (the "canonical element"). Note that
     * this function must return the same value for every element in the
     * connected component (hence the element is "canonical").
     * @param a the node to check
     * @return the connected component of node a.
     */
    public int  find(int a);

    /**
     * Check if node a is connected to b. By default, checks that a and b
     * are in the same connected component by calling find for both and
     * checking if they have the same id.
     * @param a the first node to check
     * @param b the second node to check
     * @return true if a and b are connected, false otherwise.
     */
    public default boolean connected(int a, int b) {
        return this.find(a) == this.find(b);
    }

    /**
     * Counts the total number of connected components in the UF system.
     * @return the total number of distinct connected components.
     */
    public int count();
}