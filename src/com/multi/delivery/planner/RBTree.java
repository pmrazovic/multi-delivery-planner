package com.multi.delivery.planner;

@SuppressWarnings("unchecked")
public class RBTree<K extends Comparable<K>, V> {

    // The public inner class for nodes.
    public class Node {
        protected Node left, right; // this Node's children
        protected Node parent;      // this Node's parent
        private K key;              // this Node's key
        private V value;            // the associated value
        protected boolean isBlack;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            parent = left = right = sentinel;
            isBlack = false;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V newValue) {
            value = newValue;
        }

        protected boolean isBlack() {
            return isBlack;
        }

        protected boolean isRed() {
            return !isBlack;
        }

        protected void blacken() {
            isBlack = true;
        }

        protected void redden() {
            isBlack = false;
        }

        // Return a reference to the node in the subtree rooted at this node with the minimum
        public Node minimum() {
            Node x = this;

            // Keep going to the left until finding a node with no left child. That node
            // is the minimum node in x's subtree.
            while (x.left != sentinel)
                x = x.left;

            return x;
        }

        // Return a reference to the node in the subtree rooted at this node with the maximum key
        public Node maximum() {
            Node x = this;

            // Keep going to the left until finding a node with no right child. That node
            // is the maximum node in x's subtree.
            while (x.right != sentinel)
                x = x.right;

            return x;
        }

        // Do a left rotation around this node
        protected void leftRotate() {
            Node x = this;
            Node y = x.right;
            x.right = y.left;
            if (y.left != sentinel)
                y.left.parent = x;
            y.parent = x.parent;
            if (x.parent == sentinel)
                root = y;
            else if (x == x.parent.left)
                x.parent.left = y;
            else
                x.parent.right = y;
            y.left = x;
            x.parent = y;
        }

        // Do a right rotation around this node
        protected void rightRotate() {
            Node y = this;
            Node x = y.left;
            y.left = x.right;
            if (x.right != sentinel)
                x.right.parent = y;
            x.parent = y.parent;
            if (y.parent == sentinel)
                root = x;
            else if (y == y.parent.right)
                y.parent.right = x;
            else
                x.parent.left = x;
            x.right = y;
            y.parent = x;
        }

        // Fix the possible violation of the red-black properties
        // created by the insert method.  This node, z, which is red, could
        // have a red parent, or z could be root, which must be black.
        // Push the possible violation up toward the root, until it
        // reaches the root, which can just be made black.
        protected void rbInsertFixup() {
            Node z = this;

            while (z.parent.isRed()) {
                if (z.parent == z.parent.parent.left) {
                    // z's parent is a left child.
                    Node y = z.parent.parent.right;
                    if (y.isRed()) {
                        // Case 1: z's uncle y is red
                        z.parent.blacken();
                        y.blacken();
                        z.parent.parent.redden();
                        z = z.parent.parent;
                    }
                    else {
                        if (z == z.parent.right) {
                            // Case 2: z's uncle y is black and z is a right child
                            z = z.parent;
                            z.leftRotate();
                        }
                        // Case 3: z's uncle y is black and z is a left child
                        z.parent.blacken();
                        z.parent.parent.redden();
                        z.parent.parent.rightRotate();
                    }
                }
                else {
                    // z's parent is a right child.  Do the same as when z's
                    // parent is a left child, but exchange "left" and "right"
                    Node y = z.parent.parent.left;
                    if (y.isRed()) {
                        // Case 1: z's uncle y is red
                        z.parent.blacken();
                        y.blacken();
                        z.parent.parent.redden();
                        z = z.parent.parent;
                    }
                    else {
                        if (z == z.parent.left) {
                            // Case 2: z's uncle y is black and z is a left child
                            z = z.parent;
                            z.rightRotate();
                        }
                        // Case 3: z's uncle y is black and z is a right child
                        z.parent.blacken();
                        z.parent.parent.redden();
                        z.parent.parent.leftRotate();
                    }
                }
            }

            // The root is always black.
            root.blacken();
        }

