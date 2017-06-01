# HardeningProblem
The project contains code for the Entity Hardening and Targeted Entity Hardening Problem  using a new model of dependency called IIM model (Paper Link: https://arxiv.org/abs/1701.07106). The source contains the following code.
1. For generation of data for power network please refer to https://github.com/jbanerje1989/ContingencyList. The dependency equations for the IIM model are generated using IIRGenerator.java. 
2. The ILP and heuristic solutions to the problems.
3. A driver code that takes in as input the system parameters to generate the ILP and heuristic solutions.

Note: A licensed version of IBM CPLEX optimizer is required for the ILP solution. I used a student license for the same.

