package dynamicsetproblem;

/**
 * A class that implements a sorted set collection using a binary search tree (BST).
 * Note this implementation of a binary tree does not have duplicate (equal)
 * elements. This class allows a restricted view of the tree, between
 * fromElement (inclusive) and toElement (exclusive)
 *
 * @author Andrew Ensor
 *
 * This class was retrieved from the Canvas discussion forum for ADA and has
 * been extended & sub-classed for this assignment Source:
 * https://canvas.aut.ac.nz/courses/10962/discussion_topics/181813)
 */
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 * For this assignment, this class was adapted to better suit the problem:
 * - Some add/remove functionality was moved into separate methods, so it could be overriden by subclasses
 * - A hook methods were added to facilitate versioning (implemented in subclass)
 * - Additions were made to the BinaryTreeNode
 * 
 * This class was setup to maintain its original functionality, while also adding things needed for its subclasses to work
 * 
 * @author Tamati Rudd 18045626
 * @param <E> 
 */
public class BinarySearchTree<E> extends AbstractSet<E> implements SortedSet<E> {
    private int numElements;
    protected BinaryTreeNode rootNode;
    private Comparator<? super E> comparator;//null for natural ordering
    private E fromElement, toElement; // bounds for visible view of tree

    public BinarySearchTree() {
        super();
        numElements = 0;
        rootNode = null;
        comparator = null;
        fromElement = null;
        toElement = null;
    }
    
    public enum Direction {
        RIGHT_NODE,
        LEFT_NODE
    }

    public BinarySearchTree(Collection<? extends E> c) {
        this();
        for (E element : c) {
            add(element);
        }
    }

    public BinarySearchTree(Comparator<? super E> comparator) {
        this();
        this.comparator = comparator;
    }

    public BinarySearchTree(SortedSet<E> s) {
        this();
        this.comparator = s.comparator();
        for (E element : s) {
            add(element);
        }
    }

    // private constructor used to create a view of a portion of tree
    private BinarySearchTree(BinaryTreeNode rootNode, Comparator<? super E> comparator, E fromElement, E toElement) {
        this(comparator);
        this.rootNode = rootNode;
        this.fromElement = fromElement;
        this.toElement = toElement;
        // calculate the number of elements
        this.numElements = countNodes(rootNode);
    }

    // recursive helper method that counts number of descendants of node
    private int countNodes(BinaryTreeNode node) {
        if (node == null) {
            return 0;
        } else {
            return countNodes(node.leftChild) + 1
                    + countNodes(node.rightChild);
        }
    }

    // helper method that determines whether an element is within the
    // specified view
    private boolean withinView(E element) {
        boolean inside = true;
        if (fromElement != null && compare(element, fromElement) < 0) {
            inside = false;
        }
        if (toElement != null && compare(element, toElement) >= 0) {
            inside = false;
        }
        return inside;
    }

    /**
     * 
     * Hook for recording nodes that have been visited while traversing the tree
     * Implemented in subclass for use with versioning
     * @param visitedNode The node that has been visited
     */
    protected void traverseHook(BinaryTreeNode visitedNode) {
    }
    
    /**
     * Insert a BinaryTreeNode into the tree
     * @param insertionNode the node that shall be the parent of the newNode
     * @param direction which child of the insertionNode to insert at
     * @param newNode The node that is to be inserted
     */
    protected void insertNode(BinaryTreeNode insertionNode, Direction direction, BinaryTreeNode newNode) {
        if (direction == Direction.RIGHT_NODE) {
            insertionNode.rightChild = newNode;
        } else if (direction == Direction.LEFT_NODE) {
            insertionNode.leftChild = newNode;
        }
    }
    
