import sys
import math
import argparse
from typing import Dict, Tuple, List
import networkx as nx
from cartoGRAPHs import generate_layout, layout_topographic, layout_geodesic
from graph_tool.all import load_graph, sfdp_layout

class NetworkLayouter:
    LAYOUTS = ["portrait", "topographic_x", "topographic_y", "geodesic", "geodesic_x", "geodesic_y"]

    def __init__(self, graphml_path: str, layout_type: str):
        self.graphml_path = graphml_path
        if layout_type not in self.LAYOUTS:
            raise ValueError(f"Invalid layout. Please choose from: {', '.join(self.LAYOUTS)}")
        self.layout_type = layout_type

        # Load the graph in both formats we need
        self.g_tool = load_graph(graphml_path)
        self.g_nx = nx.read_graphml(graphml_path)

        # Create mapping between vertex IDs and domain IDs
        self.domain_map = {
            self.g_tool.properties[('v', "primaryDomainId")][v]: v
            for v in self.g_tool.vertices()
        }

    def compute_cartograph_layout(self) -> Dict[str, Tuple[float, float]]:
        """Compute the initial layout using cartoGRAPHs."""
        if self.layout_type == "portrait":
            pos2D = generate_layout(
                self.g_nx,
                dim=2,
                layoutmethod='global',
                dimred_method='umap'
            )
            return {k: (v[0], v[1]) for k, v in pos2D.items()}

        elif "topographic" in self.layout_type:
            pos2D = generate_layout(
                self.g_nx,
                dim=2,
                layoutmethod='global',
                dimred_method='umap'
            )
            degree_cent = dict(nx.degree_centrality(self.g_nx))
            z_values = dict(zip(self.g_nx.nodes(), degree_cent.values()))
            pos_topo = layout_topographic(pos2D, z_values)

            if "x" in self.layout_type:
                return {k: (v[0], v[2]) for k, v in pos_topo.items()}
            else:  # y
                return {k: (v[1], v[2]) for k, v in pos_topo.items()}

        elif "geodesic" in self.layout_type:
            degree_cent = dict(nx.degree_centrality(self.g_nx))
            rad_values = {k: (1-v) for k, v in degree_cent.items()}
            pos_geo = layout_geodesic(self.g_nx, rad_values)

            if "x" in self.layout_type:
                return {k: (v[0], v[2]) for k, v in pos_geo.items()}
            elif "y" in self.layout_type:
                return {k: (v[1], v[2]) for k, v in pos_geo.items()}
            else:
                return {k: (v[0], v[1]) for k, v in pos_geo.items()}

    def refine_with_sfdp(self, initial_layout: Dict[str, Tuple[float, float]]) -> Dict[int, Tuple[float, float]]:
        """Refine the layout using graph-tool's SFDP layout."""
        # Initialize properties
        pos = sfdp_layout(self.g_tool, C=0.15, p=1.5, r=2, K=6)
        pin = self.g_tool.new_vertex_property("boolean")

        # Scale and set initial positions
        scaled_layout = {
            k: ((v[0]-0.5)*-100, (v[1]-0.5)*-100)
            for k, v in initial_layout.items()
        }

        # Pin vertices from the initial layout
        for domain_id, coords in scaled_layout.items():
            vertex = self.g_tool.vertex(self.domain_map[domain_id])
            pos[vertex] = coords
            pin[vertex] = True

        # Compute final layout
        return sfdp_layout(
            self.g_tool,
            C=0.15,
            p=1.5,
            r=2,
            K=6,
            pos=pos,
            pin=pin
        )

    def compute_scale_factor(self) -> float:
        """Compute scaling factor based on number of nodes."""
        return 10 ** int(math.log10(self.g_tool.num_vertices())) * 5

    def save_layout(self, final_pos, output_path: str):
        """Save the layout to file."""
        scale = self.compute_scale_factor()

        with open(output_path, 'w') as fh:
            for v in self.g_tool.vertices():
                fh.write(f"{self.g_tool.properties[('v', 'type')][v]}\t"
                        f"{self.g_tool.properties[('v', 'primaryDomainId')][v]}\t"
                        f"{final_pos[v][0] * scale}\t"
                        f"{final_pos[v][1] * scale}\n")

def main():
    if len(sys.argv) != 4:
        print("Usage: python script.py graphml_file nodes_file edges_file output_file layout_type")
        print(f"Available layouts: {', '.join(NetworkLayouter.LAYOUTS)}")
        sys.exit(1)

    graphml_file = sys.argv[1]
    output_file = sys.argv[2]
    layout_type = sys.argv[3]

    try:
        layouter = NetworkLayouter(graphml_file, layout_type)
        initial_layout = layouter.compute_cartograph_layout()
        final_positions = layouter.refine_with_sfdp(initial_layout)
        layouter.save_layout(final_positions, output_file)

    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()