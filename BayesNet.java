// this it the class contains the bayesian network
// it contains the nodes of the network
import java.util.HashSet;


public class BayesNet {
    private HashSet<BayesNode> nodes = new HashSet<BayesNode>();

    //constructor
    public BayesNet() {
    }

    public void addNode(BayesNode node) {
        nodes.add(node);
    }

    public BayesNode getNode(String name) {
        for (BayesNode node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public HashSet<BayesNode> getNodes() {
        return nodes;
    }

    public BayesNet clone() {
        BayesNet clone = new BayesNet();
        for (BayesNode node : this.getNodes()) {
            clone.addNode(node);
        }
        return clone;
    }
    /*_________________________________________ Bayes ball area______________________________________________________*/
    //B-E|J=T or B-E|J=T,M=T... or B-E|
    // dose B independent of E given J=T or given J=T,M=T... or given nothing
    boolean isIndependent(String start, String stop, String[] given) {

            // set the given nodes to true
            for (String g : given) {
                getNode(g).isGiven = true;
            }


        boolean ans1 = isIndependentRecursive(start, getNode(stop), true);
        boolean ans2 = isIndependentRecursive(stop, getNode(start), false);
        clearGiven();
        return ans1 && ans2;

    }

    // is independent recursive function
    // operation:
        /*
        if you visit the dest node return false

        given:
        if you visit from above:
        call recursively on the parents
        if you visit from below:
        return true

        not given:
        if you visit node from below call recursively on the parents
        if you visit node from above call recursively on the childrens

        if you visit childless node from above
        if you visit parentless node from below
         */
    boolean isIndependentRecursive(String destName, BayesNode current, boolean direction) {
        if (current.getName().equals(destName))                     // if you reached the destination
            return false;
        else if (current.isGiven) {
            if (direction) {                                        // if you visit from above
                for (BayesNode parent : current.getParents()) {     // call recursively on the parents without the node you came from
                    if (!isIndependentRecursive(destName, parent, !direction))
                        return false;                           // if the recursive call returned false return false
                }
                return true;                                        // if you reached the end of the parents return true
            }                                                 // if you visit from below
            return true;                                        // return true
        } else {
            if (direction) {                                            // if you visit node from above
                if (current.getChildren().size() == 0) {                // if you visit childless node from above
                    return true;                                        // return true
                } else {
                    for (BayesNode child : current.getChildren()) {     // call recursively on the childrens
                        if (!isIndependentRecursive(destName, child, direction))
                            return false;                               // if the recursive call returned false return false
                    }
                    return true;                                        // if you reached the end of the children return true
                }
            } else {                                                    // if you visit node from below
                if (current.getParents().size() == 0) {                 // if you visit parentless node from below
                    return true;                                        // return true
                } else {
                    for (BayesNode parent : current.getParents()) {     // call recursively on the parents
                        if (!isIndependentRecursive(destName, parent, direction))
                            return false;                               // if the recursive call returned false return false
                    }
                    return true;                                        // if you reached the end of the parents return true
                }
            }
        }
    }
    /*_________________________________________ end of Bayes ball area______________________________________________________*/

    /*_________________________________________ variable elimination area______________________________________________________*/
    void givenUpdate(String query) {
        // extracting the Given part
        String[] givenString = query.split("\\|");
        if (givenString.length == 1) {
            return;
        }
        String givenPart = givenString[1];
        givenPart = givenPart.split("\\)")[0];
        for (String given : givenPart.split(",")) {
            if (given.isEmpty()) {
                break;
            }
            String node = given.split("=")[0];
            String value = given.split("=")[1];
            value = value.split("\\)")[0];

            BayesNode current_node = this.getNode(node);
            current_node.isGiven = true;
            current_node.givenOutcome = value;
        }
    }

    //
public HashSet<BayesNode> addParentsAndMe( BayesNode node , HashSet<BayesNode> parents){
        if(node != null) {
            parents.add(node);
            for (BayesNode parent : node.getParents()) {
                parents = addParentsAndMe(parent, parents);
            }
        }
        return parents;
}


    /*_________________________________________ end of variable elimination area______________________________________________________*/




    /*_________________________________________ private area______________________________________________________*/
    public void clearGiven() {
        for (BayesNode node : nodes) {
            node.isGiven = false;
        }
    }


    public void refresh() {
        for (BayesNode node : nodes) {
            node.factor = new Factor(node.Cpt, node);
        }
    }
}