    /**
     * Adds the element to the sorted set provided it is not already in the set, and
     * Returns true if the sorted set did not already contain the element
     *  
     * Extended to utilize hook functionality
     * Some parts of this method were moved to separate functions
     * 
     * @param o object to add
     * @return whether the object was added into a new node
     */
    public boolean add(E o) {
        if (!withinView(o)) {
            throw new IllegalArgumentException("Outside view");
        }
        BinaryTreeNode newNode = makeNode(o);
        boolean added = false;
        if (rootNode == null) {
            replaceRootNode(newNode);
            added = true;
        } else {  // find where to add newNode
            BinaryTreeNode currentNode = rootNode;
            boolean done = false;
            while (!done) {
                traverseHook(currentNode); //Assignment Extension: Record visting of a tree node
                int comparison = compare(o, currentNode.element);
                if (comparison < 0) // newNode is less than currentNode
                {
                    if (currentNode.leftChild == null) {  // add newNode as leftChild
                        insertNode(currentNode, Direction.LEFT_NODE, newNode);
                        done = true;
                        added = true;
                    } else { //Traverse left
                        currentNode = currentNode.leftChild;
                    }
                } else if (comparison > 0)//newNode is greater than currentNode
                {
                    if (currentNode.rightChild == null) {  // add newNode as rightChild
                        insertNode(currentNode, Direction.RIGHT_NODE, newNode);
                        done = true;
                        added = true;
                    } else { //Traverse right
                        currentNode = currentNode.rightChild;
                    }
                } else if (comparison == 0) // newNode equal to currentNode
                {
                    done = true; // no duplicates in this binary tree impl.
                }
            }
        }
        if (added) {
            numElements++;
        }
        return added;
    }

    // performs a comparison of the two elements, using the comparator
    // if not null, otherwise using the compareTo method
    private int compare(E element1, E element2) {
        if (comparator != null) {
            return comparator.compare(element1, element2);
        } else if (element1 != null && element1 instanceof Comparable) {
            return ((Comparable) element1).compareTo(element2); //unchecked
        } else if (element2 != null && element2 instanceof Comparable) {
            return -((Comparable) element2).compareTo(element1);//unchecked
        } else {
            return 0;
        }
    }
    
    /**
     * Replace the root node of the tree
     * @param newRootNode the node that is to be the root
     */
    protected void replaceRootNode(BinaryTreeNode newRootNode) {
        rootNode = newRootNode;
    }
    
    /**
     * Replace the removal node with another node
     * @param parentNode 
     * @param removalNode the node to remove
     * @param replacementNode the node to replace the removalNode
     */
    protected void replaceRemovalNode(BinaryTreeNode parentNode, BinaryTreeNode removalNode, BinaryTreeNode replacementNode) {
        if (removalNode == parentNode.leftChild) {
            parentNode.leftChild = replacementNode;
        } else // removalNode==parentNode.rightChild
        {
            parentNode.rightChild = replacementNode;
        }
    }

    /**
     * Remove the element from the sorted set and returns true if the element was in the sorted set
     * 
     * Extended to utilize hook functionality
     * Some parts of this method were moved to separate functions
     * 
     * @param o object to remove
     * @return whether a node with element o was removed
     */
    public boolean remove(Object o) {
        boolean removed = false;
        E element = (E) o; // unchecked, could throw exception
        if (!withinView(element)) {
            throw new IllegalArgumentException("Outside view");
        }
        if (rootNode != null) {  // check if root to be removed
            if (compare(element, rootNode.element) == 0) {
                replaceRootNode(makeReplacement(rootNode));
                removed = true;
            } else {  // Remove node in tree: search for the element o
                BinaryTreeNode parentNode = rootNode;
                BinaryTreeNode removalNode;
                // determine whether to traverse to left or right of root
                if (compare(element, rootNode.element) < 0) {
                    removalNode = rootNode.leftChild;
                } else { // compare(element, rootNode.element)>0
                    removalNode = rootNode.rightChild;
                }
                while (removalNode != null && !removed) {  // determine whether the removalNode has been found
                    traverseHook(parentNode); //Extension: call traverseHook
                    int comparison = compare(element, removalNode.element);
                    if (comparison == 0) {
                        replaceRemovalNode(parentNode, removalNode, makeReplacement(removalNode));
                        removed = true;
                    } else // determine whether to traverse to left or right
                    {
                        parentNode = removalNode;
                        if (comparison < 0) {
                            removalNode = removalNode.leftChild;
                        } else // comparison>0
                        {
                            removalNode = removalNode.rightChild;
                        }
                    }
                }
            }
        }
        if (removed) {
            numElements--;
        }
        return removed;
    }
    
