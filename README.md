Gossip and Push-Sum
======

Gossip and Push-Sum algorithm in Scala
Gossip type algorithms can be used both for group commu-
nication and for aggregate computation. The goal of this project is to determine
the convergence of such algorithms through a simulator based on actors written
in Scala. Since actors in Scala are fully asynchronous, the particular type of
Gossip implemented is the so called Asynchronous Gossip.

Push-Sum algorithm for sum computation
 State: Each actor Ai maintains two quantities: s and w. Initially, s =
xi = i (that is actor number i has value i, play with other distribution if
you so desire) and w = 1
 Starting: Ask one of the actors to start from the main process.
 Receive: Messages sent and received are pairs of the form (s;w). Upon
receive, an actor should add received pair to its own corresponding val-
ues. Upon receive, each actor selects a random neighboor and sends it a
message.

Topologies The actual network topology plays a critical role in the dissemi-
nation speed of Gossip protocols. The topology determines who is considered a neighboor
in the above algorithms.
 Full Network Every actor is a neighboor of all other actors. That is,
every actor can talk directly to any other actor.
 2D Grid: Actors form a 2D grid. The actors can only talk to the grid
neigboors.
 Line: Actors are arranged in a line. Each actor has only 2 neighboors
(one left and one right, unless you are the rst or last actor).
 Imperfect 2D Grid: Grid arrangement but one random other neighboor
is selected from the list of all actors (4+1 neighboors).
