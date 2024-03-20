
function main() {
    var a = parseInt(readLine());
    var b = parseInt(readLine());;

    var res = addNumbers(a, b);
    console.log("The sum is " + res);
}

// Given a binary tree where each node contains a character, print the tree’s hierarchy orderly as follows:

// Input tree:

//                  a
//              /       \
//           b            c 
//         /   \         /  \
//       d       e      f    g
//              /
//             h

// Assumption 
// Children char value is larger (gte? outliers?) than parent
// Right node is larger char value than left node 



// Output:

// _ _ _ _ a -      5 items 

// _ b _ _ _ _ c -  7 

// d _ _ e _ f _ g - 8

// _ _ h -           3



// Note that the number of rows in the output matches the tree’s depth, and each column in the output should contain one and only one node.   

// Calculate the width of this representation 

// units would be arrays of course

// each level of the tree will start off wider than its parent (TODO deal with one side having no children)

class TreeNode {
  constructor(value) {
    this.value = value;
    this.left = null;
    this.right = null;
  }
}

function prettyPrint(node) {
    // Need to know how many rows -- i.e. the depth
    // Need to know how wide this shold be 
    
    const rows = findTreeDepth(node); // You would enter the root node here
    
    console.log(findNumberOfNodes(node));
    
}

//                  a
//              /       \
//           b            c 
//         /   \         /  \
//       d       e      f    g
//              /
//      

const findTreeDepth = (node, currentDepth = 0) => {
   
    // exit condition
    if (!node.left && !node.right) {
        return currentDepth;
    }
    
    return Math.max(findTreeDepth(node.right, currentDepth + 1), findTreeDepth(node.left, currentDepth + 1));
}


const findNumberOfNodes = (node, nodeCount = 0) => {
   
    // exit condition
    if (!node.left && !node.right) {
        return nodeCount;
    }
    
    const nodeCountRight = findNumberOfNodes(node.right, nodeCount + 1);
    const nodeCountLeft = findNumberOfNodes(node.left, nodeCount + 1);
    
    console.log(`nodeCountRight: ${nodeCountRight}`);
    console.log(`nodeCountLeft: ${nodeCountLeft}`);
    
    
    return nodeCountRight + nodeCountLeft;
}

/*
   1
  2    3
4  5  6  7
 */
// Example Usage:
const root = new TreeNode(1);
root.left = new TreeNode(2);
root.right = new TreeNode(3);
root.left.left = new TreeNode(4);
root.left.right = new TreeNode(5);
root.right.left = new TreeNode(6);
root.right.right = new TreeNode(7);

prettyPrint(root);