    /**
     * When the removal node has two children, create a subtree on the replacement node, containing the removal node's children
     * @param removalNode the node to remove
     * @return The replacement node, with the removal node's children
     */
    protected BinaryTreeNode handleRightReplacement(BinaryTreeNode removalNode) {
        BinaryTreeNode replacementNode = removalNode.rightChild;
        // replacementNode can be pushed up one level to replace removalNode, move the left child of removalNode to be the left child of replacementNode
        if (replacementNode.leftChild == null) { //Insert removal left child into replacement node (left child)
            replacementNode.leftChild = removalNode.leftChild;
        } else { //Move the right replacement child to the left
            BinaryTreeNode parentNode;
            do { //find left-most descendant of right subtree of removalNode
                parentNode = replacementNode;
                replacementNode = replacementNode.leftChild;
            } while (replacementNode.leftChild != null);

            // move the right child of replacementNode to be the left child of the parent of replacementNode
            parentNode.leftChild = replacementNode.rightChild;
            // move the children of removalNode to be children of replacementNode
            replacementNode.leftChild = removalNode.leftChild;
            replacementNode.rightChild = removalNode.rightChild;
        }

        return replacementNode;
    }
    
    /**
     * Helper method which removes removalNode (presumed not null) and returns a reference to node that should take place of removalNode
     * 
     * Some parts of this method were moved to separate functions
     * 
     * @param removalNode node to remove
     * @return the node that replaced the removalNode
     */
    protected BinaryTreeNode makeReplacement(BinaryTreeNode removalNode) {
        BinaryTreeNode replacementNode = null;
        // check cases when removalNode has only one child
        if (removalNode.leftChild != null && removalNode.rightChild == null) {
            replacementNode = removalNode.leftChild;
        } else if (removalNode.leftChild == null && removalNode.rightChild != null) {
            replacementNode = removalNode.rightChild;
        } // check case when removalNode has two children
        else if (removalNode.leftChild != null && removalNode.rightChild != null) {  // find the inorder successor and use it as replacementNode
            replacementNode = handleRightReplacement(removalNode);
        }
        // else both leftChild and rightChild null so no replacementNode
       
        return replacementNode;
    }

    public Iterator<E> iterator() {
        return new BinaryTreeIterator(rootNode);
    }

    // returns the number of elements in the tree
    public int size() {
        return numElements;
    }

    // removes all elements from the collection
    public void clear() {
        replaceRootNode(null); // all nodes will be garbage collected as well
    }

    // overridden method with an efficient O(log n) search algorithm
    // rather than the superclasses O(n) linear search using iterator
    public boolean contains(Object o) {
        boolean found = false;
        E element = (E) o; // unchecked, could throw exception
        if (!withinView(element)) {
            return false;
        }
        BinaryTreeNode currentNode = rootNode;
        while (!found && currentNode != null) {
            int comparison = compare(currentNode.element, element);
            if (comparison == 0) {
                found = true;
            } else if (comparison < 0) {
                currentNode = currentNode.rightChild;
            } else // comparison>0
            {
                currentNode = currentNode.leftChild;
            }
        }
        return found;
    }

    // returns the Comparator used to compare elements or null if
    // the element natural ordering is used
    public Comparator<? super E> comparator() {
        return comparator;
    }

    // returns the first (lowest) element currently in sorted set that
    // is at least as big as fromElement, returns null if none found
    public E first() {
        if (rootNode == null) {
            throw new NoSuchElementException("empty tree");
        }
        // find the least descendant of rootNode that is at least
        // as big as fromElement by traversing down tree from root
        BinaryTreeNode currentNode = rootNode;
        BinaryTreeNode leastYetNode = null; // smallest found so far
        while (currentNode != null) {
            if (compare(currentNode.element, fromElement) >= 0) {
                if (compare(currentNode.element, toElement) < 0) {
                    leastYetNode = currentNode;
                }
                // move to the left child to see if a smaller element okay
                // since all in right subtree will be larger
                currentNode = currentNode.leftChild;
            } else // compare(currentNode.element, fromElement)<0
            {  // move to the right child since this element too small
                // so all in left subtree will also be too small
                currentNode = currentNode.rightChild;
            }
        }
        if (leastYetNode == null) // no satisfactory node found
        {
            return null;
        } else {
            return leastYetNode.element;
        }
    }

