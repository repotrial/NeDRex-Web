#Example Overview
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


### Drug-Target Investigation
In this example, the the genes (proteins) that are targeted by Aspirin are taken to explore the disease associations there are for these Aspirin genes.

- [Aspirin Genes](drug_repurposing/drug-target_investigation/Aspirin-module.png): The list of genes Aspirin targets.
- [Aspirin Disorders](drug_repurposing/drug-target_investigation/Aspirin-disorder.list): The list of disorders identified to be associated with genes that are targeted by Aspirin
- [Aspirin Network](drug_repurposing/drug-target_investigation/Aspirin-targets.graphml): A graphml file containing the aspirin-gene-disorder network


### Hypothesis-driven Drug Repurposing

- [ALCL vs NSCLC](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc.png)
- [ALCL vs NSCLC-1](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-1.png)
- [ALCL vs NSCLC-2](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-2.png)
- [ALCL vs NSCLC-3](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-3.png)
- [ALCL vs NSCLC-4](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-4.png)
- [ALCL vs NSCLC-5](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-5.png)
- [ALCL vs NSCLC-6](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-6.png)
- [ALCL vs NSCLC-7](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-7.png)
- [ALCL vs NSCLC-8](drug_repurposing/hypothesis_driven_drug_repurposing/alcl_vs_nsclc-8.png)



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
