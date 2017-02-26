import os
os.chdir('/Users/joydeepbanerjee/Dropbox/papers/WorkUnderProgress/ElsevierJournal/Paper Code/HardeningProblem/OutFileForHeuristics/');
InputFile = open('DataSet1HeuristicDataSet1forK15.txt','r');
FailedSet = InputFile.readline().strip().split(" "); # Set of all failed entities
InitialFailureSet = InputFile.readline().strip().split(" "); # Set of initially failed entities
IDR = []; # Dependencies of the entities

while True:
    line = InputFile.readline();
    if line == '': break;
    index = 0;
    leftSideEntity = [];
    minterms= [];
    for val in line.strip().split("   "):
        if index == 0:
            val2 = val.split(" ");
            leftSideEntity.append(val2[0]);
            minterms.append(val2[2].split(" "));
        else :
            minterms.append(val.split(" "))
        index = index + 1;
    IDR.append(leftSideEntity);
    IDR.append(minterms); 

os.chdir('/Users/joydeepbanerjee/Dropbox/papers/WorkUnderProgress/ElsevierJournal/Paper Code/HardeningProblem/src/HardeningCode/');

# Heuristic Code