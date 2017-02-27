package HardeningCode;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import ilog.concert.*;
import ilog.cplex.*;

public class EntityHardeningILP {
	private HashMap<String, Integer> entityLabeltoIndexMap; 
	private HashMap<String, Integer> mintermLabelToIndexMap;
	private HashMap<String, List<String>> IIRs;
	private int K;
	private int DK;
	private int XCOUNT;
	private int CCOUNT;
	private int STEPS;
	private int compDeadInit;
	private String fileName;
	
	// ILP variables
	IloCplex cplex;
	private IloIntVar[][] x;	
	private IloIntVar[][] c;
	private IloIntVar[] qx; // entity hardened array
	private int[] gx; // input from run of K most vulnerable ILP
		
	public EntityHardeningILP(String file, int KVal, int DKVal) {
		try {
			entityLabeltoIndexMap = new HashMap<String, Integer>();
			mintermLabelToIndexMap = new HashMap<String, Integer>();
			IIRs = new HashMap<String, List<String>>();
			File caseFile = new File("OutputFiles/" + file + ".txt");
			Scanner scan = new Scanner(caseFile);
			int cTermIndex = 0;
			int eIndex = 0;
			while(scan.hasNext()){
				String exp = scan.nextLine();
				StringBuilder firstEntity = new StringBuilder();
				int index = 0;
				while(exp.charAt(index) != ' '){
					firstEntity.append(exp.charAt(index));
					index ++;
				}
				if(!entityLabeltoIndexMap.containsKey(firstEntity.toString())){
					entityLabeltoIndexMap.put(firstEntity.toString(), eIndex);
					eIndex ++;
				}
				index ++;
				while(exp.charAt(index) != ' '){
					index ++;
				}
				String[] minterms = exp.substring(index + 1, exp.length()).split("   ");
				for(String str: minterms){
					if(mintermLabelToIndexMap.containsKey(str)) continue;
					mintermLabelToIndexMap.put(str, cTermIndex);
					for(String entity: str.split(" ")){
						if(!entityLabeltoIndexMap.containsKey(entity)){
							entityLabeltoIndexMap.put(entity, eIndex);
							eIndex ++;
						}
					}
					cTermIndex ++;
				}
				IIRs.put(firstEntity.toString(), Arrays.asList(minterms));
			}
			cplex = new IloCplex();
			XCOUNT = entityLabeltoIndexMap.size();
			CCOUNT = mintermLabelToIndexMap.size();
			STEPS = IIRs.size() + 1;
			fileName = file;
			K = KVal;
			DK = DKVal;
			x = new IloIntVar[XCOUNT][STEPS];
			c = new IloIntVar[CCOUNT][STEPS];
			qx = new IloIntVar[XCOUNT];
			gx = new int[XCOUNT];
			scan.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void optimize() {
		try {
			createXVariables();
			createCVariables();
			KVulnerableNodeILP attackerILP = new KVulnerableNodeILP(fileName, K);			
			attackerILP.optimize();
			attackerILP.generateFileForHeuristic();
			gx = attackerILP.getInitialFailureX();
			compDeadInit = attackerILP.getFinalFailureX().size();
			createConstraints();
			createObjective();
			cplex.solve();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createXVariables() {
		try {
			for (int i = 0; i < XCOUNT; i++) {				
				x[i] = cplex.intVarArray(STEPS, 0, 1);	
				qx[i] = cplex.intVar(0, 1);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}

	public void createCVariables() {
		try {
			for (int i = 0; i < CCOUNT; i++) {				
				c[i] = cplex.intVarArray(STEPS, 0, 1);				
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	public void createObjective() {
		try {
			IloIntExpr expr = cplex.constant(0);
			for (int i = 0; i < XCOUNT; i++)
				expr = cplex.sum(expr, x[i][STEPS - 1]);
			cplex.addMinimize(expr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void createConstraints() {
		try {
			// time step constraints	
			IloNumExpr expr = cplex.constant(0);
			for (int i = 0; i < XCOUNT; i++) {
				for (int t = 1; t < STEPS;t++) {
					cplex.addGe(x[i][t], x[i][t-1]);
				}
			}
			
			// Hardening Budget Constraint
			expr = cplex.constant(0);
			for (int i = 0;i < XCOUNT; i++)
				expr = cplex.sum(expr, qx[i]);
			cplex.addLe(expr, DK);	
	
			// If attacked entity is not defended it fails at t=0
			for (int i = 0;i<XCOUNT;i++){				
				cplex.addGe(x[i][0], cplex.diff(gx[i], qx[i]));
			}
		
			// Generating constraints for IIRs
			for(String str: IIRs.keySet()){
				for(String minterms : IIRs.get(str)){
					for(int t = 1; t < STEPS; t++){
						expr = cplex.constant(0);
						for(String entity: minterms.split(" ")){
							cplex.addGe(c[mintermLabelToIndexMap.get(minterms)][t], x[entityLabeltoIndexMap.get(entity)][t-1]);
							expr = cplex.sum(expr, x[entityLabeltoIndexMap.get(entity)][t-1]);
						}
						cplex.addLe(c[mintermLabelToIndexMap.get(minterms)][t], expr);
					}
				}
				
				for(int t = 1; t < STEPS; t++){
					IloNumExpr expr2 = cplex.constant(0);
					IloNumExpr expr3 = cplex.constant(0);
					double minCount = 0;
					for(String minterms : IIRs.get(str)){
						expr2 = cplex.sum(expr2, c[mintermLabelToIndexMap.get(minterms)][t]);
						minCount ++;
					}
					expr3 = cplex.sum(expr3, expr2);
					expr3 = cplex.prod(expr3, 1.0 / minCount);
					expr3 = cplex.sum(expr3, x[entityLabeltoIndexMap.get(str)][0]);
					cplex.addLe(x[entityLabeltoIndexMap.get(str)][t], expr3);
					
					expr2 = cplex.sum(expr2, x[entityLabeltoIndexMap.get(str)][0]);
					expr2 = cplex.diff(expr2, minCount - 1);
					expr2 = cplex.diff(expr2, qx[entityLabeltoIndexMap.get(str)]);
					cplex.addGe(x[entityLabeltoIndexMap.get(str)][t], expr2);
				}
				
			}
			
			// For entities having no dependency relation
			for(String str : entityLabeltoIndexMap.keySet()){
				if(!IIRs.containsKey(str)){
					for (int t = 1; t < STEPS; t++) {
						cplex.addEq(x[entityLabeltoIndexMap.get(str)][t], x[entityLabeltoIndexMap.get(str)][0]);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void printX() {
		try {
			System.out.println();
			System.out.println("\nX: ");
			for(int i = 0; i < XCOUNT; i++) {
				System.out.println();
				for (int j = 0; j < STEPS; j++) {
					System.out.print(cplex.getValue(x[i][j]) + " ");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	    	 
	}
	
	public int printReport() {
		int compnentsDead = 0;
		try {
			for(int i = 0; i < XCOUNT; i++)	{		 
				if (cplex.getValue(x[i][STEPS-1]) >0)
					compnentsDead ++;
			}
				
			System.out.println("Time Steps       : " + STEPS);
			System.out.println("Total Components : " + XCOUNT);
			System.out.println("Components Dead Initially : " + compDeadInit);
			System.out.println("Components Dead  : " + compnentsDead);
			System.out.println("Components Protected  : " + (compDeadInit - compnentsDead));		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return compnentsDead;
	}

	public static void main(String args[]) {
		
		EntityHardeningILP ex = new EntityHardeningILP("DataSet1", 15, 4);
		ex.optimize();
		// ex.printX();
		ex.printReport();
		System.out.println("Done");	
	}
}

