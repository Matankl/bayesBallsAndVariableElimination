// this class gets an xml file and parses it to get bayes ball network


//format of the xml file:
// <NETWORK>  this is the root tag - open new network
// <VARIABLE>  this is the variable tag - open new node
// <NAME> new node name
// <OUTCOME> one of the outcomes possible - this tag can be repeated (nested in VARIABLE)
// </VARIABLE> close the node
// <DEFINITION> this is the definition tag - open the cpt table of the node

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;

public class XMLParser {
    //variables
    private String xmlFile;
    private BayesNet bayesNet;

    //constructor
    public XMLParser(String xmlFile){
        this.xmlFile = xmlFile;
        this.bayesNet = parseXML();
    }

    //methods
    BayesNet parseXML() {
        //parse the xml file
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(doc.getDocumentElement().getNodeName() != "NETWORK"){
            System.out.println("Error: root element is not NETWORK");
            return null;
        }
        Element root = doc.getDocumentElement();
        NodeList nList = root.getElementsByTagName("VARIABLE");

        BayesNet bayesNet = new BayesNet();
        // go over all the "VARIABLE" tags create a node for each one fill the node with the data from the xml and add it to the bayesNet
        for(int i = 0; i < nList.getLength(); i++){                                         // go over all the nodes
            Element nodeElement = (Element) nList.item(i);
            for(int j = 0; j < nodeElement.getChildNodes().getLength(); j++) {               // go over all the tags of the node
                if (nodeElement.getChildNodes().item(j).getNodeName() == "NAME") {
                    String nodeName = nodeElement.getChildNodes().item(j).getTextContent();
                    BayesNode node = new BayesNode(nodeName);                               // create a new node
                    bayesNet.addNode(node);                                                 // add the node to the bayesNet
                    //add the outcomes to the node
                    for(int k = 0; k < nodeElement.getChildNodes().getLength(); k++) {        // go over all the tags of the node
                        if (nodeElement.getChildNodes().item(k).getNodeName() == "OUTCOME") {
                            String outcome = nodeElement.getChildNodes().item(k).getTextContent();
                            node.addOutcome(outcome);
                        }
                    }

                }
            }
        }
        nList = root.getElementsByTagName("DEFINITION");
        // go over all the "DEFINITION" tags and fill the CPT of the nodes
        for(int i = 0; i < nList.getLength(); i++){                                         // go over all the nodes
            ArrayList<BayesNode> Variables = new ArrayList<>();
            Element nodeElement = (Element) nList.item(i);
            for(int j = 0; j < nodeElement.getChildNodes().getLength(); j++) {               // go over all the tags of the node
                if (nodeElement.getChildNodes().item(j).getNodeName() == "FOR") {
                    String nodeName = nodeElement.getChildNodes().item(j).getTextContent();
                    BayesNode node = bayesNet.getNode(nodeName);                               // get the node
                    for(int k = 0; k < nodeElement.getChildNodes().getLength(); k++) {        // go over all the tags of the node
                        if (nodeElement.getChildNodes().item(k).getNodeName() == "GIVEN") {
                            String parentName = nodeElement.getChildNodes().item(k).getTextContent();
                            BayesNode parent = bayesNet.getNode(parentName);                     // get the parent node
                            node.addParent(parent);
                            parent.addChild(node);
                            Variables.add(bayesNet.getNode(parentName));
                        }
                        if (nodeElement.getChildNodes().item(k).getNodeName() == "TABLE") {
                            String table = nodeElement.getChildNodes().item(k).getTextContent();
                            String[] tableArray = table.split(" ");
                            Factor thisFactor = new Factor(tableArray, node,Variables);
                            node.Cpt = thisFactor;

                        }
                    }
                    Variables.add(bayesNet.getNode(nodeName));
                }
            }
        }

        return bayesNet;
    }


    public BayesNet getNet(){
        return bayesNet;
    }

    private void parseXMLFile(){
        //parse the xml file
    }
}
