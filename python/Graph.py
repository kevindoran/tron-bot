__author__ = 'Kevin'

""" Basic graph data structure. """
class Graph(object):

    def __init__(self, vertices_count):
        self.v = vertices_count
        self.e = 0
        self._adj = []
        for i in range(self.v):
            self._adj.append([])

    def add_edge(self, v, w):
        self._adj[v].append(w)
        self._adj[w].append(v)
        self.e += 1

    def adj(self, vertex):
        return self._adj[vertex]
