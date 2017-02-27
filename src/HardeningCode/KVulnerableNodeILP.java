package HardeningCode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import ilog.concert.*;
import ilog.cplex.*;

public class KVulnerableNodeILP {
	private HashMap<String, Integer> entityLabeltoIndexMap; 
	private HashMap<String, Integer> mintermLabelToIndexMap;
	private HashMap<String, List<String>> IIRs;
	private String fileName;
	private int K;
	private int XCOUNT;
	private int CCOUNT;
	private int STEPS;
	
	
	// ILP variables
	IloCplex cplex;
	private IloIntVar[][] x;	
	private IloIntVar[][] c;
	private double constM = 100.0;
		
	public KVulnerableNodeILP(String file, int KVal) {
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
			x = new IloIntVar[XCOUNT][STEPS];
			c = new IloIntVar[CCOUNT][STEPS];
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
			cplex.addMaximize(expr);
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
			// Budget Constraint
			expr = cplex.constant(0);
			for (int i = 0;i < XCOUNT; i++)
				expr = cplex.sum(expr, x[i][0]);			
			cplex.addLe(expr, K);	
			
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
					expr2 = cplex.prod(expr2, 1.0 / minCount);
					expr2 = cplex.sum(expr2, x[entityLabeltoIndexMap.get(str)][0]);
					cplex.addLe(x[entityLabeltoIndexMap.get(str)][t], expr2);
					expr3 = cplex.sum(expr3, minCount);
					expr3 = cplex.diff(expr3, expr2);
					expr3 = cplex.prod(expr3, constM);
					expr3 = cplex.sum(expr3, x[entityLabeltoIndexMap.get(str)][t]);
					cplex.addGe(expr3, 1);
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
			for(int i = 0; i < XCOUNT; i++)				 
				if (cplex.getValue(x[i][STEPS-1]) >0)
					compnentsDead ++;
			for(int i = 0; i < XCOUNT; i++){				 
				if (cplex.getValue(x[i][0]) >0){
					// System.out.println(i);
				}
			}
			System.out.println("\n\n==============================================");
			System.out.println("Time Steps       : " + STEPS);
			System.out.println("Total Components : " + XCOUNT);
			System.out.println("Components Dead  : " + compnentsDead);
			System.out.println("==============================================");			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return compnentsDead;
	}

	public int[] getInitialFailureX() {
		int[] r = new int[XCOUNT];
		try {
			for(int i = 0; i < XCOUNT; i++) {
				if (cplex.getValue(x[i][0]) > 0)
					r[i] = 1;
				else
					r[i] = 0;
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return r;
	}
	
	public List<Integer> getFinalFailureX() {
		List<Integer> r = new ArrayList<Integer>();
		try {
			for(int i = 0; i < XCOUNT; i++) {
				if (cplex.getValue(x[i][STEPS - 1]) > 0)
					r.add(i);
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return r;
	}
	
	public void generateFileForHeuristic() throws IOException{
		StringBuilder sb = new StringBuilder();
		List<Integer> finalFailure = getFinalFailureX();
		int[] initialFailure = getInitialFailureX();
		File caseFile = new File("OutputFiles/" + fileName + ".txt");
		Scanner scan = new Scanner(caseFile);
		
		while(scan.hasNext()){
			String exp = scan.nextLine();
			StringBuilder firstEntity = new StringBuilder();
			int index = 0;
			while(exp.charAt(index) != ' '){
				firstEntity.append(exp.charAt(index));
				index ++;
			}
			if(!finalFailure.contains(entityLabeltoIndexMap.get(firstEntity.toString()))) continue;
			if(initialFailure[entityLabeltoIndexMap.get(firstEntity.toString())] == 1) continue;
			sb.append(firstEntity.toString() + " <-");
			index ++;
			while(exp.charAt(index) != ' '){
				index ++;
			}
			String[] minterms = exp.substring(index + 1, exp.length()).split("   ");
			index = 0;
			for(String str: minterms){
				String[] vals = str.split(" ");
				for(String entity: vals){
					if(finalFailure.contains(entityLabeltoIndexMap.get(entity))) sb.append(" " + entity);
				}
				if(index < minterms.length) sb.append("  ");
				index ++;
			}
			sb.append("\n");
		}
		scan.close();
		File file = new File("OutFileForHeuristics/" + fileName + ".txt");
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new FileWriter(file));
		    writer.append(sb);
		} finally {
		    if (writer != null) writer.close();
		}
	}

	public static void main(String args[]) throws IOException {
		
		KVulnerableNodeILP ex = new KVulnerableNodeILP("case9IIRsAtTimeStep1", 4);
		ex.optimize();
		// ex.printX();
		ex.printReport();
		ex.generateFileForHeuristic();
		System.out.println("Done");	
}
}

