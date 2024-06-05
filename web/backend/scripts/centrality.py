import sys
import graph_tool as gt
import graph_tool.topology as gtt


# =============================================================================
def print_usage():
    print(' ')
    print('        usage: python3 closeness.py network_file seed_file outfile_name(optional) only_direct_drugs only_approved_drugs')
    print('        -----------------------------------------------------------------')
    print('        network_file       : The network file must be provided as graphml or gt format.')
    print('                             Nodes attribute "primaryDomainId" is used in graphml as node id.')
    print('                             Make sure both nodes and edges have an attribute "type".')
    print('                             Only nodes with the following types are valid: Protein, Drug')
    print('                             Only edges with the following types are valid: DrugHasTarget, ProteinInteractsWithProtein')
    print('        seed_file          : table containing the seed proteins (if table contains')
    print('                             more than one column they must be tab-separated;')
    print('                             the first column will be used only)')
    print('        outfile_name       : results will be saved under this file name')
    print('                             by default the outfile_name is set to "closeness_ranked.txt"')
    print('        only_direct_drugs  : If only the drugs interacting directly with seeds should be considered in the ranking,')
    print('                             it should be set to "Y". If including the non-direct drugs is desired should be set to "N"')
    print('        only_approved_drugs: If only approved drugs should be considered in the ranking,')
    print('                             it should be set to "Y". If including all approved and unapproved drugs is desired should be set to "N"')
    print(' ')


# =============================================================================
def check_input_style(input_list):
    try:
        network_file = input_list[1]
        seeds_file = input_list[2]
    # if no input is given, print out a usage message and exit
    except:
        print_usage()
        sys.exit(0)
        return

    outfile_name = 'closeness_ranked.txt'
    only_direct_drugs = True
    only_approved_drugs = True

    if len(input_list) == 5:
        try:
            outfile_name = 'closeness_ranked.txt'
            if input_list[len(input_list) - 2] == "Y":
                only_direct_drugs = True
            elif input_list[len(input_list) - 2] == "N":
                only_direct_drugs = False
            if input_list[len(input_list) - 1] == "Y":
                only_approved_drugs = True
            elif input_list[len(input_list) - 1] == "N":
                only_approved_drugs = False
        except:
            # outfile_name = input_list[2]
            print_usage()
            sys.exit(0)
            return

    if len(input_list) == 6:
        try:
            outfile_name = input_list[3]
            if input_list[len(input_list) - 2] == "Y":
                only_direct_drugs = True
            elif input_list[len(input_list) - 2] == "N":
                only_direct_drugs = False
            if input_list[len(input_list) - 1] == "Y":
                only_approved_drugs = True
            elif input_list[len(input_list) - 1] == "N":
                only_approved_drugs = False
        except:
            print_usage()
            sys.exit(0)
            return
    return network_file, seeds_file, outfile_name, only_direct_drugs, only_approved_drugs


