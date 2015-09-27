__author__ = 'Kevin'
from Graphs import BreadthFirstPaths
from Graphs import CC
from Graph import Graph
from Tron import Possibility
from AvoidDriver import AvoidDriver

""" Minimax tron driver. """
class MinmaxDriver(object):
    def __init__(self):
        None
        self.backup_driver = AvoidDriver()

    def next(self, board):
        depth = 3
        p = Possibility(board, None, depth)
        max_score = -100
        best_child = None
        for c in p.children:
            weighting = self.choice_weighting(c)
            if weighting >= max_score:
                best_child = c
                max_score = weighting
        next_pos = best_child.board.our_pos()
        if not board.is_safe(next_pos):
            next_pos = self.backup_driver.next(board)
        return next_pos

    def choice_weighting(self, p):
        paths = BreadthFirstPaths(p.board.to_graph(True), p.board.our_vertex())
        pathLen = len(paths.pathTo(p.board.their_vertex()))
        if pathLen > 4:
            return - pathLen
        elif p.children == []:
            return self.weighting(p.board)
        else:
            child_weightings = [self.choice_weighting(c) for c in p.children]
            if p.board.turn == p.board.THEM:
                return max(child_weightings)
            else:
                return min(child_weightings)

    # Scoring: win 100, loss = -100, draw = -50,
    # disconnected (diff * factor) + 50
    # connected 50 - distance
    # us & opponent: vertex number
    def weighting(self, board):
        graph = board.to_graph(board.US)
        weighting = None
        if board.state == board.WON:
            weighting = 100
        elif board.state == board.LOST:
            weighting = -100
        elif board.state == board.DRAW:
            weighting = -50
        else:
            us = board.our_vertex()
            them = board.their_vertex()
            paths = BreadthFirstPaths(graph, us)
            if paths.hasPathTo(them):
                weighting = 50 - len(paths.pathTo(them))
            else:
                # Connected components
                cc = CC(graph)
                ourRoom = cc.size_of_cc_with(board.US)
                theirRoom = cc.size_of_cc_with(board.THEM)
                diff = ourRoom - theirRoom
                weighting = 50 + (diff * 100) // (board.WIDTH * board.HEIGHT)
        return weighting