        // Replace the subtree rooted at this node with the subtree rooted at node v.
        // Note: This method does not change v.left or v.right; it is the caller's
        // responsibility to do so
        protected void transplant(Node v) {
            if (this.parent == sentinel)        // was u the root?
                root = v;                         // if so, now v is the root
            else if (this == this.parent.left)  // otherwise adjust the child of u's parent
                this.parent.left = v;
            else
                this.parent.right = v;

            if (v != sentinel)          // if v wasn't the sentinel ...
                v.parent = this.parent;   // ... update its parent
            v.parent = this.parent;   // even if v is the sentinel
        }

        // Remove this node from a red-black tree
        // Guaranteed to remove this node, and not some other node
        public void remove() {
            Node z = this;
            Node y = z;   // y is the node either removed or moved within the tree
            boolean yOrigWasBlack = y.isBlack();  // need to know whether y was black
            Node x;       // x is the node that will move into y's original position
            if (z.left == sentinel) {       // no left child?
                x = z.right;
                z.transplant(x);              // replace z by its right child
            }
            else if (z.right == sentinel) { // no right child?
                x = z.left;
                z.transplant(x);              // replace z by its left child
            }
            else {
                // Node z has two children.  Its successor y is in its right subtree
                // and has no left child.
                y = z.right.minimum();
                yOrigWasBlack = y.isBlack();
                x = y.right;

                // Splice y out of its current location, and have it replace z.
                if (y.parent == z)
                    x.parent = y;
                else {
                    // If y is not z's right child, replace y as a child of its parent by
                    // y's right child and turn z's right child into y's right child.
                    y.transplant(x);
                    y.right = z.right;
                    y.right.parent = y;
                }

                // Regardless of whether we found that y was z's right child, replace z as
                // a child of its parent by y and replace y's left child by z's left child.
                z.transplant(y);
                y.left = z.left;
                y.left.parent = y;

                // Give y the same color as z.
                if (z.isBlack())
                    y.blacken();
                else
                    y.redden();
            }

            // If we removed a black node, then must fix up the tree because
            // black-heights are now incorrect.
            if (yOrigWasBlack)
                x.rbRemoveFixup();
        }

        // Fix the possible violation of the red-black properties caused
        // by removing a black node. This node has moved into the position of
        // the node that was removed or moved. If the removed node was black,
        // three problems could arise. If it was the root and a red child became
        // the root, now the root is red. If both this node and its parent are red, we have
        // two red nodes in a row. And moving a node y within the tree causes any simple
        // path that had contained y to have one fewer black node.
        // Consider this node, x, now occupying y's original position, as having an
        // extra black.  Restore the red-black properties by pushing the extra
        // black up in the tree until one of the following happens:
        //    x is a red-and-black node, and we color x singly black;
        //    x is the root, and we just remove the extra black; or
        //    having rotated and recolored, we exit the loop.
        protected void rbRemoveFixup() {
            Node x = this;
            Node w = null;

            while (x != root && x.isBlack()) {
                if (x == x.parent.left) {
                    w = x.parent.right;
                    if (w.isRed()) {
                        // Case 1: x's sibling w is red
                        w.blacken();
                        x.parent.redden();
                        x.parent.leftRotate();
                        w = x.parent.right;
                    }
                    if (w.left.isBlack() &&
                            (w.right).isBlack()) {
                        // Case 2: x's sibling w is black, and both of w's children are black
                        w.redden();
                        x = x.parent;
                    }
                    else {
                        if (w.right.isBlack()) {
                            // Case 3: x's sibling w is black, w's left child is red,
                            // and w's right child is black
                            w.left.blacken();
                            w.redden();
                            w.rightRotate();
                            w = x.parent.right;
                        }
                        // Case 4: x's sibling w is black, and w's right child is red
                        if (x.parent.isBlack())
                            w.blacken();
                        else
                            w.redden();
                        x.parent.blacken();
                        w.right.blacken();
                        x.parent.leftRotate();
                        x = root;
                    }
                }
                else {
                    w = x.parent.left;
                    if (w.isRed()) {
                        // Case 1: x's sibling w is red
                        w.blacken();
                        x.parent.redden();
                        x.parent.rightRotate();
                        w = x.parent.left;
                    }
                    if (w.right.isBlack() &&
                            w.left.isBlack()) {
                        // Case 2: x's sibling w is black, and both of w's children are black
                        w.redden();
                        x = x.parent;
                    }
                    else {
                        if (w.left.isBlack()) {
                            // Case 3: x's sibling w is black, w's right child is red,
                            // and w's left child is black
                            w.right.blacken();
                            w.redden();
                            w.leftRotate();
                            w = x.parent.left;
                        }
                        // Case 4: x's sibling w is black, and w's left child is red
                        if (x.parent.isBlack())
                            w.blacken();
                        else
                            w.redden();
                        x.parent.blacken();
                        w.left.blacken();
                        x.parent.rightRotate();
                        x = root;
                    }
                }
            }

            x.blacken();
        }

