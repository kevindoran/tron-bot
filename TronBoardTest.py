__author__ = 'Kevin'

import unittest
from Tron import TronBoard
import Graphs

class TestInit(unittest.TestCase):
    def setUp(self):
        self.height, self.width = 10, 10
        self.us, self.them = (0, 4), (5, 8)
        self.board = TronBoard(self.width, self.height, self.us, self.them)

    def test_init(self):
        self.assertFalse(self.board.free(self.us) or self.board.free(self.them))
        self.assertEqual(self.us, self.board.our_pos())
        self.assertEqual(self.them, self.board.their_pos())

    def test_move(self):
        pos = (1, 4)
        self.board.move(self.board.US, pos)
        # Make sure it moved us.
        self.assertTrue(pos, self.board.our_pos())
        # Make sure it didn't move them.
        self.assertTrue(self.them, self.board.their_pos())
        self.assertFalse(self.board.free(pos))
        pos2 = (2, 4)
        self.board.move(self.board.US, pos2)
        self.assertEqual(pos2, self.board.our_pos())
        self.assertEqual(self.them, self.board.their_pos())
        self.assertFalse(self.board.free(pos) or self.board.free(pos2) \
                         or self.board.free(self.them))

    def test_neighbours(self):
        our_neighbours = {(0,5), (0,3), (1, 4)}
        their_neighbours = {(5, 9), (5, 7), (4, 8), (6, 8)}
        corner = (0, 0)
        corner_neighbours = {(0, 1), (1, 0)}
        self.assertEqual(our_neighbours, self.board.neighbours(self.us))
        self.assertEqual(their_neighbours, self.board.neighbours(self.them))
        self.assertEqual(corner_neighbours, self.board.neighbours(corner))

    def test_to_vertex(self):
        our_vertex = 40
        their_vertex = 85
        zero = 0
        end = 99
        self.assertEqual(our_vertex, self.board.to_vertex(self.us))
        self.assertEqual(their_vertex, self.board.to_vertex(self.them))
        self.assertEqual(zero, self.board.to_vertex((0, 0)))
        self.assertEqual(end, self.board.to_vertex((9, 9)))

    def test_to_graph(self):
        g = self.board.to_graph()
        neighbour_v = [self.board.to_vertex(pos) for pos in self.board.neighbours(self.us)]
        self.assertEqual(set(neighbour_v), set(g.adj(self.board.to_vertex(self.us))))
        paths = Graphs.Paths(g, self.board.to_vertex(self.us))
        for x in range(10):
            for y in range(10):
                if x == self.them[0] and y == self.them[1]:
                    self.assertFalse(paths.hasPathTo(self.board.to_vertex((x, y))))
                else:
                    self.assertTrue(paths.hasPathTo(self.board.to_vertex((x, y))))

    def test_is_safe(self):
        self.assertFalse(self.board.is_safe(self.us) or self.board.is_safe(self.them))
        for n in self.board.neighbours(self.us):
            self.assertTrue(self.board.is_safe(n))

    def test_last_pos(self):
        pos_2 = (0, 5)
        self.board.move(self.board.US, pos_2)
        self.assertEqual(self.us, self.board.last_pos(self.board.US))

    def test_move_made(self):
        pos_2 = (0, 5)
        pos_name = 'DOWN'
        self.board.move(self.board.US, pos_2)
        self.assertEqual(pos_name, self.board.move_made(self.us, pos_2))

    def test_last_move(self):
        pos_2 = (0, 3)
        self.board.move(self.board.US, pos_2)
        self.assertEqual('UP', self.board.last_move())
        pos_3 = (1, 3)
        self.board.move(self.board.US, pos_3)
        self.assertEqual('RIGHT', self.board.last_move())

    def test_set_not_free(self):
        pos = (0, 5)
        self.board.set_not_free(pos)
        self.assertFalse(self.board.free(pos))

if __name__ == '__main__':
    unittest.main()
