__author__ = 'Kevin'

class AvoidDriver(object):
    def next(self, board):
        next_move = (board.our_pos()[0] + 1, board.our_pos()[1] + 0)
        for n in board.neighbours(board.our_pos()):
            if board.is_safe(n):
                next_move = n
                break
        return next_move


