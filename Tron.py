__author__ = 'Kevin'
from Graph import Graph
import copy

""" Maintains basic state of a tron game with some useful helping methods. """
class TronBoard(object):
    def __init__(self, width, height, us_pos, them_pos):
        self.WIDTH = width
        self.HEIGHT = height
        self.moves = [(1,0,'RIGHT'), (0,-1,'UP'),(-1,0,'LEFT'), (0,1,'DOWN')]
        self.move_map = {(1,0):'RIGHT', (0,-1):'UP', (-1,0):'LEFT', (0,1): 'DOWN'}
        self.US = 0
        self.THEM = 1
        self._tails = [[],[]]
        self._free = [True for x in range(self.WIDTH*self.HEIGHT)]
        self.WON = 1
        self.LOST = -2
        self.DRAW = -1
        self.IN_PLAY = 0
        self.turn = self.US
        self.state = self.IN_PLAY
        self._tails[self.US].append(us_pos)
        self._tails[self.THEM].append(them_pos)
        self.set_not_free(us_pos)
        self.set_not_free(them_pos)

    def set_not_free(self, pos):
        self._free[self.to_vertex(pos)] = False

    def move(self, who, where):
        # can raise exception if out of range
        self._tails[who].append(where)
        if not self.is_safe(where):
            self.state = self.WON if who == self.THEM else self.LOST
        if self._tails[self.US][-1] == self._tails[self.THEM][-1]:
            self.state = self.DRAW
        # assert here: who = self.turn
        self.set_not_free(where)
        self.turn = not who

    """ Returns the valid neighboring positions of the input position. Valid
    positions are those that are inside the board. They may be unsafe positions.
    """
    def neighbours(self, pos):
        x, y = pos[0], pos[1]
        neighbours = set()
        for n in [(x+1, y), (x-1, y), (x, y+1), (x, y-1)]:
                if 0 <= n[0] < self.WIDTH and 0 <= n[1] < self.HEIGHT:
                    neighbours.add(n)
        return neighbours

    """ Converts an (x,y) coordinate to a vertex number. Each board square is
    given a unique number counting from the top left towards the bottom right.
    """
    def to_vertex(self, pos):
        return pos[0] + pos[1] * self.WIDTH

    """ Generates a graph of all free space on the board. """
    def to_graph(self, who):
        graph = Graph(self.WIDTH * self.HEIGHT)
        for x in range(0, self.WIDTH):
            for y in range(0, self.HEIGHT):
                if self.free((x, y)) or (x, y) == self.our_pos():
                    for n in self.neighbours((x, y)):
                        if self.free(n) or self.pos(who) == n:
                            graph.add_edge(self.to_vertex(n),
                                           self.to_vertex((x,y)))
        return graph

    def free(self, pos):
        return self._free[self.to_vertex(pos)]

    def is_safe(self, pos):
        # Check if pos is out of bounds.
        if pos[0] >= self.WIDTH or pos[0] < 0 or pos[1] >= self.HEIGHT or pos[1] < 0:
            return False
        elif not self.free(pos):
            return False
        return True

    def our_pos(self):
        return self._tails[self.US][-1]

    def our_vertex(self):
        return self.to_vertex(self.our_pos())

    def their_vertex(self):
        return self.to_vertex(self.their_pos())

    def their_pos(self):
        return self._tails[self.THEM][-1]

    def pos(self, who):
        return self._tails[who][-1]

    def last_pos(self, who):
        last_pos = self._tails[who][-2] if len(self._tails[who]) > 1 else None
        return last_pos

    def move_made(self, pos_0, pos_1):
        diff = (pos_1[0] - pos_0[0], pos_1[1] - pos_0[1])
        return self.move_map[diff]

    def last_move(self):
        return self.move_made(self.last_pos(not self.turn), self.pos(not self.turn))

class Possibility(object):
    def __init__(self, board, parent, depth):
        self.board = board
        self.parent = parent
        self.children = []
        self.depth = depth
        self.generate_next()

    def generate_next(self):
        if self.depth > 1:
            next_board = None
            for n in self.board.neighbours(self.board.pos(self.board.turn)):
                if next_board is not None and not self.board.free(n):
                    continue
                next_board = copy.deepcopy(self.board)
                next_board.move(next_board.turn, n)
                possible_child = Possibility(next_board, self, self.depth -1)
                self.children.append(possible_child)

    # def leaves(self, depth=0):
    #     depth = self.depth if depth == 0 else depth
    #     leaves = []
    #     if depth == 1:
    #         return self.children
    #     for c in self.children:
    #         leaves.extend(c.leaves(depth-1))
