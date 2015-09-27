__author__ = 'Kevin'

import unittest
import unittest
import Tron
import AvoidDriver

class MyTestCase(unittest.TestCase):
    def test_example(self):
        driver = AvoidDriver.AvoidDriver()
        us, them = (15, 3), (2, 4)
        board = Tron.TronBoard(30, 20, us, them)
        their_moves = [(3, 4), (4, 4), (5, 4), (6, 4), (7, 4)]
        for m in their_moves:
            our_next = driver.next(board)
            board.move(board.US, our_next)
            board.move(board.THEM, m)
        return


if __name__ == '__main__':
    unittest.main()
