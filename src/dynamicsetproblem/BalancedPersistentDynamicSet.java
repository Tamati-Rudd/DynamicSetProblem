package dynamicsetproblem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This subclass of Persistent Dynamic Set adds red black tree functionality to the set
 * @author Tamati Rudd 18045626
 */
public class BalancedPersistentDynamicSet<E> extends PersistentDynamicSet<E> {
    
    /**
     * Construct a new Balanced Persistent Dynamic Set
     */
    public BalancedPersistentDynamicSet() {
        super();
    }
    
    /**
     * Colours needed for a red-black tree
     */
    public enum Colour {
        RED,
        BLACK
    }
    
    /**
     * Replace the root node
     * @param newRootNode
     */
    @Override
    protected void replaceRootNode(BinaryTreeNode newRootNode) {
        RedBlackNode newRedBlackRoot = (RedBlackNode) newRootNode;
        newRedBlackRoot.colour = Colour.BLACK;
        super.replaceRootNode(newRedBlackRoot);
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
        
        HashMap<RedBlackNode, RedBlackNode> map = new HashMap<>();
        map.put((RedBlackNode) newNode, (RedBlackNode) newParent);
        RedBlackNode currentNode = (RedBlackNode) newParent;
        for (int i = traversedNodes.size(); i > 0; i--) {
            RedBlackNode currentParent = (RedBlackNode)traversedNodes.get(i-1);
            map.put(currentNode, currentParent);
            currentNode = currentParent;
        }
        map.put(currentNode, null);
        traversedNodes.clear();
        insertFixup(map, (RedBlackNode) newNode);   
    }
    
    
    /**
     * Traverse up from a new leaf node (created by add or remove hooks) to build the new tree
     * Uses unchanged parts of the previous tree, and traversed nodes for the path to the leaf
     * @param newChild
     * @param oldChild 
     */
    @Override
    protected void createTreeFromLeaf(BinaryTreeNode newChild, BinaryTreeNode oldChild) {
        //Handle parent(s) of the inserted node
        for (int i = traversedNodes.size(); i > 0; i--) {
            
            BinaryTreeNode currentNode = traversedNodes.get(i-1);
            
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
            traversedNodes.set(i-1, clonedCurrentNode);
        }
        
        replaceRootNode(newChild);
    }
    
    /**
     * Get the parent of a node by accessing the traversed nodes list
     * @return parent of the currentNode
     * @param parentsMap map of parents
     * @param currentNode node to get the parent for
     */
    protected RedBlackNode getParent(HashMap parentsMap, RedBlackNode currentNode) {
       return (RedBlackNode) parentsMap.get(currentNode);
    }
    
    /**
     * Get the grandparent of a node by accessing the traversed nodes list
     * @return parent of the parent the currentNode
     * @param parentsMap map of parents
     * @param currentNode node to get the parent for
     */
    protected RedBlackNode getGrandparent(HashMap parentsMap, RedBlackNode currentNode) {
       return (RedBlackNode) parentsMap.get(parentsMap.get(currentNode));
    }
    
    /**
     * Restores the red-black conditions of the tree after inserting a node.
     *
     * @param parentsMap map of parents
     * @param insertedNode The node inserted.
     */
    protected void insertFixup(HashMap parentsMap, RedBlackNode insertedNode) {
        RedBlackNode y = null;

        while (getParent(parentsMap, insertedNode).colour == Colour.RED) {
            if (getParent(parentsMap, insertedNode) == getGrandparent(parentsMap, insertedNode).leftChild) {
                y = (RedBlackNode)((RedBlackNode) parentsMap.get(parentsMap.get(insertedNode))).rightChild;
                if (y != null && y.colour == Colour.RED) {
                    getParent(parentsMap, insertedNode).colour = Colour.BLACK;
                    y = y.clone();
                    y.colour = Colour.BLACK;
                    RedBlackNode clonedParent = getGrandparent(parentsMap, insertedNode);
                    clonedParent.rightChild = y;
                    clonedParent.colour = Colour.RED;
                    insertedNode = getGrandparent(parentsMap, insertedNode);
                } else { //If BLACK
                    if (insertedNode == getParent(parentsMap, insertedNode).rightChild) {
                        insertedNode = getParent(parentsMap, insertedNode);
                        leftRotate(parentsMap, getParent(parentsMap, insertedNode));
                    }

                    getParent(parentsMap, insertedNode).colour = Colour.BLACK;
                    RedBlackNode parentParent = getGrandparent(parentsMap, insertedNode);
                    if (parentParent != null) {
                        parentParent.colour = Colour.RED;
                        rightRotate(parentsMap, parentParent);
                    }
                }
            } else {
                y = (RedBlackNode) getGrandparent(parentsMap, insertedNode).leftChild;
                if (y != null && y.colour == Colour.RED) {
                    getParent(parentsMap, insertedNode).colour = Colour.BLACK;
                    y = y.clone();
                    y.colour = Colour.BLACK;
                    RedBlackNode clonedParent = getGrandparent(parentsMap, insertedNode);
                    clonedParent.leftChild = y;
                    clonedParent.colour = Colour.RED;
                    insertedNode = getGrandparent(parentsMap, insertedNode);
                } else { //If BLACK
                    if (insertedNode == getParent(parentsMap, insertedNode).leftChild) {
                        insertedNode = getParent(parentsMap, insertedNode);
                        rightRotate(parentsMap, getParent(parentsMap, insertedNode));
                    }

                    getParent(parentsMap, insertedNode).colour = Colour.BLACK;
                    getGrandparent(parentsMap, insertedNode).colour = Colour.RED;
                    
                    
                    RedBlackNode parentParent = getGrandparent(parentsMap, insertedNode);
                    if (parentParent != null) {
                        parentParent.colour = Colour.RED;
                        leftRotate(parentsMap, parentParent);
                    }
                    
                }
            }
        }
    }
    
