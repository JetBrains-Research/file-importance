import json
import networkx as nx
import matplotlib.pyplot as plt
import scipy as sp
import numpy as np
from sklearn.cluster import KMeans
import random

def generate_random_color():
    color = "%06x" % random.randint(0, 0xFFFFFF)
    return f"#{color}"

edges = []
with open('../shared/graph.json') as f:
    jsonData = json.load(f)
    for jsonEdge in jsonData:
        edges += [(jsonEdge['source'], jsonEdge['destination'])]

graph = nx.DiGraph()
graph.add_edges_from(edges)

measures = [
    nx.pagerank,
    nx.degree_centrality,
    nx.in_degree_centrality,
    nx.out_degree_centrality,
    nx.betweenness_centrality,
    nx.eigenvector_centrality,
    nx.katz_centrality,
    # nx.subgraph_centrality,
    # nx.percolation_centrality
]

features = np.zeros((len(graph.nodes), len(measures)))
nodes = list(graph.nodes)
for i in range(len(measures)):
    scores = measures[i](graph)
    for j in range(len(nodes)):
        features[j][i] = scores[nodes[j]]

kmeans = KMeans(n_clusters=2, random_state=0).fit(features)
color_labels = [generate_random_color() for l in np.unique(kmeans.labels_)]
color_map = [color_labels[l] for l in kmeans.labels_]

nx.draw(graph, node_color=color_map, with_labels=True)
plt.show()
