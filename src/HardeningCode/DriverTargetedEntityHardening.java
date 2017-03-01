package HardeningCode;

import java.io.File;
import java.io.FileNotFoundException;

public class DriverTargetedEntityHardening {
	public static void main(String[] args) throws FileNotFoundException{
		String fileName = "DataSet1";
		int KVal = 20;
		int protectBudget = 4;
		
		System.out.println("ILP Output");
		long startTime = System.currentTimeMillis();
		TargetedEntityHardeningILP Object = new TargetedEntityHardeningILP(fileName, KVal, protectBudget);
		Object.optimize();
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("==============================================");	
		Object.printReport();
		System.out.println("Total Time: " + (double) totalTime / 1000.0 + " seconds"); 
		System.out.println("==============================================");	
		
		System.out.println("Heuristic Output");
		startTime = System.currentTimeMillis();
		TargetedEntityHardeningHeuristic Object2 = new TargetedEntityHardeningHeuristic(fileName);
		Object2.compute();
		endTime   = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Components Protected: " +  Object2.getTotalProtected() + " \nComponents Def: " + Object2.getTotalHardened());
		System.out.println("Total Time: " + (double) totalTime / 1000.0 + " seconds"); 
		System.out.println("==============================================");	
		
		// Delete all files created for heuristic
		File dir = new File("OutFileForHeuristics/");
		for(File file: dir.listFiles()) 
		    if (!file.isDirectory()) 
		        file.delete();
	}
}
