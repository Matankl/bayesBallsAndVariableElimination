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
        String output_path = "output.txt";
        BayesNet bayesNet = null;
        VariableElimination ve = new VariableElimination();

//       // test runing parameters
//        if (args.length == 2) {
//            file = new File(args[0]);
//            output_path = args[1];
//        } else {
//            System.err.println("Invalid number of arguments. Please provide the input file path.");
//            return;
//        }

        File outputFile = new File(output_path);

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
                }
            }else{
                System.err.println("The first line of the input file should be the name of the XML file.");
                return;
            }
            //get all the queries
            BufferedWriter BW = new BufferedWriter(new FileWriter(outputFile));
            while ((st = br.readLine()) != null) {                                  // variable elimination
                System.out.println(st);
                if(st.contains("P(")){
                    //clone the bayesNet
                    BayesNet bayesNetClone = bayesNet.clone();
                    float veResult = ve.VECall(bayesNetClone,st);
                    veResult = Math.round(veResult * 100000f) / 100000f;
                    String StrResult = veResult + "0000000";
//                    System.out.println(StrResult.substring(0, 7) + "," + ve.sumOperations + "," + ve.mulOperations);
                    BW.write(StrResult.substring(0, 7) + "," + ve.sumOperations + "," + ve.mulOperations + "\n");

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
//                    System.out.println(bayesNet.isIndependent(startAndStop[0],startAndStop[1],given));
                    String ans = bayesNet.isIndependent(startAndStop[0],startAndStop[1],given) ? "yes" : "no";
                    BW.write(ans + "\n");
                }
            }
            BW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
