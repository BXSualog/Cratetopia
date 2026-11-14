import java.util.Comparator;
import java.util.LinkedList;

/**
 * TreeSort – implements the TreeSort sorting algorithm using a Binary Search Tree (BST).
 *
 * DSA Concepts demonstrated here
 * --------------------------------
 * ADT: Tree & TreeSort
 *   1. A Binary Search Tree (BST) node stores an element, pointers to left and right
 *      subtrees, and a LinkedList of duplicate elements that share the exact same sort key.
 *   2. Insertion maintains the BST invariant:
 *      - Elements comparing less than (< 0) are placed into the left subtree.
 *      - Elements comparing greater than (> 0) are placed into the right subtree.
 *      - Elements comparing equal (== 0) are appended to the node's duplicates LinkedList.
 *   3. In-Order Traversal (Left -> Node -> Right):
 *      Visiting the BST in-order visits elements in strictly non-decreasing sorted order.
 *   4. Time Complexity:
 *      - Average Case: O(n log n)
 *      - Space Complexity: O(n) to store the tree nodes
 *
 * Adheres strictly to the allowed ADT constraints without any forbidden arrays or hash sets.
 */
public class TreeSort {

    /**
     * A node within the Binary Search Tree.
     */
    private static class TreeNode<T> {
        final T value;
        final LinkedList<T> duplicates = new LinkedList<>();
        TreeNode<T> left;
        TreeNode<T> right;

        TreeNode(T value) {
            this.value = value;
        }
    }

    /**
     * Sorts the provided LinkedList using the TreeSort (Binary Search Tree) algorithm.
     *
     * @param input       The unsorted LinkedList.
     * @param comparator  The comparator defining the sort order.
     * @param <T>         The element type.
     * @return            A new LinkedList containing elements in sorted order.
     */
    public static <T> LinkedList<T> sort(LinkedList<T> input, Comparator<T> comparator) {
        if (input == null || input.size() <= 1) {
            return new LinkedList<>(input != null ? input : new LinkedList<>());
        }

        TreeNode<T> root = null;
        for (T element : input) {
            root = insert(root, element, comparator);
        }

        LinkedList<T> sortedResult = new LinkedList<>();
        inOrderCollect(root, sortedResult);
        return sortedResult;
    }

    private static <T> TreeNode<T> insert(TreeNode<T> node, T value, Comparator<T> comparator) {
        if (node == null) {
            return new TreeNode<>(value);
        }

        int cmp = comparator.compare(value, node.value);
        if (cmp < 0) {
            node.left = insert(node.left, value, comparator);
        } else if (cmp > 0) {
            node.right = insert(node.right, value, comparator);
        } else {
            // Equal key: preserve stability by adding to duplicates LinkedList
            node.duplicates.add(value);
        }
        return node;
    }

    private static <T> void inOrderCollect(TreeNode<T> node, LinkedList<T> output) {
        if (node != null) {
            inOrderCollect(node.left, output);
            output.add(node.value);
            for (T dup : node.duplicates) {
                output.add(dup);
            }
            inOrderCollect(node.right, output);
        }
    }
}