        // The String representation of this Node
        public String toString() {

            return "key = " + key + ", value = " + value + ", parent = "
                    + (parent == sentinel ? "sentinel" : parent.key) + ", left = "
                    + (left == sentinel ? "sentinel" : left.key) + ", right = "
                    + (right == sentinel ? "sentinel" : right.key) + ", color = " +
                    (isBlack ? "black" : "red");
        }
    }

    // Instance variables for the BST<K,V> class. They are protected so that
    // subclasses can access them.
    protected Node root;      // root of this BST
    protected Node sentinel;  // how to indicate an absent node

    // Constructor for the RBTree class
    public RBTree() {
        sentinel = getNewNode(null, null);
        sentinel.parent = sentinel;
        sentinel.left = sentinel;
        sentinel.right = sentinel;
        sentinel.blacken();
        root = sentinel;
    }

    // Create a new node and insert it into the RBTree.
    public Node insert(K key, V value) {
        Node z = getNewNode(key, value);  // create the new Node
        Node x = root;                  // Node whose key is compared with z's
        Node xParent = sentinel;        // x's parent

        // Go down the tree from the root, heading left or right depending on
        // how the new key compares with x's key, until we find a missing node,
        // indicated by the sentinel
        while (x != sentinel) {
            xParent = x;
            if (key.compareTo(x.key) < 0)
                x = x.left;
            else
                x = x.right;
        }

        // At this point, we got down to the sentinel. Make the last non-sentinel
        // node be x's parent and x the appropriate child.
        z.parent = xParent;

        if (xParent == sentinel)  // empty BST?
            root = z;               // then just the one node
        else {                    // link z as the appropriate child of x's parent
            if (key.compareTo(xParent.key) < 0)
                xParent.left = z;
            else
                xParent.right = z;
        }

        z.left = sentinel;
        z.right = sentinel;
        z.redden();
        z.rbInsertFixup();
        return z;
    }

    // Overrides the getNewNode method from the superclass.
    protected Node getNewNode(K key, V value) {
        return new Node(key, value);
    }

    // Search for a node in the subtree rooted at x with a specific key.
    public Node search(K key) {
        Node x = root;

        // Go down the left or right subtree until either we hit the sentinel or
        // find the key.
        while (x != sentinel && key.compareTo(x.key) != 0) {
            if (key.compareTo(x.key) < 0)
                x = x.left;
            else
                x = x.right;
        }

        // If we got to the sentinel, the key was not in the BST.
        if (x == sentinel)
            return null;
        else
            return x;
    }

    // Return a String representation of this tree
    public String toString() {
        if (root == sentinel)
            return "";
        else
            return print(root, 0);
    }

    // Return a string of 2*s spaces, for indenting.
    private String indent(int s) {
        String result = "";
        for (int i = 0; i < s; i++)
            result += "  ";
        return result;
    }

    // Return a String representing the subtree rooted at a node.
    private String print(Node x, int depth) {
        if (x == sentinel)
            return "";
        else
            return print(x.right, depth + 1) + indent(depth) + x.toString() + "\n"
                    + print(x.left, depth + 1);
    }


}