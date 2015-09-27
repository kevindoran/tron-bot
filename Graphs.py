__author__ = 'Kevin'

from queue import *

""" Tracks paths from a graph's vertex."""
class Paths(object):
    def __init__(self, graph, source):
        self.edgeTo = [None for v in range(graph.v)]
        self.marked = [False for v in range(graph.v)]
        self.source = source
        self.dfs(graph, source)

    def dfs(self, graph, source):
        self.marked[source] = True
        for adj in graph.adj(source):
            if not self.marked[adj]:
                self.edgeTo[adj] = source
                self.dfs(graph, adj)

    def hasPathTo(self, vertex):
        return self.marked[vertex]

    def pathTo(self, vertex):
        if not self.hasPathTo(vertex):
            return []
        path = []
        v = vertex
        while v != self.source:
            path.append(v)
            v = self.edgeTo[v]
        path.append(self.source)
        return path

""" Same as Paths but with a faster pathTo method. """
class PathsMem(object):
    def __init__(self, graph, source):
        self.paths = [[] for v in range(graph.v)]
        self.dfs(graph, source, [])

    def dfs(self, graph, source, path):
        path.append(source)
        self.paths[source] = list(path[::-1])
        for v in graph.adj(source):
            if self.paths[v] == []:
                self.dfs(graph, v, path)
        path.pop()

    def hasPathTo(self, vertex):
        return  self.paths[vertex] != []

    def pathTo(self, vertex):
        return self.paths[vertex]

""" Paths found are shortest paths, but algorithms uses more memory."""
class BreadthFirstPaths(object):
    def __init__(self, graph, source):
        self.edgeTo = [None for v in range(graph.v)]
        self.marked = [False for v in range(graph.v)]
        self.source = source
        self.bfs(graph, source)

    def bfs(self, graph, source):
        queue = Queue()
        queue.put(source)
        self.marked[source] = True
        while not queue.empty():
            v = queue.get()
            for adjQ in graph.adj(v):
                if not self.marked[adjQ]:
                    self.edgeTo[adjQ] = v
                    self.marked[adjQ] = True
                    queue.put(adjQ)

    def hasPathTo(self, vertex):
        return self.marked[vertex]

    def pathTo(self, vertex):
        if not self.hasPathTo(vertex):
            return []
        path = []
        v = vertex
        while v != self.source:
            path.append(v)
            v = self.edgeTo[v]
        path.append(self.source)
        return path

""" Determines connected components of a graph. """
class CC(object):
    def __init__(self, graph):
        self._id = [None for v in range(graph.v)]
        self._size = [0]
        self.count = 0
        for v in range(graph.v):
            if self._id[v] is None:
                # Currently, algorithm is hard-coded to depth first search.
                self.dfs_cc(graph, v)
                self.count += 1
                self._size.append(0)

    def dfs_cc(self, graph, v):
        self._id[v] = self.count
        self._size[self.count] += 1
        for adj in graph.adj(v):
            if self._id[adj] is None:
                self.dfs_cc(graph, adj)

    def bfs_cc(self, graph, v):
        self._id[v] = self.count
        q = Queue()
        q.put(v)
        while not q.empty():
            v = q.get()
            for adj in graph.adj(v):
                if self._id[adj] is None:
                    self._id[adj] = self.count
                    q.put(adj)

    def connected(self, v, w):
       return self.id(v) == self.id(w)

    def id(self, v):
        return self._id[v]

    def size(self, cc):
        return self._size[cc]

    def size_of_cc_with(self, v):
        return self.size(self.id(v))