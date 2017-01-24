package com.multi.delivery.planner;

@SuppressWarnings("unchecked")
public class RBTree<K extends Comparable<K>, V> {

    // The public inner class for nodes
    // Each node represent arrival or departure
    public class Node implements Comparable<Node> {
        protected Node left, right;     // this Node's children
        protected Node parent;          // this Node's parent
        protected boolean isBlack;      // this Node's color
        private float key;              // this Node's key (time of arrival/departure)
        private boolean isArrival;      // does this Node represents arrival or departure?
        private int leftVal;            // left value (+1 if Node is arrival, -1 if it is departure)
        private int rightVal;           // right value (-1 if Node is arrival, +1 if it is departure)
        private int sumLeft;            // sum of the left values of all of the nodes in the subtree rooted at this Node
        private int sumRight;           // sum of the right values of all of the nodes in the subtree rooted at this Node
        private int maxLeft;            // maximum possible cumulative sum of left values in the subtree rooted at this Node
        private int maxRight;           // maximum possible cumulative sum of right values in the subtree rooted at this Node
        private float maxLeftEndpoint;  // left endpoint of maximum overlapping interval in the subtree rooted at this Node
        private float maxRightEndpoint; // right endpoint of maximum overlapping interval in the subtree rooted at this Node

        // Class constructor
        public Node(float key, boolean isArrival) {
            this.key = key;
            this.isArrival = isArrival;
            if (isArrival) {
                this.leftVal = 1;
                this.rightVal = -1;
            } else {
                this.leftVal = -1;
                this.rightVal = 1;
            }
            this.sumLeft = this.leftVal;
            this.maxLeft = this.leftVal;
            this.maxLeftEndpoint = this.key;
            this.sumRight = this.rightVal;
            this.maxRight = this.rightVal;
            this.maxRightEndpoint = this.key;
            parent = left = right = sentinel;
            isBlack = false;
        }

        // Constructor for sentinel node
        public Node() {
            this.sumLeft = 0;
            this.maxLeft = 0;
            this.maxLeftEndpoint = 0;
            this.sumRight = 0;
            this.maxRight = 0;
            this.maxRightEndpoint = 0;
        }

