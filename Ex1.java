import java.io.*;

// Arcitecture:
// 1. node class for the bayesian network - contains the name of the node, the parents of the node, the children of the node, the probability table of the node
// 2. bayesian network class
// 3. main class
// 4. XML parser class to convert XML to bayesian network
// 5. CPT class to store the conditional probability table

public class Ex1 {

    public static void main(String[] args) {
        File file = new File("input.txt");
        BayesNet bayesNet = null;
        //read the queries from the file
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            st = br.readLine();
            if(st.contains(".xml")){
                XMLParser xmlParser = new XMLParser(st);
                bayesNet = xmlParser.getNet();


                //debug prints delete later
                System.out.println("Bayesian network created");
                for (BayesNode node : bayesNet.getNodes()) {
                    System.out.println("Node: " + node.getName());
                    System.out.println("Outcomes: " + node.getOutcomes());
                    System.out.println("Parents: " + node.getParents());
                    System.out.println("Children: " + node.getChildren());
                    System.out.println("Factor: " + node.getFactor());
                    node.getFactor().printFactor();
                }
            }else{
                System.err.println("The first line of the input file should be the name of the XML file.");
                return;
            }
            //get all the queries
            while ((st = br.readLine()) != null) {
                System.out.println(st);
                if(st.contains("P(")){
                    System.out.println("Variable elimination");

                }else{                                                              //Bayes Ball
                    String[] given = {};
                    System.out.println("Bayes Ball is independent:");
                    // parse the quarry before "-" is the first node after "-" and before "|" is the second node
                    String[] split = st.split("\\|");
                    String[] startAndStop = split[0].split("-");
                    // if there is no given
                    if (split.length != 1) {
                         given = split[1].split(",");
                        // strip all the given from the values and keep just the names
                        for (int i = 0; i < given.length; i++) {
                            given[i] = given[i].substring(0, given[i].indexOf("="));
                        }
                    }
                    System.out.println(bayesNet.isIndependent(startAndStop[0],startAndStop[1],given));


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        //parse the queries




    }

}
