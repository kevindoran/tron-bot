__author__ = 'Kevin'

import sys
import math

MAX_WIDTH = 30
MAX_HEIGHT = 20

moves = [(1,0,'RIGHT'), (0,-1,'UP'),(-1,0,'LEFT'), (0,1,'DOWN')]
tails = []
tails_map = {}
currentPos =0
startup = True

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

# game loop
while 1:
    # Choose a default next move.
    nextMove = 'LEFT'
    # N: total number of players (2 to 4).
    # P: your player number (0 to 3).
    N, P = [int(i) for i in input().split()]
    for i in range(N):
        # X0: starting X coordinate of lightcycle (or -1)
        # Y0: starting Y coordinate of lightcycle (or -1)
        # X1: starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
        # Y1: starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)
        X0, Y0, X1, Y1 = [int(j) for j in input().split()]
        if startup:
            tails.append([(X0, Y0)])
            if i == N:
                position = (X0, Y0)
        tails[i].append((X1, Y1))
        tails_map[(X1, Y1)] = True
        if X0 == -1 and Y0 == -1:
            tails[i] = []
        # Choose a default next move.
        if i == P:
            # Me
            currentPos = (X1, Y1)

    for m in moves:
        nextPos = (m[0] + currentPos[0], m[1] + currentPos[1])
        if isSafe(nextPos, tails):
            nextMove = m[2]
            break;
    # Write an action using print
    # To debug: print("Debug messages...", file=sys.stderr)

    # A single line with UP, DOWN, LEFT or RIGHT
    print(nextMove)
    startup = False