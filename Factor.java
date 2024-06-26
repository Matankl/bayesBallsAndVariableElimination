// Purpose: This class is used to create a CPT object that will be used to store the CPT of a node
import java.util.ArrayList;
import java.util.HashMap;

public class Factor {

    //variables
    private ArrayList<BayesNode> Variables;
    private HashMap<ArrayList<String>, Float> factorTable; // this is the CPT table of the node

    //constructor
    // Constructor to initialize the factor with raw data node and variables
    public Factor(String[] Data, BayesNode node, ArrayList<BayesNode> Variables) {
        this.factorTable = new HashMap<ArrayList<String>, Float>();
        this.Variables = Variables;
        Float[] FloatData = new Float[Data.length];
        for (int i = 0; i < Data.length; i++) {
            FloatData[i] = Float.parseFloat(Data[i]);
        }
        ArrayList<BayesNode> parentsAndNode = (ArrayList<BayesNode>) node.getParents().clone();
        parentsAndNode.add(node);
        for (int i = 0; i < FloatData.length; i++) {
            int divisor = 1;
            ArrayList<String> key = new ArrayList<String>();
            for (int j = 0; j < parentsAndNode.size(); j++) {
                divisor *= parentsAndNode.get(j).getOutcomes().size();
                int index = (i / (FloatData.length / divisor)) % parentsAndNode.get(j).getOutcomes().size();
                key.add(parentsAndNode.get(j).getOutcomes().get(index));
            }
            this.factorTable.put(key, FloatData[i]);
        }
    }
    public Factor(Factor first, Factor second) {
        // get all the parameter of the new factor
        this.Variables = (ArrayList<BayesNode>) first.Variables.clone();
        for (BayesNode node : second.getVariables()) {
            if (!this.Variables.contains(node)) {
                this.Variables.add(node);
            }
        }
        this.factorTable = new HashMap<>();
    }

    public Factor(Factor prev, BayesNode node){
        this.Variables = (ArrayList<BayesNode>) node.Cpt.getVariables().clone();
        this.factorTable = new HashMap<>();
        for (ArrayList<String> key : prev.factorTable.keySet()) {
            this.factorTable.put((ArrayList<String>) key.clone(), prev.factorTable.get(key));
        }
    }


    //methods
    public int removeNode(BayesNode node) {

        // creating new key
        int index = this.Variables.indexOf(node); // get the index of the node that we want to remove

        HashMap<ArrayList<String>, Float> new_data = new HashMap<>();
        ArrayList<String> temp_key;
        int total_sum = 0;
        // iterating aver all the rows of the factor and removing the collapsed parameter
        for (ArrayList<String> key : this.factorTable.keySet().toArray(new ArrayList[0])) {
            float val = this.factorTable.get(key);
            temp_key = (ArrayList<String>) key.clone();
            temp_key.remove(index);

            // if this is the second time we see this key we add the values to the previous one
            if (new_data.containsKey(temp_key)) {
                new_data.put(temp_key, new_data.get(temp_key) + val);
                total_sum += 1;
            } else { // otherwise we create a new key with the first met value
                new_data.put(temp_key, val);
            }
        }
        this.factorTable = new_data;

        // removing the node from the title
        this.Variables.remove(node);
        return total_sum;
    }
    public void updateGiven(String value, BayesNode node) {
        int index = 0;
        for (ArrayList<String> key : this.factorTable.keySet().toArray(new ArrayList[0])) {
            float temp = this.factorTable.get(key);
            this.factorTable.remove(key);
            index = this.Variables.indexOf(node);
            if (key.get(index).equals(value)) {
                key.remove(index);
                this.factorTable.put(key, temp);
            }
        }
        // removing the param from the title
        this.Variables.remove(node);
    }

    //getters and setters
    public HashMap<ArrayList<String>, Float> getFactorTable() {
        return factorTable;
    }
    public ArrayList<BayesNode> getVariables() {
        return Variables;
    }












    /*_________________________________________ private area______________________________________________________*/

    public void printFactor() {
        // printing all the factors variable and their values
        System.out.println("");
        for (BayesNode node : Variables) {
            System.out.print(node.getName() + " ");
        }
        System.out.println("");
        for (ArrayList key : factorTable.keySet()) {
            System.out.print("P(");
            for (int i = 0; i < key.size(); i++) {
                System.out.print(key.get(i));
                if (i != key.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print(") = " + factorTable.get(key) + "\n");
        }
    }

}
