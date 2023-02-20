import networkx as nx
import numpy as np
from sklearn.cluster import KMeans
import random
import sys
import pandas as pd
import time
import argparse

measures = [
    (nx.pagerank, "pageRank"),
    (nx.degree_centrality, "degreeCentrality"),
    (nx.in_degree_centrality, "inDegreeCentrality"),
    (nx.out_degree_centrality, "outDegreeCentrality"),
    (nx.betweenness_centrality, "betweennessCentrality"),
    # (nx.eigenvector_centrality, "eigenvectorCentrality"),
    # (nx.katz_centrality, "katzCentrality")
]


def log(msg):
    print(f"****Analyzer**** {msg}")


def generate_random_color():
    color = "%06x" % random.randint(0, 0xFFFFFF)  # it is also possible to generate a triplet of 0..255 ints
    return f"#{color}"


def load_graph(path):
    log(f"Loading graph from {path}")
    results = []  # never used
    with open(path) as f:
        data_frame = pd.read_csv(f)
        return nx.from_pandas_edgelist(data_frame, source="source", target="destination", create_using=nx.DiGraph())


def create_feature_vector(graph):
    log("Creating feature vector")

    features = np.zeros((len(graph.nodes), len(measures)))
    nodes = list(graph.nodes)
    for i in range(len(measures)):
        scores = measures[i][0](graph)
        for j in range(len(nodes)):
            features[j][i] = scores[nodes[j]]

    names = [m[1] for m in measures]

    return features, names


def cluster_nodes(features):
    log("Clustering nodes")

    kmeans = KMeans(n_clusters=2, random_state=0).fit(features)
    # kmeans doesn't have outliers, so you can generate a color for each cluster
    color_labels = [generate_random_color() for _ in range(2)]
    color_map = [color_labels[l] for l in kmeans.labels_]

    return color_map


def print_banner():
    print("""    ____                            __                         ___                __                     
       / __ \___  ____  ___  ____  ____/ /__  ____  _______  __   /   |  ____  ____ _/ /_  ______  ___  _____
      / / / / _ \/ __ \/ _ \/ __ \/ __  / _ \/ __ \/ ___/ / / /  / /| | / __ \/ __ `/ / / / /_  / / _ \/ ___/
     / /_/ /  __/ /_/ /  __/ / / / /_/ /  __/ / / / /__/ /_/ /  / ___ |/ / / / /_/ / / /_/ / / /_/  __/ /    
    /_____/\___/ .___/\___/_/ /_/\__,_/\___/_/ /_/\___/\__, /  /_/  |_/_/ /_/\__,_/_/\__, / /___/\___/_/     
              /_/                                     /____/                        /____/                   """)


if __name__ == "__main__":

    print_banner()
    # Parsing parameters

    """
    when fewer than 4 parameters are passed it will fall with IndexError exception. So you need to check beforehand how 
    many were passed and exit the code if necessary. Also the are no check for the values other than not None.
    Also, I suggest considering using argparse library, I'll leave commented example below
    
    
    parser = argparse.ArgumentParser(description="dependency graph evaluator")

    parser.add_argument("-g", "--graph_file_path", type=str, help="path to the graph", required=True)
    parser.add_argument("-i", "--output_image_path", type=str, help="path to output images", required=True)
    parser.add_argument("-f", "--output_features_path", type=str, help="path to output features", required=True)
    args = parser.parse_args()

    graph_file_path = args.graph_file_path
    output_image_path = args.output_image_path
    output_features_path = args.output_features_path
    
    
    """

    graph_file_path = sys.argv[1]
    if graph_file_path is None:
        log("Please enter graph file path")
        exit(1120)

    output_image_path = sys.argv[2]
    if output_image_path is None:
        log("Please enter output image file path")
        exit(1121)

    output_features_path = sys.argv[3]
    if output_features_path is None:
        log("Please enter output features file path")
        exit(1122)

    graph = load_graph(graph_file_path)

    t = time.time()
    features, feature_names = create_feature_vector(graph)
    log(f"feature extraction time is {time.time() - t}")
    # color_map = cluster_nodes(features)

    # nx.draw(graph, node_color=color_map, with_labels=True)
    # # plt.show()
    # log(f"Output result diagram to {output_image_path}")
    # plt.savefig(output_image_path)

    log(f"Output features to {output_features_path}")
    df = pd.DataFrame(features, columns=feature_names, index=list(graph.nodes))
    df.to_csv(output_features_path)
