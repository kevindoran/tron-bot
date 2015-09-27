__author__ = 'Kevin'

import unittest
import Graphs as G
from Graph import Graph

class GraphsTest(unittest.TestCase):
    graph_string = "7\n(0, 1), (0, 2), (0, 5), (1, 2), (2, 3), " \
                   "(2, 4), (3, 5), (3, 4)"

    def test_depth_first_search(self):
        g = Graph.from_string(self.graph_string)
        dfs = G.DepthFirstSearch(g, 0)
        connected = 6
        self.assertEqual(connected, dfs.count)
        self.assertTrue(dfs.marked(0) and dfs.marked(1) and dfs.marked(2) \
                        and dfs.marked(3) and dfs.marked(4) and dfs.marked(5))
        self.assertFalse(dfs.marked(6))

    def test_has_paths(self):
        g = Graph.from_string(self.graph_string)
        p_finder_mem = G.PathsMem(g, 0)
        p_finder = G.Paths(g, 0)
        breadth_first_p_finder = G.BreadthFirstPaths(g, 0)
        path_finders = [p_finder, p_finder_mem, breadth_first_p_finder]
        for p in path_finders:
            self.assertTrue(p.hasPathTo(0) and p.hasPathTo(1) \
                            and p.hasPathTo(2) \
                            and p.hasPathTo(3) \
                            and p.hasPathTo(4) \
                            and p.hasPathTo(5))
            self.assertFalse(p.hasPathTo(6))

    def test_paths(self):
        g = Graph.from_string(self.graph_string)
        p_finder_mem = G.PathsMem(g, 0)
        p_finder = G.Paths(g, 0)
        path_finders = [p_finder, p_finder_mem]
        path_to_0 = [0]
        path_to_1 = [1, 0]
        path_to_2 = [2, 1, 0]
        path_to_3 = [3, 2, 1, 0]
        path_to_4 = [4, 3, 2, 1, 0]
        path_to_5 = [5, 3, 2, 1, 0]
        path_to_6 = []
        for p in path_finders:
            self.assertEqual(path_to_0, p.pathTo(0))
            self.assertEqual(path_to_1, p.pathTo(1))
            self.assertEqual(path_to_2, p.pathTo(2))
            self.assertEqual(path_to_3, p.pathTo(3))
            self.assertEqual(path_to_4, p.pathTo(4))
            self.assertEqual(path_to_5, p.pathTo(5))
            self.assertEqual(path_to_6, p.pathTo(6))

    def test_breadth_first_paths(self):
        g = Graph.from_string(self.graph_string)
        path_finder = G.BreadthFirstPaths(g, 0)
        path_to = [[0],[1, 0],[2, 0],[3, 2, 0],[4, 2, 0],[5, 0],[]]
        for i in range(len(path_to)):
            self.assertEqual(path_to[i], path_finder.pathTo(i))

    def test_CC(self):
        g = Graph.from_string(self.graph_string)
        cc = G.CC(g)
        self.assertTrue(cc.connected(1, 0))
        self.assertTrue(cc.connected(2, 0))
        self.assertTrue(cc.connected(3, 0))
        self.assertTrue(cc.connected(2, 3))
        self.assertFalse(cc.connected(0, 6))
        self.assertFalse(cc.connected(5, 6))
        self.assertTrue(cc.connected(6, 6))
        self.assertEqual(0, cc.id(0))
        self.assertEqual(0, cc.id(1))
        self.assertEqual(1, cc.id(6))
        self.assertEqual(6, cc.size(0))
        self.assertEqual(1, cc.size(1))
        self.assertEqual(6, cc.size_of_cc_with(1))
        self.assertEqual(1, cc.size_of_cc_with(6))

if __name__ == '__main__':
    unittest.main()