    /**
     * Performs a left rotation on a node, making the node's right child its parent.
     *
     * @param parentsMap the map of parents
     * @param x The node to rotate
     */
    protected void leftRotate(HashMap parentsMap, RedBlackNode x) {
        RedBlackNode y = (RedBlackNode) x.rightChild;
        if (y != null) {
            y = y.clone();
            // Swap the in-between subtree from y to x.
            x.rightChild = y.leftChild;
            if (y.leftChild != null) {
                parentsMap.put(y.leftChild, x);
            }
            parentsMap.put(y, getParent(parentsMap, x));
        } else {
            x.rightChild = null;
        } 

        // If x is the root of the entire tree, make y the root.
        // Otherwise, make y the correct child of the subtree's parent.
        if (getParent(parentsMap, x) == null && y != null) {
            replaceRootNode(y);
        } else {
            RedBlackNode xParent = getParent(parentsMap, x);
            if (xParent != null) {
                RedBlackNode xParentClone = xParent.clone();
                if (x == xParentClone.leftChild) {
                    xParentClone.leftChild = y;
                } else {
                    xParentClone.rightChild = y;
                }

                RedBlackNode xxParent = getParent(parentsMap, xParent);
                if (xxParent != null) {
                    parentsMap.put(xParentClone, xxParent);
                }
            }
        } 

        // Relink x and y
        if (y != null) {
            y.leftChild = x;
        } 
        parentsMap.put(x, y);
    }

    /**
     * Performs a right rotation on a node, making the node's left child its parent.
     *
     * @param parentsMap
     * @param x The node to rotate
     */
    protected void rightRotate(HashMap parentsMap, RedBlackNode x) {
        RedBlackNode y = (RedBlackNode) x.leftChild;
        
        if (y != null) {
            y = y.clone();
             x.leftChild = y.rightChild;
            if (x.leftChild != null) {
                parentsMap.put(y.rightChild, x);
            }
            parentsMap.put(y, getParent(parentsMap, x));

            y.rightChild = x;
        } else {
            x.leftChild = null;
        }
        parentsMap.put(x, y);

        if (rootNode == x && y != null) {
            replaceRootNode(y);
        } else {
            RedBlackNode yParent = getParent(parentsMap, y);
            if (yParent != null) {
                 RedBlackNode yParentClone = yParent.clone();
                if (yParent.leftChild == x) {
                    yParent.leftChild = y;
                } else {
                    yParent.rightChild = y;
                }

                RedBlackNode yyParent = getParent(parentsMap, yParent);
                if (yyParent != null) {
                    parentsMap.put(yParentClone, yyParent);
                }
            }
        }
    }
 
    /**
     * Make a new Red Black Node
     *
     * @param element element of the node
     * @return new RedBlackNode
     */
    protected BinaryTreeNode makeNode(E element) {
        return new RedBlackNode(element);
    }

    /**
     * Inner class that overrides the BinaryTreeNode class to include a colour
     */
    public class RedBlackNode extends BinaryTreeNode {
        public Colour colour;

        /**
         * Construct a RedBlackNode
         *
         * @param element
         */
        public RedBlackNode(E element) {
            super(element);
            colour = Colour.RED;
        }
        
        /**
         * Clone the red black node
         * @return clone
         */
        public RedBlackNode clone() {
            RedBlackNode newNode = new RedBlackNode(element);
            newNode.leftChild = leftChild;
            newNode.rightChild = rightChild;
            newNode.colour = colour;
            newNode.version = version+1;
            return newNode;
        }
        
        /**
         * Return a visualization of the tree that clearly shows children using in-order traversal
         * Different levels of the tree are differentiated by the amount of horizontal space
         * @param level level of the tree
         * @return the tree in text form
         */
        @Override
        public String toString(int level) {
            String tree = "";
            for (int i = 0; i < level; i++) {
                tree += "     ";
            }
            
            tree += element+" "+colour+" (v"+version+")\n";
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
}
