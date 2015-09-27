__author__ = 'Kevin'

import unittest
import Tron
import Minmax

class Test(unittest.TestCase):
    def test_weightings(self):
        driver = Minmax.MinmaxDriver()
        us, them = (0, 0), (0, 4)
        board = Tron.TronBoard(10, 10, us, them)
        move = driver.next(board)
        self.assertEqual((0, 1), move)

    def test_weightings_2(self):
        driver = Minmax.MinmaxDriver()
        us, them = (8, 8), (8, 9)
        board = Tron.TronBoard(10, 10, us, them)
        board.move(board.THEM, (9, 9))
        move = driver.next(board)
        self.assertEqual((9, 8), move)

    def test_example_game_2(self):
        driver = Minmax.MinmaxDriver()
        us, them = (6, 5), (4, 3)
        board = Tron.TronBoard(10, 10, us, them)
        board._tails[1] = [(5, 6), (5, 7), (5, 8), (4, 8), (3, 8), (2, 8), (2, 7), (2, 6), (2, 5), (2, 4), (2, 3), (2, 2), (3, 2), (4, 2), (5, 2), (5, 3), (5, 4), (4, 4)]
        board._tails[0] = [(6, 4), (6, 5)]
        all_points = board._tails[0] + board._tails[1]
        for p in all_points:
            board._free[board.to_vertex(p)] = False
        move = driver.next(board)
        self.assertEqual((6, 5), move)

    def test_first_move(self):
        driver = Minmax.MinmaxDriver()
        us, them = (4, 4), (10, 4)
        should_move = (5, 4)
        board = Tron.TronBoard(20, 20, us, them)
        # board.move(board.THEM, (4, 1))
        move = driver.next(board)
        self.assertEqual(should_move, move)

    def test_example_game(self):
        driver = Minmax.MinmaxDriver()
        us, them = (15, 3), (2, 4)
        board = Tron.TronBoard(30, 20, us, them)
        their_moves = [(3, 4), (4, 4), (5, 4), (6, 4), (7, 4)]
        for m in their_moves:
            our_next = driver.next(board)

            board.move(board.US, our_next)
        return