        public float getKey() {
            return this.key;
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

        // Method compares this Node with another node
        public int compareTo(Node otherNode) {
            if (this.key < otherNode.key) {
                return -1;
            } else if (this.key == otherNode.key) {
                if (this.isArrival && !otherNode.isArrival) {
                    return 1;
                } else if (!this.isArrival && otherNode.isArrival) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        }

        // Helper method returns position of the max element in array
        private int maxOption(int[] options) {
            int maxIdx = 0;
            int max = options[0];

            for (int i = 0; i < options.length; i++) {
                if (options[i] > max) {
                    maxIdx = i;
                    max = options[i];
                }
            }

            return maxIdx;
        }

        // Method updates this Node's augmented attributes
        // when balancing the tree
        public void updateAugmentedAttrs() {
            if (this.left == sentinel && this.right == sentinel) {
                // Both of the children are sentinels
                this.sumLeft = this.leftVal;
                this.maxLeft = this.leftVal;
                this.maxLeftEndpoint = this.key;
                this.sumRight = this.rightVal;
                this.maxRight = this.rightVal;
                this.maxRightEndpoint = this.key;
            } else {
                int[] maxLeftOptions;
                float[] maxLeftPosOptions;
                int[] maxRightOptions;
                float[] maxRightPosOptions;

                if (this.left != sentinel && this.right == sentinel) {
                    // Only right child is sentinel
                    this.sumLeft = this.left.sumLeft + this.leftVal;
                    this.sumRight = this.left.sumRight + this.rightVal;

                    maxLeftOptions = new int[]{
                            this.left.maxLeft,
                            this.left.sumLeft + this.leftVal
                    };

                    maxLeftPosOptions = new float[]{
                            this.left.maxLeftEndpoint,
                            this.key
                    };

                    maxRightOptions = new int[]{
                            this.rightVal + this.left.maxRight,
                            this.rightVal
                    };

                    maxRightPosOptions = new float[]{
                            this.left.maxRightEndpoint,
                            this.key
                    };

                } else if (this.left == sentinel && this.right != sentinel) {
                    // Only left child is sentinel
                    this.sumLeft = this.leftVal + this.right.sumLeft;
                    this.sumRight = this.rightVal + this.right.sumRight;

                    maxLeftOptions = new int[]{
                            this.leftVal,
                            this.leftVal + this.right.maxLeft
                    };

                    maxLeftPosOptions = new float[]{
                            this.key,
                            this.right.maxLeftEndpoint
                    };

                    maxRightOptions = new int[]{
                            this.right.sumRight + this.rightVal,
                            this.right.maxRight
                    };

                    maxRightPosOptions = new float[]{
                            this.key,
                            this.right.maxRightEndpoint
                    };

                } else {
                    // No sentinel children
                    this.sumLeft = this.left.sumLeft + this.leftVal + this.right.sumLeft;
                    this.sumRight = this.left.sumRight + this.rightVal + this.right.sumRight;

                    maxLeftOptions = new int[]{
                            this.left.maxLeft,
                            this.left.sumLeft + this.leftVal,
                            this.left.sumLeft + this.leftVal + this.right.maxLeft
                    };

                    maxLeftPosOptions = new float[]{
                            this.left.maxLeftEndpoint,
                            this.key,
                            this.right.maxLeftEndpoint
                    };

                    maxRightOptions = new int[]{
                            this.right.sumRight + this.rightVal + this.left.maxRight,
                            this.right.sumRight + this.rightVal,
                            this.right.maxRight
                    };

                    maxRightPosOptions = new float[]{
                            this.left.maxRightEndpoint,
                            this.key,
                            this.right.maxRightEndpoint
                    };

                }

                int optionLeft = maxOption(maxLeftOptions);
                int optionRight = maxOption(maxRightOptions);

                this.maxLeft = maxLeftOptions[optionLeft];
                this.maxLeftEndpoint = maxLeftPosOptions[optionLeft];
                this.maxRight = maxRightOptions[optionRight];
                this.maxRightEndpoint = maxRightPosOptions[optionRight];
            }
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

            x.updateAugmentedAttrs();
            y.updateAugmentedAttrs();
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

            x.updateAugmentedAttrs();
            y.updateAugmentedAttrs();
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

            v.parent = this.parent;   // even if v is the sentinel

            Node x = v.parent;
            while (x != sentinel) {
                x.updateAugmentedAttrs();
                x = x.parent;
            }
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

            return "key = " + key + ", max_L = "
                    + maxLeft + ", max_R = " + maxRight + ", max_endpoint_L = "
                    + maxLeftEndpoint + ", max_endpoint_R = " + maxRightEndpoint;
        }
    }

    // Instance variables for the BST<K,V> class. They are protected so that
    // subclasses can access them.
    protected Node root;      // root of this BST
    protected Node sentinel;  // how to indicate an absent node

    // Constructor for the RBTree class
    public RBTree() {
        sentinel = new Node();
        sentinel.parent = sentinel;
        sentinel.left = sentinel;
        sentinel.right = sentinel;
        sentinel.blacken();
        root = sentinel;
    }

    public void insertInterval(int[] interval) {
        System.out.println("\n ----" + interval[0] + " - " + interval[1] + "----");
        insert(interval[0],true);
        System.out.println(this.toString());
        insert(interval[1],false);
        System.out.println(" ---------- ");
        System.out.println(this.toString());
    }

    public float[] findMaxOverlappingInterval() {
        return new float[]{this.root.maxLeftEndpoint, this.root.maxRightEndpoint};
    }

    public int getMaximumOverlaps() {
        return this.root.maxLeft;
    }

    // Create a new node and insert it into the RBTree.
    public Node insert(float key, boolean isArrival) {
        Node z = new Node(key, isArrival);  // create the new Node
        Node x = root;                  // Node whose key is compared with z's
        Node xParent = sentinel;        // x's parent

        // Go down the tree from the root, heading left or right depending on
        // how the new key compares with x's key, until we find a missing node,
        // indicated by the sentinel
        while (x != sentinel) {
            xParent = x;
            if (z.compareTo(x) < 0)
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
            if (z.compareTo(xParent) < 0)
                xParent.left = z;
            else
                xParent.right = z;
        }

        z.left = sentinel;
        z.right = sentinel;
        z.redden();

        // At this point we need to climb back the tree in order to update nodes' augmented attributes
        x = z;
        while (x != sentinel) {
            x = x.parent;
            x.updateAugmentedAttrs();
        }

        z.rbInsertFixup();
        return z;
    }

    // Search for a node in the subtree rooted at x with a specific key.
    public Node search(float key, boolean isArrival) {
        Node x = root;
        Node tmpNode = new Node(key,isArrival); // temporary node

        // Go down the left or right subtree until either we hit the sentinel or
        // find the key.
        while (x != sentinel && tmpNode.compareTo(x) != 0) {
            if (tmpNode.compareTo(x) < 0)
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