import java.util.*;
import java.util.ArrayList;


/**
 * The class that contains the variable elimination algorithm
 */
public class VariableElimination {

    ArrayList<BayesNode> RelevantNodes = new ArrayList<>(); // the nodes that are relevant to the query
    ArrayList<BayesNode> GivenNodes = new ArrayList<>();
    ArrayList<String> GivenVals = new ArrayList<>();
    ArrayList<BayesNode> HiddenNodes = new ArrayList<>();
    BayesNode queryNode;
    String queryVal;
    int sumOperations = 0;
    int mulOperations = 0;

    public Float VECall(BayesNet network, String query) {
        // Resetting the network
        resetVE(network);

        // Extracting the variables
        ProcessQueryString(network, query);

        // updating the node of their given state                                         V
        network.givenUpdate(query);

        // First step - getting rid of the non-parent of query or evidence nodes          V
        this.eliminateUselessNodes();

        // Second step - Keeping only relevant nodes using bayes ball                     V
        BBElimination(network);

        // Third step - Actually eliminating the variables
        Factor result = this.eliminateVariables();

        // creating a key to get the result from the hash map
        ArrayList<String> key = new ArrayList<>();
        key.add(this.queryVal);

        // returning the result
        return result.getFactorTable().get(key);
    }

    /**
     * Reset the variables of the variable elimination algorithm
     * @param network the network to reset
     */
    private void resetVE(BayesNet network) {
        network.refresh();
        this.mulOperations = 0;
        this.sumOperations = 0;
        this.GivenNodes.clear();
        this.GivenVals.clear();
        this.HiddenNodes.clear();
    }

    /**
     * Eliminate the irrelevant nodes using the bayes ball algorithm
     * @param network the network to eliminate the irrelevant nodes from
     */
    private void BBElimination(BayesNet network) {
        // convert GivenNodes to a string array
        String[] given = new String[this.GivenNodes.size()];
        for (int i = 0; i < this.GivenNodes.size(); i++) {
            given[i] = this.GivenNodes.get(i).getName();
        }
        for (int i = 0; i < this.RelevantNodes.size(); ) {
            BayesNode node = this.RelevantNodes.get(i);
            if (network.isIndependent(this.queryNode.getName(), node.getName(), given)) {
                System.out.println("Removing node: " + node.getName());
                this.RelevantNodes.remove(node);
            } else {
                i++;
            }
        }
    }

    /**
     * Normalize the factor to have a sum of probabilities equal to 1
     * @param result the factor to normalize
     */
    private void normalizingFactor(Factor result) {
        float sum = 0;
        this.sumOperations += result.getFactorTable().size() - 1;
        for (ArrayList<String> key : result.getFactorTable().keySet()) {
            sum += result.getFactorTable().get(key);
        }
        for (ArrayList<String> key : result.getFactorTable().keySet()) {
            result.getFactorTable().put(key, result.getFactorTable().get(key) / sum);
        }
    }

    /**
     * Eliminate the variables using the variable elimination algorithm
     * @return the factor containing the result
     */
    private Factor eliminateVariables() {

        // First step - reducing the variables by evidences
        System.out.println("Relevant nodes: ");
        for (BayesNode node : this.RelevantNodes) {
            System.out.println(node.getName());
        }
        this.givenReduction();
        System.out.println("Relevant nodes: ");
        for (BayesNode node : this.RelevantNodes) {
            System.out.println(node.getName());
        }

        // Checking if the query is already computed
        int total = 0;
        for (BayesNode node : this.RelevantNodes) {
            if (node.factor.getVariables().contains(this.queryNode)) {
                total++;
            }
        }
        if (total == 1 && this.queryNode.factor.getFactorTable().size() == 2) {
            return this.queryNode.factor;
        }

        // Second step - reducing the variables by hidden variables
        Factor result = this.hiddenReduction();

        // Third step returning the Normalized output
        System.out.println("Result factor before normalization: ");
        result.printFactor();
        normalizingFactor(result);
        System.out.println("Result factor after normalization: ");
        result.printFactor();
        return result;

    }

