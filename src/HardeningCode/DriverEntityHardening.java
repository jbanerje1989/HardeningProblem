package HardeningCode;

import java.io.File;
import java.io.FileNotFoundException;

public class DriverEntityHardening {
	public static void main(String[] args) throws FileNotFoundException{
		String fileName = "case39IIRsAtTimeStep1";
		int KVal = 16;
		int hardeningBudget = 14;
		
		System.out.println("ILP Output");
		long startTime = System.currentTimeMillis();
		EntityHardeningILP Object = new EntityHardeningILP(fileName, KVal, hardeningBudget);
		Object.optimize();
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("==============================================");	
		Object.printReport();
		System.out.println("Total Time: " + (double) totalTime / 1000.0 + " seconds"); 
		System.out.println("==============================================");	
		
		System.out.println("Heuristic Output");
		startTime = System.currentTimeMillis();
		EntityHardeningHeuristic Object2 = new EntityHardeningHeuristic(fileName, hardeningBudget);
		Object2.compute();
		endTime   = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Components Protected: " +  Object2.getTotalProtected());
		System.out.println("Total Time: " + (double) totalTime / 1000.0 + " seconds"); 
		System.out.println("==============================================");	
		
		// Delete all files created for heuristic
		File dir = new File("OutFileForHeuristics/");
		for(File file: dir.listFiles()) 
		    if (!file.isDirectory()) 
		        file.delete();
	}
}
