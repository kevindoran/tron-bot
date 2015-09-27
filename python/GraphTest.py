__author__ = 'Kevin'

import unittest
from Graph import Graph

class GraphTest(unittest.TestCase):

    def test_init(self):
        vertices = 6
        g = Graph(vertices)
        self.assertEqual(vertices, g.v)
        edges = 0
        self.assertEqual(edges, g.e)

    def test_from_string(self):
        graph_string = "7\n(0, 1), (0, 2), (0, 5), (1, 2), (2, 3), " \
                       "(2, 4), (3, 5), (3, 4)"
        vertices = 7
        edges = 8
        graph  = Graph.from_string(graph_string)
        self.assertEqual(vertices, graph.v)
        self.assertEqual(edges, graph.e)
        self.assertEqual(True, 0 in graph.adj(1) and 1 in graph.adj(0))
        self.assertEqual(False, 7 in graph.adj(1) or 2 in graph.adj(5))


    def test_edge_and_adj(self):
        vertices = 4
        g = Graph(vertices)
        g.add_edge(0, 1)
        self.assertEqual(1, g.e)
        self.assertEqual(True, 1 in g.adj(0))
        self.assertEqual(True, 0 in g.adj(1))
        self.assertEqual(False, 2 in  g.adj(0))

if __name__ == '__main__':
    unittest.main()
