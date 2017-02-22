package TargetedEntityHardeningILP;

import java.util.ArrayList;

import KVulnerableEntityILP.DataGroup2_KVN;
import ilog.concert.*;
import ilog.cplex.*;

public class DataGroup2_TargetedEntityHardening_Defender_ILP {
	private int K=1;
	private int XCOUNT = 28;
	private int YCOUNT = 18;
	private int CCOUNT = 100;
	private int STEPS = XCOUNT + YCOUNT;
	
	
	// ILP variables
	IloCplex cplex;
	private IloIntVar[][] x;	
	private IloIntVar[][] y;
	private IloIntVar[][] c;
	private IloIntVar[] qx; // ent hard	
	private IloIntVar[] qy; // ent hard	
	DataGroup2_KVN attackerILP; // attackerILP // ent hard
	private int attackerBudget;
	private int[] gx; // input from run of attacker ILP - x variables
	private int[] gy; // input from run of attacker ILP - y variables
	public int totalConstraints = 1;
	private double constM = 100.0;
	private int cCounter = 0;
		
	public DataGroup2_TargetedEntityHardening_Defender_ILP(int attackerBudget) {
		try {
			cplex = new IloCplex();
			x = new IloIntVar[XCOUNT][STEPS];
			y = new IloIntVar[YCOUNT][STEPS];
			c = new IloIntVar[CCOUNT][STEPS];
			qx = new IloIntVar[XCOUNT]; // ent hard	
			qy = new IloIntVar[YCOUNT]; // ent hard
			this.attackerBudget = attackerBudget;		// ent hard
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
		
			// initialize attacker results 		// ent hard
			attackerILP = new DataGroup2_KVN();			
			attackerILP.setK(attackerBudget);
			attackerILP.optimize();
			gx = attackerILP.getInitialFailureX();
			gy = attackerILP.getInitialFailureY();
			
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
			// cplex.addMaximize(expr, "Objective: Max(sum(x[0..." + (XCOUNT-1) + "][" + (STEPS-1) + "] + sum(y[0..." + (YCOUNT-1) + "][" + (STEPS-1) + "])");
			cplex.addMinimize(expr, "Objective: Min(sum(x[0..." + (XCOUNT-1) + "][" + (STEPS-1) + "] + sum(y[0..." + (YCOUNT-1) + "][" + (STEPS-1) + "])");  // ent hard
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
		
	
	public void createConstraints() {
		try {
			int constMainCount = 1;
			int constSubCount = 1;			
			
			// defender budget constraint
			IloNumExpr expr = cplex.constant(0); 
			for (int i = 0;i<XCOUNT;i++)
				expr = cplex.sum(expr, qx[i]); // ent hard
			for (int i = 0;i<YCOUNT;i++)
				expr = cplex.sum(expr, qy[i]);			
			cplex.addLe(expr, K, "Constraint 1: sum(qx[i]) + sum(qy[j]) <= K, for all i=1.." + XCOUNT + ", j=1.." + YCOUNT);
			
			// If attacked entity is not defended it fails at t=0
			constMainCount++;
			constSubCount = 1;		
			for (int i = 0;i<XCOUNT;i++){				
				cplex.addEq(x[i][0], cplex.diff(gx[i], qx[i]), "Constraint " + constMainCount + "(" + constSubCount++ + "): x[" + i + "][0] >= gx[" + i + "] - qx[" + i + "]");				
			}			
			constMainCount++;
			constSubCount = 1;
			for (int i = 0;i<YCOUNT;i++){				
				cplex.addEq(y[i][0], cplex.diff(gy[i], qy[i]), "Constraint " + constMainCount + "(" + constSubCount++ + "): y[" + i + "][0] >= gy[" + i + "] - qy[" + i + "]");
			}
		
			// time step constraints // changed from KVN & RCA			
			constMainCount++;
			constSubCount = 1;
			for (int i = 0;i<XCOUNT;i++) {
				for (int t = 1;t<STEPS;t++) {
					cplex.addGe(x[i][t], x[i][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): x[" + i + "][" + t + "] >= x[" + i + "][" + (t-1) + "]");
					totalConstraints++;
				}
			}
			
			constMainCount++;
			constSubCount = 1;			
			for (int i = 0;i<YCOUNT;i++) {
				for (int t = 1;t<STEPS;t++) {
					cplex.addGe(y[i][t], y[i][t-1], "Constraint " + constMainCount + "(" + constSubCount++ + "): y[" + i + "][" + t + "] >= y[" + i + "][" + (t-1) + "]");
					totalConstraints++;
				}
			}		
			
			// D2
			type1_x(0, 5, 8 , 12);
			type1_x(1, 3, 9 , 13);
			type4_x(2, 2);
			type1_x(3, 0, 6, 14);			
			type1_x(4, 1, 6, 15);
			type3_x(5, 10, 16);
			type1_x(6, 4, 10, 17);
			type4_x(7, 4);
			
			type2_y(0, 3, 8, 4 , 9);
			type3_y(1, 4, 10);
			type2_y(2, 2, 11, 4, 12);
			type3_y(3, 1, 13);
			type2_y(4, 6, 14, 7, 15);
			type3_y(5, 0, 16);
			type2_y(6, 3, 17, 4, 18);
			type2_y(7, 3, 19, 5, 20);
			type2_y(8, 0, 21, 3, 22);
			type3_y(9, 1, 23);
			type2_y(10, 5, 24, 6, 25);
			type2_y(11, 1, 26, 4, 27);
						
			// transmission lines
			for (int i = 8; i < XCOUNT; i++) {
				for (int t = 1;t<STEPS;t++) {
					cplex.addEq(x[i][t], x[i][0]);
					totalConstraints++;
				}
			}
			
			// fiber lines not dependent on any other entity
			for (int i = 12; i < YCOUNT; i++) {
				for (int t = 1;t<STEPS;t++) {
					cplex.addEq(y[i][t], y[i][0]);
					totalConstraints++;
				}
			}
			
			
			// D1
			/*type1_x(0, 3, 10 , 15);
			type4_x(1, 5);
			type1_x(2, 4, 11, 16);
			type1_x(3, 4, 14, 17);			
			type1_x(4, 9, 13, 18);
			
			type2_y(0, 0, 28, 1 , 5);
			type3_y(1, 4, 6);
			type2_y(2, 0, 7, 4, 8);
			type3_y(3, 0, 9);
			type2_y(4, 2, 10, 3, 11);
			type2_y(5, 1, 12, 2, 13);
			type3_y(6, 1, 14);
			type2_y(7, 2, 15, 4, 16);
			type2_y(8, 1, 17, 4, 18);
			type3_y(9, 4, 19);
			type2_y(10, 2, 20, 4, 21);
			type2_y(11, 0, 22, 4, 23);
			type2_y(12, 0, 24, 3, 25);
			type3_y(13, 2, 26);
			type3_y(14, 4, 27);

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
			}*/
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void type1_x(int xindex, int yindex1, int yindex2, int yindex3) {
		// equations of the format:
		// x[xindex] =  y[yindex1] + y[yindex2] x y[yindex3]
		
		try {
			IloNumExpr expr = cplex.constant(0);
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(y[yindex2][t-1], y[yindex3][t-1]);
				cplex.addLe(c[cCounter][t-1], expr, "Constraint " + totalConstraints++ + ": c[" + cCounter + "][" + (t-1) + "] <= y[" + yindex2 + "][" + (t-1) + "] + y[" + yindex3 + "][" + (t-1) + "]");
			}

			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[cCounter][t-1], y[yindex2][t-1], "Constraint " + totalConstraints++ + ": c[" + cCounter + "][" + (t-1) + "] >= y[" + yindex2 + "][" + (t-1) + "]");
			}

			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[cCounter][t-1], y[yindex3][t-1], "Constraint " + totalConstraints++ + ": c[" + cCounter + "][" + (t-1) + "] >= y[" + yindex3 + "][" + (t-1) + "]");
			}

			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[cCounter][t-1], y[yindex1][t-1]);
				expr = cplex.diff(expr, 1);
				expr = cplex.diff(expr, qx[xindex]);
				//expr = cplex.prod(expr, 0.5);
				//expr = cplex.sum(expr, x[xindex][0]);
				//cplex.addLe(x[xindex][t], expr, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] <= 0.5(c[" + cCounter + "][" + (t-1) + "] + y[" + yindex1 + "][" + (t-1) + "]) + x[" + xindex + "][0]" );
				cplex.addGe(x[xindex][t], expr, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] >= (c[" + cCounter + "][" + (t-1) + "] + y[" + yindex1 + "][" + (t-1) + "]) - (2-1) - qx[" + xindex + "]" );
				
				expr = cplex.constant(0);
				expr = cplex.sum(c[cCounter][t-1], y[yindex1][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, x[xindex][0]);
				cplex.addLe(x[xindex][t], expr, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] <= 0.5(c[" + cCounter + "][" + (t-1) + "] + y[" + yindex1 + "][" + (t-1) + "]) + x[" + xindex + "][0]" );
			}
			
			

			/*for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[cCounter][t-1], y[yindex1][t-1]));
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, x[xindex][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] + " + constM + "(2 - y[" + yindex1 + "][" + (t-1) + "] - c[" + cCounter + "][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}*/
			cCounter++;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void type2_y(int yindex, int xindex1, int xindex2, int xindex3, int xindex4) {
		// equations of the format:
		// y[yindex] =  x[xindex1] x x[xindex2] + x[xindex3] x x[xindex4]
		// b[0][1] =  a[0][0] x a[28][0] + a[1][0] x a[5][0]
		try {
			IloNumExpr expr = cplex.constant(0);
			
			int c1 = cCounter;
			int c2 = cCounter + 1;
			cCounter += 2;
			
			for (int t = 1;t<STEPS;t++) {				
				expr = cplex.constant(0);
				expr = cplex.sum(x[xindex1][t-1], x[xindex2][t-1]);
				cplex.addLe(c[c1][t-1], expr, "Constraint " + totalConstraints++ + ": c[" + c1 + "][" + (t-1) + "] <= x[" + xindex1 + "][" + (t-1) + "] + x[" + xindex2 + "][" + (t-1) + "]");
			}			
					
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[c1][t-1], x[xindex1][t-1], "Constraint " + totalConstraints++ + ": c[" + c1 + "][" + (t-1) + "] >= x[" + xindex1 + "][" + (t-1) + "]");				
			}			
					
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[c1][t-1], x[xindex2][t-1], "Constraint " + totalConstraints++ + ": c[" + c1 + "][" + (t-1) + "] >= x[" + xindex2 + "][" + (t-1) + "]");				
			}
								
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(x[xindex3][t-1], x[xindex4][t-1]);
				cplex.addLe(c[c2][t-1], expr, "Constraint " + totalConstraints++ + ": c[" + c2 + "][" + (t-1) + "] <= x[" + xindex3 + "][" + (t-1) + "] + y[2][" + (t-1) + "]");
			}			
			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[c2][t-1], x[xindex3][t-1], "Constraint " + totalConstraints++ + ": c[" + c2 + "][" + (t-1) + "] >= x[" + xindex3 + "][" + (t-1) + "]");				
			}
			
			for (int t = 1;t<STEPS;t++) {
				cplex.addGe(c[c2][t-1], x[xindex4][t-1], "Constraint " + totalConstraints++ + ": c[" + c2 + "][" + (t-1) + "] >= x[" + xindex4 + "][" + (t-1) + "]");			
			}
			
			for (int t = 1;t<STEPS;t++) {
				expr = cplex.constant(0);
				expr = cplex.sum(c[c1][t-1], c[c2][t-1]);
				expr = cplex.diff(expr, 1);
				expr = cplex.diff(expr, qy[yindex]);
				//expr = cplex.prod(expr, 0.5);
				//expr = cplex.sum(expr, y[yindex][0]);
				//cplex.addLe(y[yindex][t], expr, "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] <= 0.5(c[" + c1 + "][" + (t-1) + "] + c[" + c2 + "][" + (t-1) + "]) + y[" + yindex + "][0]");
				cplex.addGe(y[yindex][t], expr, "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] >= (c[" + c1 + "][" + (t-1) + "] + c[" + c2 + "][" + (t-1) + "]) - (2-1) - qy[" + yindex + "]");
				
				expr = cplex.constant(0);
				expr = cplex.sum(c[c1][t-1], c[c2][t-1]);
				expr = cplex.prod(expr, 0.5);
				expr = cplex.sum(expr, y[yindex][0]);
				cplex.addLe(y[yindex][t], expr, "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] <= 0.5(c[" + c1 + "][" + (t-1) + "] + c[" + c2 + "][" + (t-1) + "]) + y[" + yindex + "][0]");
			}
			
			/*for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 2);
				expr2 = cplex.diff(expr2, cplex.sum(c[c1][t-1], c[c2][t-1]));
				expr2 = cplex.prod(expr2, constM);
					
				expr = cplex.constant(0);
				expr = cplex.sum(expr, y[yindex][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] + " + constM + "(2 - c[" + c1 + "][" + (t-1) + "] - c[" + c2 + "][" + (t-1) + "]) >= 1");				
			}*/
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	private void type3_y(int yindex, int xindex1, int xindex2) {
		// equations of the format:
		// y[yindex] =  x[xindex1] x x[xindex2]
		try {
			IloNumExpr expr = cplex.constant(0);
			for (int t = 1;t<STEPS;t++) {
				/*
				expr = cplex.constant(0);
				expr = cplex.sum(x[xindex1][t-1], x[xindex2][t-1]);				
				expr = cplex.sum(expr, y[yindex][0]);
				cplex.addLe(y[yindex][t], expr, "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] <= x[" + xindex1 + "][" + (t-1) + "] + x[" + xindex2 + "][" + (t-1) + "]");				
				cplex.addGe(y[yindex][t], x[xindex1][t-1], "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] >= x[" + xindex1 + "][" + (t-1) + "]");
				cplex.addGe(y[yindex][t], x[xindex2][t-1], "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] >= x[" + xindex2 + "][" + (t-1) + "]");				
				*/
				expr = cplex.constant(0);
				expr = cplex.sum(x[xindex1][t-1], x[xindex2][t-1]);				
				cplex.addLe(y[yindex][t], expr, "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] <= x[" + xindex1 + "][" + (t-1) + "] + x[" + xindex2 + "][" + (t-1) + "]");
				cplex.addGe(y[yindex][t], cplex.diff(x[xindex1][t-1], qy[yindex]), "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] >= x[" + xindex1 + "][" + (t-1) + "] - qy[" + yindex + "]");
				cplex.addGe(y[yindex][t], cplex.diff(x[xindex2][t-1], qy[yindex]), "Constraint " + totalConstraints++ + ": y[" + yindex + "][" + t + "] >= x[" + xindex2 + "][" + (t-1) + "] - qy[" + yindex + "]");
				
			}	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void type3_x(int xindex, int yindex1, int yindex2) {
		// equations of the format:
		// x[xindex] =  y[yindex1] x y[yindex2] 
		try {
			IloNumExpr expr = cplex.constant(0);
			for (int t = 1;t<STEPS;t++) {
				/*
				expr = cplex.constant(0);
				expr = cplex.sum(y[yindex1][t-1], y[yindex2][t-1]);				
				expr = cplex.sum(expr, x[xindex][0]);
				cplex.addLe(x[xindex][t], expr, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] <= y[" + yindex1 + "][" + (t-1) + "] + y[" + yindex2 + "][" + (t-1) + "]");
				cplex.addGe(x[xindex][t], y[yindex1][t-1], "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] >= y[" + yindex1 + "][" + (t-1) + "]");
				cplex.addGe(x[xindex][t], y[yindex2][t-1], "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] >= y[" + yindex2 + "][" + (t-1) + "]");
				*/
				expr = cplex.constant(0);
				expr = cplex.sum(y[yindex1][t-1], y[yindex2][t-1]);
				cplex.addLe(x[xindex][t], expr, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] <= y[" + yindex1 + "][" + (t-1) + "] + y[" + yindex2 + "][" + (t-1) + "]");
				cplex.addGe(x[xindex][t], cplex.diff(y[yindex1][t-1], qx[xindex]), "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] >= y[" + yindex1 + "][" + (t-1) + "] - qx[" + xindex + "]");
				cplex.addGe(x[xindex][t], cplex.diff(y[yindex2][t-1], qx[xindex]), "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] >= y[" + yindex2 + "][" + (t-1) + "] - qx[" + xindex + "]");
			}	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void type4_x(int xindex, int yindex) {
		// equations of the format:
		// x[xindex] =  y[yindex] 
		// a[1][1] =  b[5][0]
		try {
			//IloNumExpr expr = cplex.constant(0);
			for (int t = 1;t<STEPS;t++) {
				/*
				expr = cplex.constant(0);
				expr = cplex.sum(y[yindex][t-1], x[xindex][0]);
				cplex.addLe(x[xindex][t], expr, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] <= y[" + yindex + "][" + (t-1) + "] + x[" + xindex + "][0]");
				*/
				cplex.addLe(x[xindex][t], y[yindex][t-1], "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] <= y[" + yindex + "][" + (t-1) + "]");
				cplex.addGe(x[xindex][t], cplex.diff(y[yindex][t-1], qx[xindex]), "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] >= y[" + yindex + "][" + (t-1) + "] - qx[" + xindex + "]");
			}
			
			/*for (int t = 1;t<STEPS;t++) {					
				IloNumExpr expr2 = cplex.constant(0);
				expr2 = cplex.sum(expr2, 1);
				expr2 = cplex.diff(expr2, y[yindex][t-1]);
				expr2 = cplex.prod(expr2, constM);

				expr = cplex.constant(0);
				expr = cplex.sum(expr, x[xindex][t]);
				expr = cplex.sum(expr, expr2);
				cplex.addGe(expr, 1, "Constraint " + totalConstraints++ + ": x[" + xindex + "][" + t + "] + " + constM + "(1 - y[" + yindex + "][" + (t-1) + "]) >= 1");
				totalConstraints++;
			}*/	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public void createXVariables() {
		try {
			for (int i = 0; i < XCOUNT; i++) {				
				x[i] = cplex.intVarArray(STEPS, 0, 1);
				qx[i] = cplex.intVar(0, 1);	 // ent hard
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	public void createYVariables() {
		try {
			for (int i = 0; i < YCOUNT; i++) {				
				y[i] = cplex.intVarArray(STEPS, 0, 1);				
				qy[i] = cplex.intVar(0, 1);  // ent hard
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
					if (cplex.getValue(qx[i]) >0) {
						System.out.print("* ");
						continue;
					}
						
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
					if (cplex.getValue(qy[i]) >0) {
						System.out.print("* ");
						continue;
					}
					
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
			System.out.println("Attacker Budget  : " + attackerBudget);
			System.out.println("Defender Budget  : " + K);
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
		ArrayList <Integer> defenderBgt = new ArrayList <Integer>();
		ArrayList <Integer> killed = new ArrayList <Integer>();
		int attackerBgt = 8;
		for (int bgt=5;bgt<8;bgt++) {
			DataGroup2_TargetedEntityHardening_Defender_ILP ex = new DataGroup2_TargetedEntityHardening_Defender_ILP(attackerBgt);
			//NetSciCom_DataGroup1_KVN ex = new NetSciCom_DataGroup1_KVN();
			//DataGroup1_EntityHardening_Defender_ILP ex = new DataGroup1_EntityHardening_Defender_ILP();
			ex.setK(bgt); // needed
			//ex.setK(6);
			ex.optimize(); //needed
			//ex.printC();
			//System.out.println();
			//ex.printX();
			//System.out.println();
			//ex.printY();
			//System.out.println("\n");
			//System.out.println("Total Constraints: " + ex.totalConstraints);		
			ex.printPretty();
			ex.printReport();
			defenderBgt.add(bgt);
			//killed.add(ex.printReport());
			
			// req 0 1 3 5 7
			if(bgt!=0)
				bgt++;
		}		
		/*for(int i=0;i<defenderBgt.size();i++) {
			System.out.println(defenderBgt.get(i) + "\t" + killed.get(i));
		}*/
		
	}

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
	}
}

