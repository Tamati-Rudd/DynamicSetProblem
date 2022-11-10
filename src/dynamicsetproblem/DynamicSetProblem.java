package dynamicsetproblem;

import dynamicsetproblem.BinarySearchTree.BinaryTreeNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * This class tests the Binary Search Tree and its subclasses (PersistentDynamicSet & BalancedPersistentDynamicSet)
 * @author Tamati Rudd 18045626
 */
public class DynamicSetProblem {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {  
        //Create a tree for testing. Comment in the tree type that is to be tested
//        BinarySearchTree<String> tree = new BinarySearchTree<String>();
//        PersistentDynamicSet<String> tree = new PersistentDynamicSet<String>();
        BalancedPersistentDynamicSet<String> tree = new BalancedPersistentDynamicSet<String>();

        //Build the tree
        tree.add("cow");
        tree.add("bat");
        tree.add("fox");
        tree.add("ant");
        tree.add("cat");
        tree.add("eel");
        tree.add("owl");
        tree.add("fly");
        tree.add("dog");
        tree.remove("fox");
        
        //Print the tree information
        for (BinaryTreeNode oldTree : tree.previousVersions) {
            System.out.println("Old Tree Version: \n"+oldTree.toString(0));
        }
        System.out.println("Current Tree: \n" + tree);
        System.out.println("Total Versions: "+(tree.previousVersions.size()+1));
        
        //Binary Search Tree Stuff (ignore)
//        SortedSet<String> subtree = tree.subSet("cat", "fox");
//        System.out.print("Subtree iteration: ");
//        Iterator<String> i = subtree.iterator();
//        while (i.hasNext()) {
//            System.out.print(i.next());
//            if (i.hasNext()) {
//                System.out.print(", ");
//            }
//        }
//        System.out.println();
//        System.out.println("first element in subtree: " + subtree.first());
//        System.out.println("last element in subtree: " + subtree.last());
    }
}
