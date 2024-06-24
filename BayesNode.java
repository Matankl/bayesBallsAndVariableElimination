// 1. node class for the bayesian network - contains the name of the node, the parents of the node, the children of the node, the probability table of the node

import java.util.ArrayList;

public class BayesNode {
    //variabels
    private String name;
    private ArrayList<String> outcomes = new ArrayList<String>();   // .length = number of outcomes
    private ArrayList<BayesNode> parents = new ArrayList<BayesNode>();
    private ArrayList<BayesNode> children = new ArrayList<BayesNode>();
    Factor factor;    // Factor of the node (will be minimized in variable elimination)
    Factor Cpt;      // Conditional Probability Table as given in the XML
    public boolean isGiven = false;
    public String givenOutcome = "";

    //constructor
    public BayesNode(String name){
        this.name = name;
    }

    //methods
    public void addOutcome(String outcome){
        outcomes.add(outcome);
    }
    public void addParent(BayesNode parent){
        parents.add(parent);
    }
    public void addChild(BayesNode child){
        children.add(child);
    }
    public void collapseGiven(BayesNode node, String givenVal){
        System.out.println("factor before collapse: ");
        factor.printFactor();
        this.factor.updateGiven(givenVal, node);
        System.out.println("factor after collapse: ");
        factor.printFactor();
    }


    //getters and setters
    public String getName(){
        return name;
    }
    public ArrayList<String> getOutcomes(){
        return outcomes;
    }
    public ArrayList<BayesNode> getParents(){
        return parents;
    }
    public ArrayList<BayesNode> getChildren(){
        return children;
    }
    public Factor getFactor(){
        return factor;
    }
    public void setFactor(Factor factor){
        this.factor = factor;
    }




}
