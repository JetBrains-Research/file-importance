import json
import networkx as nx
import matplotlib.pyplot as plt
import numpy as np
from sklearn.cluster import KMeans
import random
import sys


def log(log):
    print(f"****Analyzer**** {log}")


def generate_random_color():
    color = "%06x" % random.randint(0, 0xFFFFFF)
    return f"#{color}"


def load_graph(path):
    log(f"Loading graph from {path}")
    results = []
    with open(path) as f:
        jsonData = json.load(f)
        for jsonEdge in jsonData:
            results += [(jsonEdge['source'], jsonEdge['destination'])]

    graph = nx.DiGraph()
    graph.add_edges_from(results)
    return graph


def create_feature_verctor(graph):
    log("Creating feature vector")
    measures = [
        nx.pagerank,
        nx.degree_centrality,
        nx.in_degree_centrality,
        nx.out_degree_centrality,
        nx.betweenness_centrality,
        nx.eigenvector_centrality,
        nx.katz_centrality
    ]

    features = np.zeros((len(graph.nodes), len(measures)))
    nodes = list(graph.nodes)
    for i in range(len(measures)):
        scores = measures[i](graph)
        for j in range(len(nodes)):
            features[j][i] = scores[nodes[j]]

    return features


def cluster_nodes(features):
    log("Clustering nodes")
    kmeans = KMeans(n_clusters=2, random_state=0).fit(features)
    color_labels = [generate_random_color() for l in np.unique(kmeans.labels_)]
    color_map = [color_labels[l] for l in kmeans.labels_]

    return color_map


def print_banner():
    print("""    ____                            __                         ___                __                     
       / __ \___  ____  ___  ____  ____/ /__  ____  _______  __   /   |  ____  ____ _/ /_  ______  ___  _____
      / / / / _ \/ __ \/ _ \/ __ \/ __  / _ \/ __ \/ ___/ / / /  / /| | / __ \/ __ `/ / / / /_  / / _ \/ ___/
     / /_/ /  __/ /_/ /  __/ / / / /_/ /  __/ / / / /__/ /_/ /  / ___ |/ / / / /_/ / / /_/ / / /_/  __/ /    
    /_____/\___/ .___/\___/_/ /_/\__,_/\___/_/ /_/\___/\__, /  /_/  |_/_/ /_/\__,_/_/\__, / /___/\___/_/     
              /_/                                     /____/                        /____/                   """)


print_banner()
# Parsing parameters
graphFilePath = sys.argv[1]
if graphFilePath is None:
    log("Please enter graph file path")
    exit(1)

outputImagePath = sys.argv[2]
if outputImagePath is None:
    log("Please enter output image file path")
    exit(10)

graph = load_graph(graphFilePath)
features = create_feature_verctor(graph)
color_map = cluster_nodes(features)

nx.draw(graph, node_color=color_map, with_labels=True)
# plt.show()
log(f"Output result diagram to {outputImagePath}")
plt.savefig(outputImagePath)
