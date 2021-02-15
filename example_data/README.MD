# Example Overview
Here some example cases alongside data that is produced along the exploration is presented. In addition images are provided.
A list explaining the content follows!

- [Metagraph](metagraph.png): Image describing the node and edge types that are used
- [Technologies](Technologies.png): Image containing a simplified view of the project setup
- [Datamanagement](Datamanagement.png): Image containing a simplified view of data communication and processing in this project


## Induced Subnetworks
This example shows the creation of a gene-induced disorder-disorder network. A list of disorder umbrella terms is taken to create a gene-disease network and from that a disease-disease network is derived by using shared genes as conneciton.

- [Disorder Umbrealla-names](induced_subnetworks/disorder_umbrellas.list): The umbrella term list used to create the disorder lists
- [Disorder Names](induced_subnetworks/disorders.list): List of all disorders used for the GiDD
- [GiDD Network](induced_subnetworks/GiDD.graphml): A graphml file for the GiDD
- [GiDD](induced_subnetworks/GiDD-network.png): A high resolution image of the GiDD with colored disorder groups and labeled subgroups.
- [G-D Network Small](induced_subnetworks/GiDD-network_small-genes.png): A small image of the gene-disorder layout based on the disorder list.
- [GiDD Network Small](induced_subnetworks/GiDD-network_small.png): A small image of the GiDD layout.

## Drug Repurposing
The goal of the drug repurposing examples is to show different approaches to use our tool to identify alternative targets for drugs.

### Drug-Target Investigation
In this example, the the genes (proteins) that are targeted by Aspirin are taken to explore the disease associations there are for these Aspirin genes.

- [Aspirin Genes](drug_repurposing/drug-target_investigation/Aspirin-module.png): The list of genes Aspirin targets.
- [Aspirin Disorders](drug_repurposing/drug-target_investigation/Aspirin-disorder.list): The list of disorders identified to be associated with genes that are targeted by Aspirin
- [Aspirin Network](drug_repurposing/drug-target_investigation/Aspirin-targets.graphml): A graphml file containing the aspirin-gene-disorder network


### Hypothesis-driven Drug Repurposing
This approach uses two disorders ALCL (anaplastic large-cell lymphoma) and NSCLC (non-small-cell lung carcinoma) derives for each a +50 disease module with DIAMOnD, creates the module intersection and uses the intersection to identify drug targets for both disorders.

- [ALCL vs NSCLC](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc.png): The example setup as an image
- [ALCL Seeds](drug_repurposing/hypothesis_driven_drug_repurposing/ALCL_seeds.tsv): Seed list of ALCL
- [ALCL Module List](drug_repurposing/hypothesis_driven_drug_repurposing/ALCL_module.tsv): Module list of ALCL
- [ALCL Module Graph](drug_repurposing/hypothesis_driven_drug_repurposing/ALCL_module.graphml): Module as graphml of ALCL
- [NSCLC Seeds](drug_repurposing/hypothesis_driven_drug_repurposing/NSCLC_seeds.tsv): Seed list of NSCLC
- [NSCLC Module List](drug_repurposing/hypothesis_driven_drug_repurposing/NSCLC_module.tsv): Module list of NSCLC
- [NSCLC Module Graph](drug_repurposing/hypothesis_driven_drug_repurposing/NSCLC_module.graphml): Module as graphml of NSCLC
- [ALCL vs NSCLC Intersection List](drug_repurposing/hypothesis_driven_drug_repurposing/ALCL-NSCLC_modules_intersecting.tsv): A list with the module intersection of ALCL and NSCLC
- [ALCL vs NSCLC Intersection Drugs](drug_repurposing/hypothesis_driven_drug_repurposing/ALCL-NSCLC_modules_intersecting-drugs.tsv): The ranked drugs for the module intersection
- [ALCL vs NSCLC Intersection Graph](drug_repurposing/hypothesis_driven_drug_repurposing/ALCL-NSCLC_modules_intersecting-drugs.graphml): The graphml file for the graph of the ranked drugs for the module intesection

A list of Screenshots depicting the exact example step-by-step.

- [ALCL vs NSCLC-1](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-1.png)
- [ALCL vs NSCLC-2](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-2.png)
- [ALCL vs NSCLC-3](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-3.png)
- [ALCL vs NSCLC-4](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-4.png)
- [ALCL vs NSCLC-5](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-5.png)
- [ALCL vs NSCLC-6](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-6.png)
- [ALCL vs NSCLC-7](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-7.png)
- [ALCL vs NSCLC-8](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-8.png)

### Explorative Drug Repurposing
In this example Alzheimer's Disease (AD) is taken as example to go through a exploratory process of trying to identify drugs for the given disorder. The methods grow more complex and sophisticated with each step:

#### Guided Exploration
The Guided Exploration is used to create a basic Alzheimer-protein-drug network.

- [Alzheimer Drugs Graph](explorative_drug_repurposing/Guided-Alzheimer-drugs.graphml): The graphml file showing the first result of creating a Alzheimer-gene-drug network.

#### Blitz Module Creation
Blitz Module is used to create a disease module for AD, the protein interactions are used to create a new induced drug network.

- [Alzheimer Module List](explorative_drug_repurposing/Blitz-Alzheimer-protein_module.tsv): The module created by the blitz module creation for AD
- [Alzheimer Module Graph](explorative_drug_repurposing/Blitz-Alzheimer-protein_module_induced_drugs.graphml): The graphml file for the protein module induced Alzheimer-drug network

#### Blitz Drug Repurposing
The Blitz Drug Repurposing (Blitz Drug Module + Blitz Drug Prioritization) is applied on AD.

- [Alzheimer Drugs List](explorative_drug_repurposing/Blitz_Repurposing-Alzheimer.tsv): The list of drugs found by Blitz Repurposing for AD.
- [Alzheimer Drugs Graph](explorative_drug_repurposing/Blitz_Repurposing-Alzheimer.graphml): The graphml for the created network of Blitz Repurposing for AD.

#### Quick Drug Repurposing
A Closeness Centrality parameter (Only approved) was disabled.

- [Alzheimer Drugs List](explorative_drug_repurposing/Quick_Repurposing-Alzheimer.tsv): The list of drugs found by Quick Repurposing for AD.
- [Alzheimer Drugs Graph](explorative_drug_repurposing/Quick_Repurposing-Alzheimer.graphml): The graphml for the created network of Quick Repurposing for AD.


## Page Explanation
The following images are describing the different components in the different views of the web interface:

### Quick Drug Repurposing
- [Start](page_explanation/Quick_start.png)
- [Seed Selection](page_explanation/Quick_seeds.png)
- [Algorithm Configuration](page_explanation/Quick_algorithm.png)
- [Result Page](page_explanation/Quick_results.png)

### Guided Exploration

- [Node Selection](page_explanation/Guided_nodes.png)
- [Path Configuration](page_explanation/Guided_paths.png)
- [Result Page](page_explanation/Guided_result.png)

### Advanced Exploration

- [Start](page_explanation/Advanced_start.png)
- [List View](page_explanation/Advanced_list.png)
- [Graph View](page_explanation/Advanced_graph.png)
- [Graph History](page_explanation/Advanced_history.png)
