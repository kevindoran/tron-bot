import sys
import math
import operator
# Graph.py
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

# Tron board
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
            for n in self.board.neighbours(self.board.pos(self.board.turn)):
                if not self.board.free(n):
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

# Graphs.py
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



## end Minmax.py

def is_tail(x, y):
    return tails_map[(x,y)]

def isSafe(pos, tails):
    # Check if pos is out of bounds.
    if pos[0] >= 30 or pos[0] < 0 or pos[1] >= 20 or pos[1] < 0:
            return False
    for tail in tails:
        for tailPos in tail:
            if pos == tailPos:
                return False
    return True

class AvoidDriver(object):
    def next(self, board):
        next_move = (board.our_pos()[0] + 1, board.our_pos()[1] + 0)
        for n in board.neighbours(board.our_pos()):
            if board.is_safe(n):
                next_move = n
                break
        return next_move

MAX_WIDTH = 30
MAX_HEIGHT = 20

moves = [(1,0,'RIGHT'), (0,-1,'UP'),(-1,0,'LEFT'), (0,1,'DOWN')]
tails = []
tails_map = {}
currentPos =0
startup = True
driver = MinmaxDriver() #MinmaxDriver()
board = None
# game loop
while 1:
    # N: total number of players (2 to 4).
    # P: your player number (0 to 3).
    N, P = [int(i) for i in input().split()]
    my_pos, their_pos = None, None
    my_start, their_start = None, None
    for i in range(N):
        # X0: starting X coordinate of lightcycle (or -1)
        # Y0: starting Y coordinate of lightcycle (or -1)
        # X1: starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
        # Y1: starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)
        X0, Y0, X1, Y1 = [int(j) for j in input().split()]
        if i == P:
            # Me
            my_start = (X0, Y0)
            my_pos = (X1, Y1)
        else:
            their_start = (X0, Y0)
            their_pos = (X1, Y1)
    if startup:
        board = TronBoard(MAX_WIDTH, MAX_HEIGHT, my_start, their_start)
        startup = False

    if startup and P == 0:
        # I go first. Opponent hasn't moved yet.
        None
    else:
        board.move(board.THEM, their_pos)

    next_pos = driver.next(board)
    board.move(board.US, next_pos)
    print(board.last_move())

  