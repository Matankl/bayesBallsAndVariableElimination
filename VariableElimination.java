import java.util.*;
import java.util.ArrayList;


/**
 * MIGHT NEED TO CHANGE:
 * String[] given = {} in method bayesBallElimination this is a patch to not give the actual given values because I marked them before
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

    public Float Call(BayesNet network, String query) {
        // Resetting the network
        resetVE(network);

        // Extracting the variables
        ProcessQueryString(network, query);

        // updating the node of their given state                                         V
        network.given_update(query);

        // First step - getting rid of the non-parent of query or evidence nodes          V
        this.eliminate_useless_nodes();

        // Second step - Keeping only relevant nodes using bayes ball                     V
        BBElimination(network);


        // Third step - Actually eliminating the variables
        Factor result = this.eliminate_variables();


        // creating a key to get the result from the hash map
        ArrayList<String> key = new ArrayList<>();
        key.add(this.queryVal);

        // returning the result
        return result.getFactorTable().get(key);
    }

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
    private void Normalizing_factor(Factor result) {
        float sum = 0;
        this.sumOperations += result.getFactorTable().size() -1;
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
    private Factor eliminate_variables() {

        // First step - reducing the variables by evidences
        System.out.println("Relevant nodes: ");
        for (BayesNode node : this.RelevantNodes) {
            System.out.println(node.getName());
        }
        this.given_reduction();
        System.out.println("Relevant nodes: ");
        for (BayesNode node : this.RelevantNodes) {
            System.out.println(node.getName());
        }

        // Checking if the query is already computed
        int total = 0;
        for (BayesNode node : this.RelevantNodes){
            if (node.factor.getVariables().contains(this.queryNode)){
                total ++;
            }
        }
        if (total == 1 && this.queryNode.factor.getFactorTable().size() == 2) {
            return this.queryNode.factor;
        }

        // Second step - reducing the variables by hidden variables
        Factor result = this.hidden_reduction();

        // Third step returning the Normalized output
        System.out.println("Result factor before normalization: ");
        result.printFactor();
        Normalizing_factor(result);
        System.out.println("Result factor after normalization: ");
        result.printFactor();
        return result;

    }

    /**
     * Reduce the hidden variables one by one
     * @return the factor containing the result
     */
    private Factor hidden_reduction() {
        // Get all the nodes cpt variables
        ArrayList<Factor> factors = this.get_relevant_factors(); // the list containing all the factors that are relevant to the query
        ArrayList<Factor> temp_factors; // the list containing all the factors that contains the wanted parameter

        System.out.println(this.HiddenNodes.size() + " hidden nodes");
        // print all the hidden nodes
        for (BayesNode node : this.HiddenNodes) {
            System.out.println(node.getName());
        }
        for (BayesNode node : this.HiddenNodes) { // for each hidden node
            // Get all the Factors containing the node name
            temp_factors = this.get_relevant_factors(node, factors);
            // sort the factors by their size
            Comparator<Factor> comp = new FactorComparator();
            temp_factors.sort(comp);
            // Join the factors recursively
            Factor last_factor = this.join(temp_factors, factors);
            // Reduce the remaining factor
            if (last_factor != null) {
                if (last_factor.getVariables().size() != 1) {
                    System.out.println("Sum operations before reduction: " + this.sumOperations);
                    System.out.println("Reducing the last factor: ");
                    last_factor.printFactor();
                    this.sumOperations += last_factor.removeNode(node);
                    System.out.println("Reduced factor: ");
                    System.out.println("Sum operations after reduction: " + this.sumOperations);
                    last_factor.printFactor();
                } else {
                    factors.remove(last_factor);
                }
            }
        }

        this.join(factors, factors); // last join with the query node
        return factors.get(0); // return the last factor containing the query variable
    }

    /**
     * Join the factors recursively
     * @param factors the list of factors to join
     * @param all_factors the list of all the factors
     * @return the new factor
     */
    private Factor join(ArrayList<Factor> factors, ArrayList<Factor> all_factors) {

        // if there is no factor left
        if (factors.isEmpty()){
            return null;
        }
        // if there is only one factor left
        if (factors.size() == 1) {
            if (!all_factors.contains(factors.get(0))){
                all_factors.add(factors.get(0));
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
        all_factors.remove(first);
        all_factors.remove(second);

        // swap them by size so that the smaller is first
        if (first.getFactorTable().size() > second.getFactorTable().size()) {
            Factor temp = first;
            first = second;
            second = temp;
        }

        // get the corresponding values of common parameters
        ArrayList<Integer> matching_values = new ArrayList<>();
        for (BayesNode param : first.getVariables()) {
            matching_values.add(second.getVariables().indexOf(param));
        }

        Factor newFactor = new Factor(first, second);
        // iterating over all the values of the first factor
        for (ArrayList<String> key : first.getFactorTable().keySet()) {
            // Find the matching rows of the second factor
            for (ArrayList<String> key2 : second.getFactorTable().keySet()) {
                boolean match = true;
                for (int i = 0; i < matching_values.size(); i++) {
                    if (matching_values.get(i) == -1) { // if the parameter is not in the second factor
                        continue;
                    }
                    String value1 = key.get(i); // The value of the joined parameter in the first factor
                    String value2 = key2.get(matching_values.get(i)); // The value of the joined parameter in the second factor

                    if (!value1.equals(value2)) {// if the rows are matching we can multiply the values into the new factor
                        match = false;
                        break;
                    }
                }

                if (match){ // if the whole row matches
                    ArrayList<String> new_key = create_new_key(key, key2, first, second, newFactor);
                    newFactor.getFactorTable().put(new_key, first.getFactorTable().get(key) * second.getFactorTable().get(key2));
                    this.mulOperations++;
                }
            }
        }
        // New factor list with the new one instead of the two old ones
        ArrayList<Factor> new_factors = new ArrayList<>();
        System.out.print("New factor");
        newFactor.printFactor();
        new_factors.add(newFactor);
        for (int i = 2; i < factors.size(); i++) {
            new_factors.add(factors.get(i));
        }
        return join(new_factors, all_factors);
    }

    /**
     * Get all the factors that are relevant to the query
     * @return the list of the factors that are relevant to the query
     */
    private ArrayList<Factor> get_relevant_factors() {
        ArrayList<Factor> relevant_factors = new ArrayList<>();
        for (BayesNode n : this.RelevantNodes) {
            if (n.factor.getFactorTable().size() > 1) {
                relevant_factors.add(n.factor);
            }
        }
        return relevant_factors;
    }

    /**
     * Get all the factors that are relevant to the query
     * @param node the node that we want to get the factors for
     * @param factors the list of all the factors
     * @return the list of the factors that are relevant to the node
     */
    private ArrayList<Factor> get_relevant_factors(BayesNode node, ArrayList<Factor> factors) {
        ArrayList<Factor> relevant_factors = new ArrayList<>();
        for (Factor f : factors) {
            if (f.getVariables().contains(node)) {
                relevant_factors.add(f);
            }
        }
        return relevant_factors;
    }

    /**
     * Iterate over all the nodes and reduce their given values
     */
    private void given_reduction() {

        for (BayesNode node : this.RelevantNodes) {
            ArrayList<BayesNode> temp = (ArrayList<BayesNode>) node.factor.getVariables().clone();
            for (BayesNode factorParam : temp) {
                if (this.GivenNodes.contains(factorParam) && node.factor.getVariables().contains(factorParam)) {
                    System.out.println("Factor before collapse: ");
                    node.factor.printFactor();
                    node.collapse_given(factorParam, factorParam.givenOutcome);
                    System.out.println("Factor after collapse: ");
                    node.factor.printFactor();
                }
            }
        }
    }

    /**
     * Process the query string and extract the variables
     * @param network the network
     * @param query the query string
     */
    private void ProcessQueryString(BayesNet network, String query) {
        String paramString = query.split("\\(")[1]; //P(A=T|B=T,C=F) E-M -> A|B=T,C=F) E-M
        String HiddenVar = paramString.split("\\)")[1]; // -> E-M
        paramString = paramString.split("\\)")[0]; // -> A=T|B=T,C=F
        String GivenVar = "";
        if (paramString.split("\\|").length == 2){ // if there is a given variable
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
    private void eliminate_useless_nodes() {
        for (BayesNode node : this.GivenNodes) {
            eliminate_useless_nodes_recursive(node);
        }
        eliminate_useless_nodes_recursive(this.queryNode);
    }

    /**
     * The recursive part of eliminate_useless_nodes
     * @param node the node to eliminate
     */
    private void eliminate_useless_nodes_recursive(BayesNode node) {
        if (!this.RelevantNodes.contains(node)) {
            this.RelevantNodes.add(node);
        }

        for (BayesNode parent : node.getParents()) {
            eliminate_useless_nodes_recursive(parent);
        }
    }

    /**
     * Create a new key for the new factor
     * @param key the key of the first factor
     * @param key2 the key of the second factor
     * @param first the first factor
     * @param second the second factor
     * @param newFactor the new factor
     * @return  the new key
     */
    private ArrayList<String> create_new_key(ArrayList<String> key, ArrayList<String> key2, Factor first, Factor second, Factor newFactor) {
        ArrayList<String> new_key = new ArrayList<>();
        // iterating aver all the parameters of the new factor
        for (BayesNode param : newFactor.getVariables()) {
            if (first.getVariables().contains(param)) {
                new_key.add(key.get(first.getVariables().indexOf(param))); // add the value for the parameter
            } else {
                new_key.add(key2.get(second.getVariables().indexOf(param)));
            }
        }
        return new_key;
    }
}