# =============================================================================
def read_input(network_file, seed_file, only_direct_drugs, only_approved_drugs):
    """
    Reads the network and the list of seed proteins from external files.
    * The network must be provided as a graphml or gt format file. Nodes should
    have attribute type. Valid node types are: Protein, Drug.
    Nodes should also have "primaryDomainId" attribute as their unique identifier.
    * The seed proteins mus be provided as a table. If the table has more
    than one column, they must be tab-separated. The first column will
    be used only.
    * Filters the input network file based on the options: only_direct_drugs and
    only_approved_drugs.
    * Lines that start with '#' will be ignored in both cases
    """

    # p_type = "Protein"
    d_type = "Drug"
    # d_type = "drug-approved"
    # pp_type = "ProteinInteractsWithProtein"
    dp_type = "DrugHasTarget"
    node_name_attribute = "primaryDomainId"
    # node_name_attribute = "name"

    # read the seed proteins:
    seeds = set()
    for line in open(seed_file, 'r'):
        # lines starting with '#' will be ignored
        if line[0] == '#':
            continue
        # the first column in the line will be interpreted as a seed
        # protein:
        line_data = line.strip().split('\t')
        seed = line_data[0]
        seeds.add(seed)

    # read the network:
    G = gt.load_graph(network_file)
    G_lcc = gtt.extract_largest_component(G, directed=False, prune=True)

    seed_ids = []
    drug_ids = []
    is_matched = {protein: False for protein in seeds}
    print("Number of nodes in G {} and in G_lcc {}".format(G.num_vertices(), G_lcc.num_vertices()))
    print(G_lcc)

    # Before obtaining seeds and drugs ids, if only_approved_drugs should be included, remove any drug nodes that is not approved
    # Maybe it's better to keep all drugs in, since the interactions exist anyway disragrding user wishes to include/exclude unapproved drugs
    # An approved drug might receive a lower rank if the seed connected to it is also connected to another unapproved drug, compared to the case
    # that the seed is only connected to this approved drug
    # deleted_nodes = []
    # for node in range(G_lcc.num_vertices()):
    #     node_type = G_lcc.vertex_properties["type"][node]
    #     if node_type == d_type:
    #         if only_approved_drugs:
    #             drug_groups = G_lcc.vertex_properties["drugGroups"][node].split(', ')
    #             if "approved" not in drug_groups:
    #                 deleted_nodes.append(node)
    #
    # G_lcc.remove_vertex(deleted_nodes, fast=True)
    # if only_approved_drugs:
    #     print("Number of nodes {} and edges {} in G_lcc after removal of unapproved drugs".format(G_lcc.num_vertices(), G_lcc.num_edges()))

    # obtain seeds and drugs ids
    for node in range(G_lcc.num_vertices()):
        node_type = G_lcc.vertex_properties["type"][node]
        if G_lcc.vertex_properties[node_name_attribute][node] in seeds:
            seed_ids.append(node)
            is_matched[G_lcc.vertex_properties[node_name_attribute][node]] = True
        if node_type == d_type:
            if not only_approved_drugs:
                drug_ids.append(node)
            elif only_approved_drugs:
                drug_groups = G_lcc.vertex_properties["drugGroups"][node].split(', ')
                if "approved" in drug_groups:
                    drug_ids.append(node)

    # Check that all seed seeds have been matched and print the ones not found.
    for protein, found in is_matched.items():
        if not found:
            print("No node named {} found in the network read from {}".format(protein, network_file))
            # raise ValueError("Invalid seed protein {}. No node named {} in {}.".format(protein, protein, network_file))

    # If only_direct_drugs should be included, remove any drug-protein edges that the drug is not a direct neighbor of any seeds
    deleted_edges = []
    if only_direct_drugs:
        direct_drugs = []
        for edge in G_lcc.edges():
            if G_lcc.edge_properties["type"][edge] == dp_type and (edge.target() in seed_ids or edge.source() in seed_ids):
                if G_lcc.vertex_properties["type"][edge.target()] == d_type:
                    direct_drugs.append(edge.target())
                elif G_lcc.vertex_properties["type"][edge.source()] == d_type:
                    direct_drugs.append(edge.source())
        for edge in G_lcc.edges():
            if G_lcc.edge_properties["type"][edge] == dp_type and (edge.target() not in direct_drugs and edge.source() not in direct_drugs):
                deleted_edges.append(edge)

        G_lcc.set_fast_edge_removal(fast=True)
        for edge in deleted_edges:
            G_lcc.remove_edge(edge)
        G_lcc.set_fast_edge_removal(fast=False)

    # with open("/Users/sepideh/Documents/TUM/Python/centralities/drugIDs.txt", 'w') as fout:
    #             fout.write("drug_ids" + '\n')
    #             for dr in drug_ids:
    #                 fout.write(str(dr) + '\n')

    return G_lcc, seeds, seed_ids, drug_ids