    /**
     * Reduce the hidden variables one by one
     * @return the factor containing the result
     */
    private Factor hiddenReduction() {
        // Get all the nodes cpt variables
        ArrayList<Factor> factors = this.getRelevantFactors(); // the list containing all the factors that are relevant to the query
        ArrayList<Factor> tempFactors; // the list containing all the factors that contains the wanted parameter

        System.out.println(this.HiddenNodes.size() + " hidden nodes");
        // print all the hidden nodes
        for (BayesNode node : this.HiddenNodes) {
            System.out.println(node.getName());
        }
        for (BayesNode node : this.HiddenNodes) { // for each hidden node
            // Get all the Factors containing the node name
            tempFactors = this.getRelevantFactors(node, factors);
            // sort the factors by their size
            Comparator<Factor> comp = new FactorComparator();
            tempFactors.sort(comp);
            // Join the factors recursively
            Factor lastFactor = this.factorJoin(tempFactors, factors);
            // Reduce the remaining factor
            if (lastFactor != null) {
                if (lastFactor.getVariables().size() != 1) {
                    System.out.println("Sum operations before reduction: " + this.sumOperations);
                    System.out.println("Reducing the last factor: ");
                    lastFactor.printFactor();
                    this.sumOperations += lastFactor.removeNode(node);
                    System.out.println("Reduced factor: ");
                    System.out.println("Sum operations after reduction: " + this.sumOperations);
                    lastFactor.printFactor();
                } else {
                    factors.remove(lastFactor);
                }
            }
        }

        this.factorJoin(factors, factors); // last join with the query node
        return factors.get(0); // return the last factor containing the query variable
    }

    /**
     * Join the factors recursively
     *
     * @param factors    the list of factors to join
     * @param allFactors the list of all the factors
     * @return the new factor
     */
    private Factor factorJoin(ArrayList<Factor> factors, ArrayList<Factor> allFactors) {

        // if there is no factor left
        if (factors.isEmpty()) {
            return null;
        }
        // if there is only one factor left
        if (factors.size() == 1) {
            if (!allFactors.contains(factors.get(0))) {
                allFactors.add(factors.get(0));
            }
            return factors.get(0);
        }
        // get the first and second factors
        Factor first = factors.get(0);
        Factor second = factors.get(1);

        System.out.println("Joining factors: ");
        first.printFactor();
        second.printFactor();

        //removing them from the factors list
        allFactors.remove(first);
        allFactors.remove(second);

        // swap them by size so that the smaller is first
        if (first.getFactorTable().size() > second.getFactorTable().size()) {
            Factor temp = first;
            first = second;
            second = temp;
        }

        // get the corresponding values of common parameters
        ArrayList<Integer> matchingValues = new ArrayList<>();
        for (BayesNode param : first.getVariables()) {
            matchingValues.add(second.getVariables().indexOf(param));
        }

        Factor newFactor = new Factor(first, second);
        // iterating over all the values of the first factor
        for (ArrayList<String> key : first.getFactorTable().keySet()) {
            // Find the matching rows of the second factor
            for (ArrayList<String> key2 : second.getFactorTable().keySet()) {
                boolean match = true;
                for (int i = 0; i < matchingValues.size(); i++) {
                    if (matchingValues.get(i) == -1) { // if the parameter is not in the second factor
                        continue;
                    }
                    String value1 = key.get(i); // The value of the joined parameter in the first factor
                    String value2 = key2.get(matchingValues.get(i)); // The value of the joined parameter in the second factor

                    if (!value1.equals(value2)) {// if the rows are matching we can multiply the values into the new factor
                        match = false;
                        break;
                    }
                }

                if (match) { // if the whole row matches
                    ArrayList<String> newKey = createNewKey(key, key2, first, second, newFactor);
                    newFactor.getFactorTable().put(newKey, first.getFactorTable().get(key) * second.getFactorTable().get(key2));
                    this.mulOperations++;
                }
            }
        }
        // New factor list with the new one instead of the two old ones
        ArrayList<Factor> newFactors = new ArrayList<>();
        System.out.print("New factor");
        newFactor.printFactor();
        newFactors.add(newFactor);
        for (int i = 2; i < factors.size(); i++) {
            newFactors.add(factors.get(i));
        }
        return factorJoin(newFactors, allFactors);
    }

    /**
     * Get all the factors that are relevant to the query
     *
     * @return the list of the factors that are relevant to the query
     */
    private ArrayList<Factor> getRelevantFactors() {
        ArrayList<Factor> relevantFactors = new ArrayList<>();
        for (BayesNode n : this.RelevantNodes) {
            if (n.factor.getFactorTable().size() > 1) {
                relevantFactors.add(n.factor);
            }
        }
        return relevantFactors;
    }

