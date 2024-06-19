// Purpose: This class is used to create a CPT object that will be used to store the CPT of a node
import java.util.ArrayList;
import java.util.HashMap;
public class Factor {

    //variables
    private HashMap<ArrayList, Double> factorTable; // this is the CPT table of the node

    //constructor
    public Factor(String[] Data, BayesNode node) {
        this.factorTable = new HashMap<ArrayList, Double>();
        double[] DoubleData = new double[Data.length];
        for (int i = 0; i < Data.length; i++) {
            DoubleData[i] = Double.parseDouble(Data[i]);
        }
        ArrayList<BayesNode> parentsAndNode = (ArrayList<BayesNode>) node.getParents().clone();
        parentsAndNode.add(node);
        for (int i = 0; i < DoubleData.length; i++) {
            int divisor = 1;
            ArrayList<String> key = new ArrayList<String>();
            for (int j = 0; j < parentsAndNode.size(); j++) {
                divisor *= parentsAndNode.get(j).getOutcomes().size();
                int index = (i / (DoubleData.length / divisor)) % parentsAndNode.get(j).getOutcomes().size();
                key.add(parentsAndNode.get(j).getOutcomes().get(index));
            }
            this.factorTable.put(key, DoubleData[i]);
        }
    }


    //methods
    public void printFactor() {
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