# =============================================================================
def closeness(G_lcc, seed_ids, drug_ids, outfile=None):
    """
    Runs the closeness centrality w.r.t. seeds
    Input:
    ------
     - G:
        The network
     - seed_ids:
            a set of seed proteins
     - drug_ids:
            set of all available drugs in the network
     - outfile:
             filename for the output generates by the algorithm,
             if not given the program will name it 'closeness_ranked.txt'
     Returns:
     --------
      - score_map: A map of scores for all drugs in the network
        A sorted map based on the score values is also written in the outfile

      Notes
    -----
    This implementation is based on graph-tool, a very efficient Python package for network
    analysis with C++ backend and multi-threading support. Installation instructions for graph-tool
    can be found at https://git.skewed.de/count0/graph-tool/-/wikis/installation-instructions.

    """

    # Set number of threads if OpenMP support is enabled.
    # if gt.openmp_enabled():
    #     gt.openmp_set_num_threads(num_threads)

    node_name_attribute = "primaryDomainId"
    # node_name_attribute = "name"

    # Call graph-tool to compute closeness centrality.
    all_dists = []
    for node in seed_ids:
        all_dists.append(gtt.shortest_distance(G_lcc, node, weights=weights))
    scores = float(len(seed_ids)) / sum([dists.get_array() for dists in all_dists])

    # returning only scores for drugs:
    score_map = dict()
    for drug in drug_ids:
        score_map[G_lcc.vertex_properties[node_name_attribute][drug]] = scores[drug]

    # sorted_scores = [item[0] for item in sorted(drug_scores, key=lambda item: item[1], reverse=True)]
    # for dr in sorted(score_map, key=score_map.get, reverse=True):
    #     print(dr, score_map[dr])

    # Saving the results (only scores for drug nodes)
    # with open(outfile, 'w') as fout:
    #     fout.write('\t'.join(['drug_name', 'score']) + '\n')
    #     for dr, s in score_map.items():
    #         fout.write('\t'.join([dr, str(s)]) + '\n')

    # Saving the sorted results (only scores for drug nodes)
    with open(outfile, 'w') as fout:
        fout.write('\t'.join(['drug_name', 'score']) + '\n')
        for dr in sorted(score_map, key=score_map.get, reverse=True):
            fout.write('\t'.join([dr, str(score_map[dr])]) + '\n')

    return score_map


# ===========================================================================

if __name__ == '__main__':
    # -----------------------------------------------------
    # Checking for input from the command line:
    # -----------------------------------------------------
    #
    # [1] file providing the network in graphml or gt format
    #     (valid node types: Drug, Protein)
    #
    # [2] file with the seed proteins (if table contains more than one
    #     column they must be tab-separated; the first column will be
    #     used only)
    #
    # [3] (optional) name for the results file
    #
    # [4] Letter "Y" to include only direct drugs in ranking or letter "N"
    #     to include all the drugs
    #
    # [5] Letter "Y" to include only approved drugs in ranking or letter "N"
    #     to include all the drugs

    # check if input style is correct
    input_list = sys.argv
    network_file, seeds_file, outfile_name, only_direct_drugs, only_approved_drugs = check_input_style(input_list)
    print('Network file: ' + network_file)
    print('Seed file:' + seeds_file)
    print('Output file: ' + outfile_name)
    print('Only direct drugs: ' + str(only_direct_drugs))
    print('Only approved drugs: ' + str(only_approved_drugs))

    # read the network and the seed proteins:
    G_lcc, seeds, seed_ids, drug_ids = read_input(network_file, seeds_file, only_direct_drugs, only_approved_drugs)
    weights = G_lcc.new_edge_property("double", val=1.00000)

    # run closeness
    drug_scores = closeness(G_lcc, seed_ids, drug_ids, outfile=outfile_name)

    print("\n results have been saved to '%s' \n" % outfile_name)