    /**
     * Get all the factors that are relevant to the query
     *
     * @param node    the node that we want to get the factors for
     * @param factors the list of all the factors
     * @return the list of the factors that are relevant to the node
     */
    private ArrayList<Factor> getRelevantFactors(BayesNode node, ArrayList<Factor> factors) {
        ArrayList<Factor> relevantFactors = new ArrayList<>();
        for (Factor f : factors) {
            if (f.getVariables().contains(node)) {
                relevantFactors.add(f);
            }
        }
        return relevantFactors;
    }

    /**
     * Iterate over all the nodes and reduce their given values
     * by collapsing the factors
     */
    private void givenReduction() {

        for (BayesNode node : this.RelevantNodes) {
            ArrayList<BayesNode> temp = (ArrayList<BayesNode>) node.factor.getVariables().clone();
            for (BayesNode factorParam : temp) {
                if (this.GivenNodes.contains(factorParam) && node.factor.getVariables().contains(factorParam)) {
                    System.out.println("Factor before collapse: ");
                    node.factor.printFactor();
                    node.collapseGiven(factorParam, factorParam.givenOutcome);
                    System.out.println("Factor after collapse: ");
                    node.factor.printFactor();
                }
            }
        }
    }

    /**
     * Process the query string and extract the variables
     *
     * @param network the network
     * @param query   the query string
     */
    private void ProcessQueryString(BayesNet network, String query) {
        String paramString = query.split("\\(")[1]; //P(A=T|B=T,C=F) E-M -> A|B=T,C=F) E-M
        String HiddenVar = paramString.split("\\)")[1]; // -> E-M
        paramString = paramString.split("\\)")[0]; // -> A=T|B=T,C=F
        String GivenVar = "";
        if (paramString.split("\\|").length == 2) { // if there is a given variable
            GivenVar = paramString.split("\\|")[1]; // -> B=T,C=F
        }
        paramString = paramString.split("\\|")[0]; // -> A=T
        String QueryVar = paramString.split("=")[0]; // -> A
        String QueryVal = paramString.split("=")[1]; // -> T

        // Assigning the variable and the value
        this.queryNode = network.getNode(QueryVar);
        this.queryVal = QueryVal;

        // Assigning the hidden and given variables
        for (String hidden : HiddenVar.split("-")) {
            this.HiddenNodes.add(network.getNode(hidden.trim()));
        }

        for (String given : GivenVar.split(",")) {
            if (given.isEmpty()) {
                break;
            }
            this.GivenNodes.add(network.getNode(given.split("=")[0]));
            this.GivenVals.add(given.split("=")[1]);
        }

        System.out.println("Hidden nodes: ");
        for (BayesNode node : this.HiddenNodes) {
            System.out.println(node.getName());
        }
    }

    /**
     * Eliminate the nodes that are not a parent of the query node or a given node
     */
    private void eliminateUselessNodes() {
        for (BayesNode node : this.GivenNodes) {
            eliminateUselessNodesRec(node);
        }
        eliminateUselessNodesRec(this.queryNode);
    }

    /**
     * Eliminate the nodes that are not a parent of the given node
     *
     * @param node the node to eliminate the parents of
     */
    private void eliminateUselessNodesRec(BayesNode node) {
        if (!this.RelevantNodes.contains(node)) {
            this.RelevantNodes.add(node);
        }

        for (BayesNode parent : node.getParents()) {
            eliminateUselessNodesRec(parent);
        }
    }

    /**
     * Create a new key for the new factor
     *
     * @param key       the key of the first factor
     * @param key2      the key of the second factor
     * @param first     the first factor
     * @param second    the second factor
     * @param newFactor the new factor
     * @return the new key
     */
    private ArrayList<String> createNewKey(ArrayList<String> key, ArrayList<String> key2, Factor first, Factor second, Factor newFactor) {
        ArrayList<String> newKey = new ArrayList<>();
        // iterating aver all the parameters of the new factor
        for (BayesNode param : newFactor.getVariables()) {
            if (first.getVariables().contains(param)) {
                newKey.add(key.get(first.getVariables().indexOf(param))); // add the value for the parameter
            } else {
                newKey.add(key2.get(second.getVariables().indexOf(param)));
            }
        }
        return newKey;
    }
}