    public SortedSet<E> headSet(E toElement) {
        return subSet(null, toElement);
    }

    // returns the last (highest) element currently in sorted set that
    // is less than toElement, return null if none found
    public E last() {
        if (rootNode == null) {
            throw new NoSuchElementException("empty tree");
        }
        // find the greatest descendant of rootNode that is less than
        // toElement by traversing down tree from root
        BinaryTreeNode currentNode = rootNode;
        BinaryTreeNode greatestYetNode = null; // greatest found so far
        while (currentNode != null) {
            if (compare(currentNode.element, toElement) < 0) {
                if (compare(currentNode.element, fromElement) >= 0) {
                    greatestYetNode = currentNode;
                }
                // move to the right child to see if a greater element okay
                // since all in left subtree will be smaller
                currentNode = currentNode.rightChild;
            } else // compare(currentNode.element, toElement)>=0
            {  // move to the left child since this element too large
                // so all in right subtree will also be too large
                currentNode = currentNode.leftChild;
            }
        }
        if (greatestYetNode == null) // no satisfactory node found
        {
            return null;
        } else {
            return greatestYetNode.element;
        }
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        return new BinarySearchTree<E>(rootNode, comparator, fromElement,
                toElement);
    }

    public SortedSet<E> tailSet(E fromElement) {
        return subSet(fromElement, null);
    }

    // outputs the elements stored in the full binary tree (not just the view) using inorder traversal
    public String toString() {
        return rootNode.toString(0);
    }

    /**
     * Make a new Binary Tree Node
     *
     * @param element
     * @return
     */
    protected BinaryTreeNode makeNode(E element) {
        return new BinaryTreeNode(element);
    }
        
    /**
     * Inner class that represents a node in the binary tree
     * Each node consists of the element and links to left child and right child
     * No parent link is used
     * 
     * Additions:
     * - Clone method 
     * - Versioning implemented (not used in base BST, but used in subclasses)
     * - toString overriden to print the tree more clearly
     */
    public class BinaryTreeNode {
        public BinaryTreeNode leftChild, rightChild;
        public E element;
        public int version;

        public BinaryTreeNode(E element) {
            this.element = element;
            leftChild = null;
            rightChild = null;
            version = 1;
        }
        
        /**
         * Clone the BinaryTreeNode, returning an exact copy with an incremented version number
         * @return clone of this node
         */
        public BinaryTreeNode clone() {
            BinaryTreeNode newNode = new BinaryTreeNode(element);
            newNode.leftChild = leftChild;
            newNode.rightChild = rightChild;
            newNode.version = version+1;
            return newNode;
        }
        
        /**
         * Return a visualization of the tree that clearly shows children using in-order traversal
         * Different levels of the tree are differentiated by the amount of horizontal space
         * @param level level of the tree
         * @return the tree in text form
         */
        public String toString(int level) {
            String tree = "";
            for (int i = 0; i < level; i++) {
                tree += "     ";
            }
            tree += element+" (v"+version+")\n";
            if (leftChild != null) {
                tree += "L: "+leftChild.toString(level+1);
            }   
            if (rightChild != null) {
                tree += "R: "+rightChild.toString(level+1);
            }
                
            level--;
            return tree;
        }
    }

    // inner class that represents an Iterator for a binary tree
    private class BinaryTreeIterator implements Iterator<E> {
        private LinkedList<E> list;
        private Iterator<E> iterator;

        public BinaryTreeIterator(BinaryTreeNode rootNode) {  // puts the elements in a linked list using inorder traversal
            list = new LinkedList<E>();
            traverseInOrder(rootNode);
            iterator = list.iterator();
        }

        // recursive helper method that traverses the subtree from node
        // adding the elements to the list collection
        private void traverseInOrder(BinaryTreeNode node) {
            if (node != null) {
                traverseInOrder(node.leftChild);
                if (withinView(node.element)) {
                    list.add(node.element);
                }
                traverseInOrder(node.rightChild);
            }
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E next() {
            return iterator.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
