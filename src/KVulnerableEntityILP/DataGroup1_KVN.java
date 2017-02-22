package KVulnerableEntityILP;

import java.util.ArrayList;

import ilog.concert.*;
import ilog.cplex.*;

public class DataGroup1_KVN {
	private int K=1;
	private int XCOUNT = 29;
	private int YCOUNT = 19;
	private int CCOUNT = 22;
	private int STEPS = XCOUNT + YCOUNT;
	
	
	// ILP variables
	IloCplex cplex;
	private IloIntVar[][] x;	
	private IloIntVar[][] y;
	private IloIntVar[][] c;
	public int totalConstraints = 1;
	private double constM = 100.0;
		
	public DataGroup1_KVN() {
		try {
			cplex = new IloCplex();
			x = new IloIntVar[XCOUNT][STEPS];
			y = new IloIntVar[YCOUNT][STEPS];
			c = new IloIntVar[CCOUNT][STEPS];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void optimize() {
		try {
			createXVariables();
			createYVariables();
			createCVariables();
		
			createConstraints();
			createObjective();
		
			//System.out.println(cplex.toString());
			
			if ( cplex.solve() ) {
				System.out.println("Obj " + cplex.getObjValue());
				double eps = cplex.getParam(IloCplex.DoubleParam.EpInt);
				System.out.println(eps);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createObjective() {
		try {
			IloIntExpr expr = cplex.constant(0);
			for (int i = 0;i<XCOUNT;i++)
				expr = cplex.sum(expr, x[i][STEPS-1]);
			for (int i = 0;i<YCOUNT;i++)
				expr = cplex.sum(expr, y[i][STEPS-1]);	
			cplex.addMaximize(expr, "Objective: Max(sum(x[0..." + (XCOUNT-1) + "][" + (STEPS-1) + "] + sum(y[0..." + (YCOUNT-1) + "][" + (STEPS-1) + "])");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
		
	
	public void createConstraints() {
		try {
			int constMainCount = 1;
			int constSubCount = 1;			
			
			// budget constraint
			IloNumExpr expr = cplex.constant(0);
			for (int i = 0;i<XCOUNT;i++)
				expr = cplex.sum(expr, x[i][0]);
			for (int i = 0;i<YCOUNT;i++)
				expr = cplex.sum(expr, y[i][0]);			
			cplex.addLe(expr, K, "Constraint 1: sum(x[i][0]) + sum(y[j][0]) <= K, for all i=1.." + XCOUNT + ", j=1.." + YCOUNT);			
		
			// time step constraints			
			constMainCount++;
			constSubCount = 1;
			for (int i = 0;i<XCOUNT;i++) {
				for (int t = 0;t<STEPS-1;t++) {
					cplex.addLe(x[i][t], x[i][t+1], "Constraint " + constMainCount + "(" + constSubCount++ + "): x[" + i + "][" + t + "] <= x[" + i + "][" + (t+1) + "]");
					totalConstraints++;
				}
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int i = 0;i<YCOUNT;i++) {
				for (int t = 0;t<STEPS-1;t++) {
					cplex.addLe(y[i][t], y[i][t+1], "Constraint " + constMainCount + "(" + constSubCount++ + "): y[" + i + "][" + t + "] <= y[" + i + "][" + (t+1) + "]");
					totalConstraints++;
				}
			}
		
			// CNE
			// equation 1			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {				
				expr = cplex.constant(0);
				expr = cplex.sum(x[0][t-1], x[28][t-1]);
				cplex.addLe(c[0][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[0][t-1], x[0][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[0][t-1], x[28][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[1][t-1], x[5][t-1]);
				cplex.addLe(c[1][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[1][t-1], x[1][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[1][t-1], x[5][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;					
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[0][t-1], c[1][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[0][0]);
				cplex.addLe(y[0][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[0][t-1], c[1][t-1]));
				expr2 = cplex.prod(expr2, constM);
					
				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[0][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
		
		// equation 2
			constMainCount++;
			constSubCount = 1;					
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[6][t-1]);				
				expr = cplex.sum(expr, y[1][0]);
				cplex.addLe(y[1][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				cplex.addGe(y[1][t], x[4][t-1]);
				cplex.addGe(y[1][t], x[6][t-1]);
				totalConstraints++;
			}
				
		// equation 3
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[0][t-1], x[7][t-1]);
				cplex.addLe(c[2][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[8][t-1]);
				cplex.addLe(c[3][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[2][t-1], x[0][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[2][t-1], x[7][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[3][t-1], x[4][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[3][t-1], x[8][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[2][t-1], c[3][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[2][0]);
				cplex.addLe(y[2][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[2][t-1], c[3][t-1]));
				expr2 = cplex.prod(expr2, constM);
					
				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[2][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
		
		// equation 4
			constMainCount++;
			constSubCount = 1;					
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[0][t-1], x[9][t-1]);				
				expr = cplex.sum(expr, y[3][0]);
				cplex.addLe(y[3][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				cplex.addGe(y[3][t], x[0][t-1]);
				cplex.addGe(y[3][t], x[9][t-1]);
				totalConstraints++;
			}
		
		// equation 5
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[2][t-1], x[10][t-1]);
				cplex.addLe(c[4][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[3][t-1], x[11][t-1]);
				cplex.addLe(c[5][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[4][t-1], x[2][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[4][t-1], x[10][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[5][t-1], x[3][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[5][t-1], x[11][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[4][t-1], c[5][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[4][0]);
				cplex.addLe(y[4][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[4][t-1], c[5][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[4][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
		// equation 6
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[1][t-1], x[12][t-1]);
				cplex.addLe(c[6][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[2][t-1], x[13][t-1]);
				cplex.addLe(c[7][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[6][t-1], x[1][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[6][t-1], x[12][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[7][t-1], x[2][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[7][t-1], x[13][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[6][t-1], c[7][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[5][0]);
				cplex.addLe(y[5][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[6][t-1], c[7][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[5][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
		// equation 7
			constMainCount++;
			constSubCount = 1;					
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[1][t-1], x[14][t-1]);				
				expr = cplex.sum(expr, y[6][0]);
				cplex.addLe(y[6][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				cplex.addGe(y[6][t], x[1][t-1]);
				cplex.addGe(y[6][t], x[14][t-1]);
				totalConstraints++;
			}
		
		// equation 8
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[2][t-1], x[15][t-1]);
				cplex.addLe(c[8][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[16][t-1]);
				cplex.addLe(c[9][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[8][t-1], x[2][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[8][t-1], x[15][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[9][t-1], x[4][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[9][t-1], x[16][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[8][t-1], c[9][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[7][0]);
				cplex.addLe(y[7][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[8][t-1], c[9][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[7][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
		// equation 9
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[1][t-1], x[17][t-1]);
				cplex.addLe(c[10][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[18][t-1]);
				cplex.addLe(c[11][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[10][t-1], x[1][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[10][t-1], x[17][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[11][t-1], x[4][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[11][t-1], x[18][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[10][t-1], c[11][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[8][0]);
				cplex.addLe(y[8][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[10][t-1], c[11][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[8][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
		// equation 10
			constMainCount++;
			constSubCount = 1;					
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[19][t-1]);				
				expr = cplex.sum(expr, y[9][0]);
				cplex.addLe(y[9][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				cplex.addGe(y[9][t], x[4][t-1]);
				cplex.addGe(y[9][t], x[19][t-1]);
				totalConstraints++;
			}
			
		// equation 11
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[2][t-1], x[20][t-1]);
				cplex.addLe(c[12][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[21][t-1]);
				cplex.addLe(c[13][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[12][t-1], x[2][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[12][t-1], x[20][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[13][t-1], x[4][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[13][t-1], x[21][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[12][t-1], c[13][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[10][0]);
				cplex.addLe(y[10][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[12][t-1], c[13][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[10][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
		// equation 12
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[0][t-1], x[22][t-1]);
				cplex.addLe(c[14][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[23][t-1]);
				cplex.addLe(c[15][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[14][t-1], x[0][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[14][t-1], x[22][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[15][t-1], x[4][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[15][t-1], x[23][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[14][t-1], c[15][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[11][0]);
				cplex.addLe(y[11][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[14][t-1], c[15][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[11][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
		// equation 13
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[0][t-1], x[24][t-1]);
				cplex.addLe(c[16][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[3][t-1], x[25][t-1]);
				cplex.addLe(c[17][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[16][t-1], x[0][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[16][t-1], x[24][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[17][t-1], x[3][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[17][t-1], x[25][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[16][t-1], c[17][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[12][0]);
				cplex.addLe(y[12][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[16][t-1], c[17][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[12][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
		// equation 14
			constMainCount++;
			constSubCount = 1;					
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[2][t-1], x[26][t-1]);				
				expr = cplex.sum(expr, y[13][0]);
				cplex.addLe(y[13][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				cplex.addGe(y[13][t], x[2][t-1]);
				cplex.addGe(y[13][t], x[26][t-1]);
				totalConstraints++;
			}
			
		// equation 15
			constMainCount++;
			constSubCount = 1;					
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[4][t-1], x[27][t-1]);				
				expr = cplex.sum(expr, y[14][0]);
				cplex.addLe(y[14][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				cplex.addGe(y[14][t], x[4][t-1]);
				cplex.addGe(y[14][t], x[27][t-1]);
				totalConstraints++;
			}
			
		// PNE
			// equation 1			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(y[10][t-1], y[15][t-1]);
				cplex.addLe(c[18][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[18][t-1], y[10][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[18][t-1], y[15][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[18][t-1], y[3][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, x[0][0]);
				cplex.addLe(x[0][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[18][t-1], y[3][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, x[0][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
			// equation 2
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				// expr = cplex.sum(y[5][t-1], x[1][t-1]);
				expr = cplex.sum(y[5][t-1], x[1][0]);
				cplex.addLe(x[1][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}
			
			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 1);
				expr2 = cplex.diff(expr2, y[5][t-1]);
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, x[1][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
			// equation 3			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(y[11][t-1], y[16][t-1]);
				cplex.addLe(c[19][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[19][t-1], y[11][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[19][t-1], y[16][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[19][t-1], y[4][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, x[2][0]);
				cplex.addLe(x[2][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[19][t-1], y[4][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, x[2][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}

			// equation 4			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(y[14][t-1], y[17][t-1]);
				cplex.addLe(c[20][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[20][t-1], y[14][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[20][t-1], y[17][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[20][t-1], y[4][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, x[3][0]);
				cplex.addLe(x[3][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[20][t-1], y[4][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, x[3][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
			// equation 5			
			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(y[13][t-1], y[18][t-1]);
				cplex.addLe(c[21][t-1], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] <= y[0][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[21][t-1], y[13][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[0][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[21][t-1], y[18][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): c[0][" + (t-1) + "] >= y[2][" + (t-1) + "]");
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;		
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[21][t-1], y[9][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, x[4][0]);
				cplex.addLe(x[4][t], expr, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[1][" + t + "] <= 0.5(c[0][" + (t-1) + "] + y[1][" + (t-1) + "]) + sum(x[1][0.." + (t-1) + "]" );
				totalConstraints++;
			}

			constMainCount++;
			constSubCount = 1;
			for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[21][t-1], y[9][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, x[4][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + constMainCount + "(" + constSubCount++ + "): x[0][" + t + "] + " + constM + "(2 - y[0][" + (t-1) + "] - y[1][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}
			
			// transmission lines
			for (int i = 5; i < XCOUNT; i++) {
				for (int t = 1;t<STEPS;t++) {
					cplex.addEq(x[i][t], x[i][0]);
					totalConstraints++;
				}
			}
			
			// fiber lines not dependent on any other entity
			for (int i = 15; i < YCOUNT; i++) {
				for (int t = 1;t<STEPS;t++) {
					cplex.addEq(y[i][t], y[i][0]);
					totalConstraints++;
				}
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
	
	public void createYVariables() {
		try {
			for (int i = 0; i < YCOUNT; i++) {				
				y[i] = cplex.intVarArray(STEPS, 0, 1);				
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
	
	public void printX() {
		try {
			System.out.println("\nX: ");
			for(int i = 0; i < XCOUNT; i++) {
				System.out.println();
				for (int j = 0; j <STEPS; j++) {
					System.out.print(cplex.getValue(x[i][j]) + " ");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	    	 
	}
	
	public void printY() {
		try {
			System.out.println("\nY: ");
			for(int i = 0; i < YCOUNT; i++) {
				System.out.println();
				for (int j = 0; j <STEPS; j++) {
					System.out.print(cplex.getValue(y[i][j]) + " ");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	    	 
	}
	
	public void printC() {
		try {
			System.out.println("\nC: ");			
			for(int i = 0; i < CCOUNT; i++) {
				System.out.println();
				for (int j = 0; j <STEPS-1; j++) {		
					System.out.print(cplex.getValue(c[i][j]) + " ");					
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	    	 
	}
	
	public void printPretty() {
		try {
			System.out.println("\nX: ");
			for(int i = 0; i < XCOUNT; i++) {
				System.out.println();
				for (int j = 0; j <STEPS; j++) {
					if (cplex.getValue(x[i][j]) >0)
						System.out.print("1 ");
					else
						System.out.print("0 ");
				}
			}
			System.out.println("\n\nY: ");
			for(int i = 0; i < YCOUNT; i++) {
				System.out.println();
				for (int j = 0; j <STEPS; j++) {
					if (cplex.getValue(y[i][j]) >0)
						System.out.print("1 ");
					else
						System.out.print("0 ");
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
					compnentsDead++;	
			for(int i = 0; i < YCOUNT; i++)				
				if (cplex.getValue(y[i][STEPS-1]) >0)
					compnentsDead++;
			System.out.println("\n\n==============================================");
			System.out.println("Budget           : " + K);
			System.out.println("Time Steps       : " + STEPS);
			System.out.println("Total Components : " + (XCOUNT + YCOUNT));
			System.out.println("Components Dead  : " + compnentsDead);
			System.out.println("==============================================");			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return compnentsDead;
	}
	
	
	public static void main(String args[]) {
		ArrayList <Integer> budget = new ArrayList <Integer>();
		ArrayList <Integer> killed = new ArrayList <Integer>();
		for (int bgt=1;bgt<3;bgt++) {
			DataGroup1_KVN ex = new DataGroup1_KVN();
			ex.setK(bgt);
			//ex.setK(6);
			ex.optimize();
//		ex.printC();
//		System.out.println();
//		//ex.printX();
//		System.out.println();
//		//ex.printY();
//		System.out.println("\n");
			System.out.println("Total Constraints: " + ex.totalConstraints);		
		//ex.printPretty();
			budget.add(bgt);
			killed.add(ex.printReport());
		}
		
		for(int i=0;i<budget.size();i++) {
			System.out.println(budget.get(i) + "\t" + killed.get(i));
		}
		
	}

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
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

	public int[] getInitialFailureY() {
		int[] r = new int[YCOUNT];
		try {
			for(int i = 0; i < YCOUNT; i++) {
				if (cplex.getValue(y[i][0]) > 0)
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
}

