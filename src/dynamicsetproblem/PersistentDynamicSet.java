package dynamicsetproblem;

import java.util.ArrayList;

/**
 * This subclass of Binary Search Tree (BST) implements tree versioning, using hook methods and template design pattern
 * @author Tamati Rudd 18045626
 */
public class PersistentDynamicSet<E> extends BinarySearchTree<E> {
    public ArrayList<BinaryTreeNode> previousVersions;
    public ArrayList<BinaryTreeNode> traversedNodes;

    /**
     * Construct a persistent dynamic set
     */
    public PersistentDynamicSet() {
        super();
        previousVersions = new ArrayList<>();
        traversedNodes = new ArrayList<>();
    }
    
    /**
     * Pop the most recently visited node off the traversedNodes list (as if it was a stack)
     * @return popped node 
     */
    protected BinaryTreeNode popTraversalStack() {
        int index = traversedNodes.size()-1;
        BinaryTreeNode currentNode = traversedNodes.get(index);
        traversedNodes.remove(index);
        return currentNode;
    }
    
    /**
     * Hook for recording nodes that have been visited while adding into the tree
     * @param visitedNode The node that has been visited
     */
    @Override
    protected void traverseHook(BinaryTreeNode visitedNode) {
        traversedNodes.add(visitedNode);
    }
    
    /**
     * Traverse up from a new leaf node (created by add or remove hooks) to build the new tree
     * Uses unchanged parts of the previous tree, and traversed nodes for the path to the leaf
     * @param newChild
     * @param oldChild 
     */
    protected void createTreeFromLeaf(BinaryTreeNode newChild, BinaryTreeNode oldChild) {
        //Handle parent(s) of the inserted node
        while (!traversedNodes.isEmpty()) {
            BinaryTreeNode currentNode = popTraversalStack();
            
            BinaryTreeNode clonedCurrentNode = currentNode.clone();

            if (currentNode.leftChild == oldChild) {
                clonedCurrentNode.leftChild = newChild;
            } else if (currentNode.rightChild == oldChild) {
                clonedCurrentNode.rightChild = newChild;
            }  else {
                throw new RuntimeException("Neither right or left");
            }
            newChild = clonedCurrentNode;
            oldChild = currentNode;
        }
        
        replaceRootNode(newChild);
    }
    
    /**
     * Insert the new node
     * Then, create a new tree version, using unchanged nodes from the old tree, and traversed nodes
     * @param oldParent
     * @param direction
     * @param newNode 
     */
    @Override
    protected void insertNode(BinaryTreeNode oldParent, Direction direction, BinaryTreeNode newNode) {
        //Handle the inserted node
        BinaryTreeNode newParent = oldParent.clone();
        if (direction == Direction.RIGHT_NODE) {
            newParent.rightChild = newNode;
        } else if (direction == Direction.LEFT_NODE) {
            newParent.leftChild = newNode;
        }
        
        //Remove the insertion node from the stack 
        popTraversalStack();
        
        //Create the new tree
        createTreeFromLeaf(newParent, oldParent);
    }
    
    
    /**
     * Clear the ArrayList before doing a new add operation
     * @param o element to add
     * @return whether a node containing o was added to the tree
     */
    @Override
    public boolean add(E o) {
        traversedNodes.clear();
        return super.add(o);
    }
       
    /**
     * Replace the root node
     */
    @Override
    protected void replaceRootNode(BinaryTreeNode newRootNode) {
        if (rootNode != null) {
            previousVersions.add(rootNode);
        } 
        rootNode = newRootNode;
    }
    
    /**
     * Replace the removal node with another node
     * @param oldParent
     * @param removalNode
     * @param replacementNode 
     */
    protected void replaceRemovalNode(BinaryTreeNode oldParent, BinaryTreeNode removalNode, BinaryTreeNode replacementNode) {
        //Handle the first parent node
        BinaryTreeNode newParent = oldParent.clone();
        if (removalNode == oldParent.leftChild) {
            newParent.leftChild = replacementNode;
        } else // removalNode==parentNode.rightChild
        {
            newParent.rightChild = replacementNode;
        }
        
        //Remove the first parent node from the stack 
        popTraversalStack();
        
        //Create the new tree
        createTreeFromLeaf(newParent, oldParent);
    }
    
    /**
     * When the removal node has two children, create a subtree on the replacement node, containing the removal node's children
     * @param removalNode
     * @return The replacement node, with the removal node's children
     */
    @Override
    protected BinaryTreeNode handleRightReplacement(BinaryTreeNode removalNode) {
        BinaryTreeNode oldReplacementNode = removalNode.rightChild;
        BinaryTreeNode newReplacementNode = oldReplacementNode.clone();
        
        // replacementNode can be pushed up one level to replace removalNode, move the left child of removalNode to be the left child of replacementNode
        if (oldReplacementNode.leftChild == null) { //Insert removal left child into replacement node (left child)
            newReplacementNode.leftChild = removalNode.leftChild;
        } else { //Move the right replacement child to the left
            BinaryTreeNode newRemovalNodeRightChild = newReplacementNode;
            BinaryTreeNode newParentNode = null; //Parent of newReplacementNode
            do { //find left-most descendant of right subtree of removalNode. Traverse left down both trees, copying each level of the tree
                oldReplacementNode = oldReplacementNode.leftChild; //traverse down the old tree
                newParentNode = newReplacementNode; //clone result of line above
                newReplacementNode = oldReplacementNode.clone(); //traverse down the new tree
                newParentNode.leftChild = newReplacementNode; //link previous and new clone
            } while (oldReplacementNode.leftChild != null);

            // move the right child of replacementNode to be the left child of the parent of replacementNode
            newParentNode.leftChild = newReplacementNode.rightChild;
            // move the children of removalNode to be children of replacementNode
            newReplacementNode.leftChild = removalNode.leftChild;
            newReplacementNode.rightChild = newRemovalNodeRightChild;
        }

        return newReplacementNode;
    }   
}
