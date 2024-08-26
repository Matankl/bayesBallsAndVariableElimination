# BayesNet Java Project

## Overview

This project implements a Bayesian Network and supporting structures in Java. It includes a variety of classes that handle different aspects of the network, such as nodes, factors, and XML parsing. The primary components of the project are:

- `BayesNet.java`: Represents the Bayesian Network and manages the structure and relationships between nodes.
- `BayesNode.java`: Represents an individual node in the Bayesian Network, with associated probabilities and parent/child relationships.
- `Factor.java`: Handles factor operations, essential for performing inference in the Bayesian Network.
- `FactorComparator.java`: Compares factors for various operations.
- `XMLParser.java`: Parses XML files to build and initialize the Bayesian Network.
- `Ex1.java`: Example or main class for executing and demonstrating the functionality of the Bayesian Network.

## Features

- **Bayesian Network Representation**: The project models a Bayesian Network with nodes, parents, and conditional probability tables.
- **XML Parsing**: The network structure can be loaded from an XML file, allowing for flexible and reusable network configurations.
- **Factor Operations**: Supports factor multiplication and marginalization, which are critical for performing probabilistic inference in Bayesian Networks.
- **Comparator for Factors**: Allows for sorting and comparing factors, useful in algorithms like Variable Elimination.
- **Example Execution**: The `Ex1.java` file demonstrates the setup and use of the Bayesian Network, providing a clear example of how to use the classes together.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Ant (optional, for building the project)

### Running the Project

1. **Compile the Code**: Ensure all `.java` files are in the same directory and run:
    ```sh
    javac *.java
    ```
2. **Run the Example**: Execute the main class to see the Bayesian Network in action:
    ```sh
    java Ex1
    ```

### XML Network Configuration

The `XMLParser.java` is designed to read an XML file that describes the structure of a Bayesian Network. Ensure your XML follows the required schema, typically involving definitions for nodes and conditional probability tables.

## Project Structure

- **`BayesNet.java`**: The core class managing the Bayesian Network.
- **`BayesNode.java`**: Represents nodes within the network, each associated with a specific variable and conditional probabilities.
- **`Factor.java`**: Implements factors and provides methods for manipulating them.
- **`FactorComparator.java`**: Compares and orders factors based on specified criteria.
- **`XMLParser.java`**: Responsible for parsing the Bayesian Network structure from an XML file.
- **`Ex1.java`**: Example class demonstrating the use of the Bayesian Network.

## Example Usage

Here's an outline of how the classes work together:

```java
// Create a new Bayesian Network
BayesNet network = new BayesNet();

// Parse network structure from XML
XMLParser parser = new XMLParser("path/to/network.xml");
network = parser.parse();

// Perform some operation with Factors
Factor f1 = new Factor(...);
Factor f2 = new Factor(...);
Factor result = f1.multiply(f2);

// Compare factors
FactorComparator comparator = new FactorComparator();
int comparison = comparator.compare(f1, f2);

// Execute example case
Ex1.main(args);
```

## Contributing

If you'd like to contribute to this project, please fork the repository and submit a pull request. For major changes, please open an issue first to discuss what you would like to change.


